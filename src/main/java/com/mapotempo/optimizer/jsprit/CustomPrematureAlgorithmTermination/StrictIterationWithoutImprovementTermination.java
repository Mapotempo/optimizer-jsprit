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
package com.mapotempo.optimizer.jsprit.CustomPrematureAlgorithmTermination;

import com.graphhopper.jsprit.core.algorithm.SearchStrategy;
import com.graphhopper.jsprit.core.algorithm.termination.PrematureAlgorithmTermination;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StrictIterationWithoutImprovementTermination implements PrematureAlgorithmTermination {

    private static Logger log = LoggerFactory.getLogger(StrictIterationWithoutImprovementTermination.class);

    private int noIterationWithoutImprovement;

    private int iterationsWithoutImprovement = 0;
    
    private SearchStrategy.DiscoveredSolution bestSolution = null;

    /**
     * Constructs termination.
     *
     * @param noIterationsWithoutImprovement previous iterations without improvement of the best solution
     */
    public StrictIterationWithoutImprovementTermination(int noIterationsWithoutImprovement) {
        this.noIterationWithoutImprovement = noIterationsWithoutImprovement;
        log.debug("initialise " + this);
    }

    @Override
    public String toString() {
        return "[name=IterationWithoutImprovementBreaker][iterationsWithoutImprovement=" + noIterationWithoutImprovement + "]";
    }

    @Override
    public boolean isPrematureBreak(SearchStrategy.DiscoveredSolution discoveredSolution) {
        if(bestSolution == null || bestSolution.getSolution().getCost() < discoveredSolution.getSolution().getCost()){
            bestSolution = discoveredSolution;
            iterationsWithoutImprovement = 0;
        }
        else iterationsWithoutImprovement++;
        return (iterationsWithoutImprovement > noIterationWithoutImprovement);
    }


}
