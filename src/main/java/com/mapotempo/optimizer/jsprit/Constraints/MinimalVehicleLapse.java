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
package com.mapotempo.optimizer.jsprit.Constraints;

import java.util.ArrayList;
import java.util.Hashtable;
import java.lang.*;

import com.graphhopper.jsprit.core.algorithm.state.StateId;
import com.graphhopper.jsprit.core.algorithm.state.StateManager;
import com.graphhopper.jsprit.core.problem.constraint.HardRouteConstraint;
import com.graphhopper.jsprit.core.problem.misc.JobInsertionContext;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;

public class MinimalVehicleLapse implements HardRouteConstraint {

    StateManager stateManager;

    Hashtable<Integer, StateId> routeStateHash;
    Hashtable<Integer, ArrayList<Integer>> lapseActivities;
    int lapse;

    public MinimalVehicleLapse(StateManager stateManager, final Hashtable<Integer, StateId> routeStateHash, final Hashtable<Integer, ArrayList<Integer>> lapseActivities, final int lapse) {
        this.stateManager = stateManager;
        this.routeStateHash = routeStateHash;
        this.lapseActivities = lapseActivities;
        this.lapse = lapse;
    }

    @Override
    public boolean fulfilled(JobInsertionContext iFacts) {
        if(lapseActivities.containsKey(iFacts.getJob().getIndex()) && lapseActivities.containsKey(iFacts.getJob().getIndex())) {
            ArrayList<Integer> minimalRouteLapse = lapseActivities.get(iFacts.getJob().getIndex());
            for(Integer toCompare : minimalRouteLapse) {
                StateId toCompareState = routeStateHash.get(toCompare);
                VehicleRoute routeCompareActivity = stateManager.getProblemState(toCompareState, VehicleRoute.class);
                if(routeCompareActivity!= null) {
                    if(Math.abs(iFacts.getNewVehicle().getIndex() - routeCompareActivity.getVehicle().getIndex()) < lapse) {
                        return false;
                    }
                }
            }
        }
        return true;
    }


}
