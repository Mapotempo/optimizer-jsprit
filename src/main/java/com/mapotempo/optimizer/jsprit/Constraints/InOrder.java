package Constraints;

import java.util.ArrayList;
import java.util.Hashtable;

import com.graphhopper.jsprit.core.algorithm.state.StateId;
import com.graphhopper.jsprit.core.algorithm.state.StateManager;
import com.graphhopper.jsprit.core.problem.constraint.HardActivityConstraint;
import com.graphhopper.jsprit.core.problem.misc.JobInsertionContext;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;

public class InOrder implements HardActivityConstraint {

    StateManager stateManager;
    Hashtable<Integer, StateId> routeStateHash;
    Hashtable<Integer, StateId> indexStateHash;
    Hashtable<Integer, ArrayList<Integer>> orderHash;
    Hashtable<Integer, ArrayList<Integer>> reverseOrderHash;
    
    public InOrder(StateManager stateManager, Hashtable<Integer, StateId> routeStateHash, Hashtable<Integer, StateId> indexStateHash, Hashtable<Integer, ArrayList<Integer>> orderHash, Hashtable<Integer, ArrayList<Integer>> reverseOrderHash) {
        this.stateManager = stateManager;
        this.routeStateHash = routeStateHash;
        this.indexStateHash = indexStateHash;
        this.orderHash = orderHash;
        this.reverseOrderHash = reverseOrderHash;
    }
    @Override
    public ConstraintsStatus fulfilled(JobInsertionContext iFacts, TourActivity prevAct, TourActivity newAct,
            TourActivity nextAct, double prevActDepTime) {
        if(orderHash.containsKey(newAct.getIndex())) {
            for(Integer indexLinked : orderHash.get(newAct.getIndex())) {
                VehicleRoute routeLinked = stateManager.getProblemState(routeStateHash.get(indexLinked), VehicleRoute.class);
                if(routeLinked != null) {
                    if(routeLinked != iFacts.getRoute() || routeLinked == iFacts.getRoute() && prevAct.getIndex() == indexLinked) {
                        return ConstraintsStatus.NOT_FULFILLED_BREAK;
                    }
                }
            }
        }        
        if(reverseOrderHash.containsKey(newAct.getIndex())) {
            for(Integer indexLinked : reverseOrderHash.get(newAct.getIndex())) {
                VehicleRoute routeLinked = stateManager.getProblemState(routeStateHash.get(indexLinked), VehicleRoute.class);
                if(routeLinked != null) {
                    if(routeLinked != iFacts.getRoute()) {
                        return ConstraintsStatus.NOT_FULFILLED_BREAK;
                    }
                    if(iFacts.getRoute().getActivities().indexOf(nextAct) < stateManager.getProblemState(indexStateHash.get(indexLinked), Integer.class)) {
                        return ConstraintsStatus.NOT_FULFILLED;
                    }
                }
            }
        }
        return ConstraintsStatus.FULFILLED;
    }


}
