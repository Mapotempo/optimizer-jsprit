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

import com.graphhopper.jsprit.core.problem.misc.JobInsertionContext;
import com.graphhopper.jsprit.core.problem.solution.route.state.RouteAndActivityStateGetter;
import com.graphhopper.jsprit.core.problem.constraint.HardRouteConstraint;

public class MaximumVehicleNumberConstraint implements HardRouteConstraint {

    private RouteAndActivityStateGetter stateManager;
    
    private int maximumVehicles;

    public MaximumVehicleNumberConstraint(RouteAndActivityStateGetter stateManager, int maximumVehicles) {
        super();
        this.stateManager = stateManager;
        this.maximumVehicles = maximumVehicles;
    }

    @Override
    public boolean fulfilled(JobInsertionContext insertionContext) {
        if(insertionContext.getNewVehicle().getIndex() > maximumVehicles)
            return false;
        return true;
    }

}
