package com.mapotempo.optimizer.jsprit.Status;

import java.util.Hashtable;

import com.graphhopper.jsprit.core.algorithm.state.StateId;
import com.graphhopper.jsprit.core.algorithm.state.StateManager;
import com.graphhopper.jsprit.core.algorithm.state.StateUpdater;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.ActivityVisitor;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;

public class RouteStatusUpdater implements StateUpdater, ActivityVisitor {

    StateManager stateManager;

    Hashtable<Integer, StateId> routeStateHash;

    private VehicleRoute route;

    public RouteStatusUpdater(StateManager stateManager, Hashtable<Integer, StateId> routeStateHash) {
        this.stateManager = stateManager;
        this.routeStateHash = routeStateHash;
    }

    @Override
    public void begin(VehicleRoute route) {
        this.route = route;
    }

    @Override
    public void visit(TourActivity activity) {
        if (routeStateHash.containsKey(activity.getIndex())) {
            stateManager.putProblemState(routeStateHash.get(activity.getIndex()), VehicleRoute.class, route);
        }
    }

    @Override
    public void finish() {
    }


}
