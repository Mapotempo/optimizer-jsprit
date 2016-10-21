package com.mapotempo.optimizer.jsprit.Status;

import java.util.Hashtable;

import com.graphhopper.jsprit.core.algorithm.state.StateId;
import com.graphhopper.jsprit.core.algorithm.state.StateManager;
import com.graphhopper.jsprit.core.algorithm.state.StateUpdater;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.ActivityVisitor;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;

public class IndexStatusUpdater implements StateUpdater, ActivityVisitor {

    StateManager stateManager;
    
    Hashtable<Integer, StateId> indexStateHash;
    
    private VehicleRoute route;
    
    private int index;

    public IndexStatusUpdater(StateManager stateManager, Hashtable<Integer, StateId> indexStateHash) {
        this.stateManager = stateManager;
        this.indexStateHash = indexStateHash;
    }

    @Override
    public void begin(VehicleRoute route) {
        this.route = route;
        this.index = 0;
    }

    @Override
    public void visit(TourActivity activity) {
        ++index;
        if (indexStateHash.containsKey(activity.getIndex())) {
            stateManager.putProblemState(indexStateHash.get(activity.getIndex()), Integer.class, index);
        }
    }

    @Override
    public void finish() {
    }


}
