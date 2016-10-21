/***
 * Copyright © Mapotempo, 2016
 *
 * This file is part of Mapotempo.
 *
 * Mapotempo is free software. You can redistribute it and/or
 * modify since you respect the terms of the GNU Affero General
 * Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * Mapotempo is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the Licenses for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Mapotempo. If not, see:
 * <http://www.gnu.org/licenses/agpl.html>
***/
package com.mapotempo.optimizer.jsprit;

import com.graphhopper.jsprit.core.algorithm.state.StateId;
import com.graphhopper.jsprit.core.algorithm.state.StateManager;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.constraint.ConstraintManager;
import com.graphhopper.jsprit.core.problem.job.*;
import com.graphhopper.jsprit.core.util.Resource;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.mapotempo.optimizer.jsprit.Constraints.InDirectSequence;
import com.mapotempo.optimizer.jsprit.Constraints.InOrder;
import com.mapotempo.optimizer.jsprit.Constraints.InSameRoute;
import com.mapotempo.optimizer.jsprit.Constraints.MinimalVehicleLapse;
import com.mapotempo.optimizer.jsprit.Status.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class RelationXMLReader {

    private static Logger logger = LoggerFactory.getLogger(RelationXMLReader.class);

    private VehicleRoutingProblem vrp;

    private StateManager stateManager;

    private ConstraintManager constraintManager;

    private boolean schemaValidation = true;

    Hashtable<Integer, StateId> RouteStateHash;
    Hashtable<Integer, StateId> IndexStateHash;

    Hashtable<Integer, ArrayList<Integer>> sameRouteHash;
    Hashtable<Integer, ArrayList<Integer>> orderHash;
    Hashtable<Integer, ArrayList<Integer>> reverseOrderHash;
    Hashtable<Integer, Integer> sequenceHash;
    Hashtable<Integer, Integer> reverseSequenceHash;

    /**
     * @param schemaValidation the schemaValidation to set
     */
    @SuppressWarnings("UnusedDeclaration")
    public void setSchemaValidation(boolean schemaValidation) {
        this.schemaValidation = schemaValidation;
    }

    public RelationXMLReader(VehicleRoutingProblem vrp, StateManager stateManager, ConstraintManager constraintManager) {
        this.vrp = vrp;
        this.stateManager = stateManager;
        this.constraintManager = constraintManager;
        RouteStateHash = new Hashtable<Integer, StateId>();
        IndexStateHash = new Hashtable<Integer, StateId>();

        sameRouteHash = new Hashtable<Integer, ArrayList<Integer>>();
        orderHash = new Hashtable<Integer, ArrayList<Integer>>();
        reverseOrderHash = new Hashtable<Integer, ArrayList<Integer>>();
        sequenceHash = new Hashtable<Integer, Integer>();
        reverseSequenceHash = new Hashtable<Integer, Integer>();
    }

    public void read(String filename) {
        logger.debug("read relation: {}", filename);
        XMLConfiguration xmlConfig = createXMLConfiguration();
        try {
            xmlConfig.load(filename);
        } catch (ConfigurationException e) {
            throw new RuntimeException(e);
        }
        read(xmlConfig);
    }

    public void read(InputStream fileContents) {
        XMLConfiguration xmlConfig = createXMLConfiguration();
        try {
            xmlConfig.load(fileContents);
        } catch (ConfigurationException e) {
            throw new RuntimeException(e);
        }
        read(xmlConfig);
    }

    private XMLConfiguration createXMLConfiguration() {
        XMLConfiguration xmlConfig = new XMLConfiguration();
        xmlConfig.setAttributeSplittingDisabled(true);
        xmlConfig.setDelimiterParsingDisabled(true);

        if (schemaValidation) {
            final InputStream resource = Resource.getAsInputStream("relation_xml_schema.xsd");
            if (resource != null) {
                EntityResolver resolver = new EntityResolver() {

                    @Override
                    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
                        {
                            InputSource is = new InputSource(resource);
                            return is;
                        }
                    }
                };
                xmlConfig.setEntityResolver(resolver);
                xmlConfig.setSchemaValidation(true);
            } else {
                logger.debug("cannot find schema-xsd file (relation_xml_schema.xsd). try to read xml without xml-file-validation.");
            }
        }
        return xmlConfig;
    }

    private void read(XMLConfiguration xmlConfig) {
        readSameRoute(xmlConfig);
        readOrder(xmlConfig);
        readSequence(xmlConfig);
        readMinimalVehicleLapse(xmlConfig);

        stateManager.addStateUpdater(new RouteStatusUpdater(stateManager, RouteStateHash));
        stateManager.addStateUpdater(new IndexStatusUpdater(stateManager, IndexStateHash));
    }

    private void readSameRoute(XMLConfiguration config) {
        sameRouteHash = new Hashtable<Integer, ArrayList<Integer>>();
        List<HierarchicalConfiguration> sameRoutesConfigs = config.configurationsAt("sameRouteRelations");
        int index = 1;
        for (HierarchicalConfiguration sameRouteConfig : sameRoutesConfigs) {
            List<HierarchicalConfiguration> sameRoutesIdConfigs = sameRouteConfig.configurationsAt("serviceIds.serviceId");
            if (!sameRoutesIdConfigs.isEmpty()) {
                for (HierarchicalConfiguration idConfig : sameRoutesIdConfigs) {
                    ListIterator<HierarchicalConfiguration> iterator = sameRoutesIdConfigs.listIterator(index);
                    String serviceId = idConfig.getString("");
                    Job job = vrp.getJobs().get(serviceId);
                    while(iterator.hasNext()) {
                        HierarchicalConfiguration idConfigNext = iterator.next();
                        String serviceIdNext = idConfigNext.getString("");
                        Job jobNext = vrp.getJobs().get(serviceIdNext);
                        if(!RouteStateHash.containsKey(job.getIndex())) {
                            RouteStateHash.put(job.getIndex(), stateManager.createStateId("routeState_" + job.getIndex()));
                            stateManager.addStateUpdater(new RouteStatusUpdater(stateManager, RouteStateHash));
                        }
                        if(!RouteStateHash.containsKey(jobNext.getIndex())) {
                            RouteStateHash.put(jobNext.getIndex(), stateManager.createStateId("routeState_" + jobNext.getIndex()));
                            stateManager.addStateUpdater(new RouteStatusUpdater(stateManager, RouteStateHash));
                        }
                        ArrayList<Integer> temp = new ArrayList<Integer>();
                        if(sameRouteHash.containsKey(job.getIndex())) {
                            temp = sameRouteHash.get(job.getIndex());
                        }
                        temp.add(jobNext.getIndex());
                        sameRouteHash.put(job.getIndex(), temp);
                    }
                    index++;
                }
            }
        }
        constraintManager.addConstraint(new InSameRoute(stateManager, RouteStateHash, sameRouteHash));
        
    }

    private void readOrder(XMLConfiguration config) {
        orderHash = new Hashtable<Integer, ArrayList<Integer>>();
        reverseOrderHash = new Hashtable<Integer, ArrayList<Integer>>();
        List<HierarchicalConfiguration> sameRoutesConfigs = config.configurationsAt("orderRelations");
        int index = 1;
        for (HierarchicalConfiguration sameRouteConfig : sameRoutesConfigs) {
            List<HierarchicalConfiguration> sameRoutesIdConfigs = sameRouteConfig.configurationsAt("serviceIds.serviceId");
            if (!sameRoutesIdConfigs.isEmpty()) {
                for (HierarchicalConfiguration idConfig : sameRoutesIdConfigs) {
                    ListIterator<HierarchicalConfiguration> iterator = sameRoutesIdConfigs.listIterator(index);
                    String serviceId = idConfig.getString("");
                    Job job = vrp.getJobs().get(serviceId);
                    while(iterator.hasNext()) {
                        HierarchicalConfiguration idConfigNext = iterator.next();
                        String serviceIdNext = idConfigNext.getString("");
                        Job jobNext = vrp.getJobs().get(serviceIdNext);
                        if(!RouteStateHash.containsKey(job.getIndex())) {
                            RouteStateHash.put(job.getIndex(), stateManager.createStateId("routeState_" + job.getIndex()));
                            stateManager.addStateUpdater(new RouteStatusUpdater(stateManager, RouteStateHash));
                        }
                        if(!RouteStateHash.containsKey(jobNext.getIndex())) {
                            RouteStateHash.put(jobNext.getIndex(), stateManager.createStateId("routeState_" + jobNext.getIndex()));
                            stateManager.addStateUpdater(new RouteStatusUpdater(stateManager, RouteStateHash));
                        }
                        if(!IndexStateHash.containsKey(job.getIndex())) {
                            IndexStateHash.put(job.getIndex(), stateManager.createStateId("indexState_" + job.getIndex()));
                            stateManager.addStateUpdater(new RouteStatusUpdater(stateManager, IndexStateHash));
                        }
                        if(!IndexStateHash.containsKey(jobNext.getIndex())) {
                            IndexStateHash.put(jobNext.getIndex(), stateManager.createStateId("indexState_" + jobNext.getIndex()));
                            stateManager.addStateUpdater(new RouteStatusUpdater(stateManager, IndexStateHash));
                        }
                        ArrayList<Integer> temp = new ArrayList<Integer>();
                        if (orderHash.containsKey(job.getIndex()))
                            temp = orderHash.get(job.getIndex());
                        temp.add(jobNext.getIndex());
                        orderHash.put(job.getIndex(), temp);
                        sameRouteHash.put(job.getIndex(), temp);
                        ArrayList<Integer> reverseTemp = new ArrayList<Integer>();
                        if (reverseOrderHash.containsKey(jobNext.getIndex()))
                            temp = reverseOrderHash.get(jobNext.getIndex());
                        reverseTemp.add(job.getIndex());
                        reverseOrderHash.put(jobNext.getIndex(), reverseTemp);
                    }
                    index++;
                }
            }
        }
        constraintManager.addConstraint(new InOrder(stateManager, RouteStateHash, IndexStateHash, orderHash, reverseOrderHash), ConstraintManager.Priority.CRITICAL);
    }
    
    private void readSequence(XMLConfiguration config) {
        sequenceHash = new Hashtable<Integer, Integer>();
        reverseSequenceHash = new Hashtable<Integer, Integer>();
        List<HierarchicalConfiguration> sameRoutesConfigs = config.configurationsAt("sequenceRelations");
        int index = 1;
        for (HierarchicalConfiguration sameRouteConfig : sameRoutesConfigs) {
            List<HierarchicalConfiguration> sameRoutesIdConfigs = sameRouteConfig.configurationsAt("serviceIds.serviceId");
            if (!sameRoutesIdConfigs.isEmpty()) {
                for (HierarchicalConfiguration idConfig : sameRoutesIdConfigs) {
                    ListIterator<HierarchicalConfiguration> iterator = sameRoutesIdConfigs.listIterator(index);
                    String serviceId = idConfig.getString("");
                    Job job = vrp.getJobs().get(serviceId);
                    if(iterator.hasNext()) {
                        HierarchicalConfiguration idConfigNext = iterator.next();
                        String serviceIdNext = idConfigNext.getString("");
                        Job jobNext = vrp.getJobs().get(serviceIdNext);
                        if(!RouteStateHash.containsKey(job.getIndex())) {
                            RouteStateHash.put(job.getIndex(), stateManager.createStateId("routeState_" + job.getIndex()));
                            stateManager.addStateUpdater(new RouteStatusUpdater(stateManager, RouteStateHash));
                        }
                        if(!RouteStateHash.containsKey(jobNext.getIndex())) {
                            RouteStateHash.put(jobNext.getIndex(), stateManager.createStateId("routeState_" + jobNext.getIndex()));
                            stateManager.addStateUpdater(new RouteStatusUpdater(stateManager, RouteStateHash));
                        }
                        ArrayList<Integer> temp = new ArrayList<Integer>();
                        if (sameRouteHash.containsKey(job.getIndex()))
                            temp = sameRouteHash.get(job.getIndex());
                        temp.add(jobNext.getIndex());
                        sequenceHash.put(job.getIndex(), jobNext.getIndex());
                        sameRouteHash.put(job.getIndex(), temp);
                        ArrayList<Integer> reverseTemp = new ArrayList<Integer>();
                        if (sameRouteHash.containsKey(jobNext.getIndex()))
                            temp = sameRouteHash.get(jobNext.getIndex());
                        reverseTemp.add(job.getIndex());
                        reverseSequenceHash.put(jobNext.getIndex(), job.getIndex());
                        sameRouteHash.put(jobNext.getIndex(), reverseTemp);
                    }
                    index++;
                }
            }
            constraintManager.addConstraint(new InDirectSequence(stateManager, RouteStateHash, sequenceHash, reverseSequenceHash), ConstraintManager.Priority.CRITICAL);
        }
    }
    
    private void readMinimalVehicleLapse(XMLConfiguration config) {
        List<HierarchicalConfiguration> minimalVehicleLapseConfigs = config.configurationsAt("minimalVehicleLapseRelations");
        int index = 1;
        for (HierarchicalConfiguration minimalVehicleLapseConfig : minimalVehicleLapseConfigs) {
            Hashtable<Integer, ArrayList<Integer>> minimalVehicleLapseHash = new Hashtable<Integer, ArrayList<Integer>>();
            List<HierarchicalConfiguration> minimalVehicleLapseIdConfigs = minimalVehicleLapseConfig.configurationsAt("serviceIds.serviceId");
            if (!minimalVehicleLapseIdConfigs.isEmpty()) {
                for (HierarchicalConfiguration idConfig : minimalVehicleLapseIdConfigs) {
                    ListIterator<HierarchicalConfiguration> iterator = minimalVehicleLapseIdConfigs.listIterator(index);
                    String serviceId = idConfig.getString("");
                    Job job = vrp.getJobs().get(serviceId);
                    while(iterator.hasNext()) {
                        HierarchicalConfiguration idConfigNext = iterator.next();
                        String serviceIdNext = idConfigNext.getString("");
                        Job jobNext = vrp.getJobs().get(serviceIdNext);
                        if(!RouteStateHash.containsKey(job.getIndex())) {
                            RouteStateHash.put(job.getIndex(), stateManager.createStateId("routeState_" + job.getIndex()));
                            stateManager.addStateUpdater(new RouteStatusUpdater(stateManager, RouteStateHash));
                        }
                        if(!RouteStateHash.containsKey(jobNext.getIndex())) {
                            RouteStateHash.put(jobNext.getIndex(), stateManager.createStateId("routeState_" + jobNext.getIndex()));
                            stateManager.addStateUpdater(new RouteStatusUpdater(stateManager, RouteStateHash));
                        }
                        ArrayList<Integer> temp = new ArrayList<Integer>();
                        if (minimalVehicleLapseHash.containsKey(job.getIndex()))
                            temp = minimalVehicleLapseHash.get(job.getIndex());
                        temp.add(jobNext.getIndex());
                        minimalVehicleLapseHash.put(job.getIndex(), temp);
                        ArrayList<Integer> reverseTemp = new ArrayList<Integer>();
                        if (minimalVehicleLapseHash.containsKey(jobNext.getIndex()))
                            reverseTemp = minimalVehicleLapseHash.get(jobNext.getIndex());
                        reverseTemp.add(job.getIndex());
                        minimalVehicleLapseHash.put(jobNext.getIndex(), reverseTemp);
                    }
                    index++;
                }
            }

            int lapse = (int) minimalVehicleLapseConfig.getDouble("lapse");
            constraintManager.addConstraint(new MinimalVehicleLapse(stateManager, RouteStateHash, minimalVehicleLapseHash, lapse));
        }
    }


}
