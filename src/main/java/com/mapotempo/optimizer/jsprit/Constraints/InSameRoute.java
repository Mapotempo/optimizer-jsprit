package Constraints;

import java.util.ArrayList;
import java.util.Hashtable;

import com.graphhopper.jsprit.core.algorithm.state.StateId;
import com.graphhopper.jsprit.core.algorithm.state.StateManager;
import com.graphhopper.jsprit.core.problem.constraint.HardRouteConstraint;
import com.graphhopper.jsprit.core.problem.misc.JobInsertionContext;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;

public class InSameRoute implements HardRouteConstraint {

    StateManager stateManager;

    Hashtable<Integer, StateId> routeStateHash;
    Hashtable<Integer, ArrayList<Integer>> sameRouteHash; 

    public InSameRoute(StateManager stateManager, final Hashtable<Integer, StateId> routeStateHash, final Hashtable<Integer, ArrayList<Integer>> sameRouteHash) {
        this.stateManager = stateManager;
        this.routeStateHash = routeStateHash;
        this.sameRouteHash = sameRouteHash;
    }

    @Override
    public boolean fulfilled(JobInsertionContext iFacts) {
        if(sameRouteHash.containsKey(iFacts.getJob().getIndex()) && sameRouteHash.containsKey(iFacts.getJob().getIndex())) {
            ArrayList<Integer> sameRouteJobs = sameRouteHash.get(iFacts.getJob().getIndex());
            for(Integer toCompare : sameRouteJobs) {
                StateId toCompareState = routeStateHash.get(toCompare);
                VehicleRoute routeCompareActivity = stateManager.getProblemState(toCompareState, VehicleRoute.class);
                if(routeCompareActivity!= null) {
                    if(!iFacts.getRoute().equals(routeCompareActivity)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }


}
