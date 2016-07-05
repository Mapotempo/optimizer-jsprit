package Constraints;

import java.util.Hashtable;

import com.graphhopper.jsprit.core.algorithm.state.StateId;
import com.graphhopper.jsprit.core.algorithm.state.StateManager;
import com.graphhopper.jsprit.core.problem.constraint.HardActivityConstraint;
import com.graphhopper.jsprit.core.problem.misc.JobInsertionContext;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;

public class InDirectSequence implements HardActivityConstraint {

    StateManager stateManager;

    Hashtable<Integer, Integer> directSequenceHash;
    Hashtable<Integer, Integer> reverseDirectSequenceHash;
    Hashtable<Integer, StateId> routeStateHash;

    public InDirectSequence(StateManager stateManager, Hashtable<Integer, StateId> routeStateHash, Hashtable<Integer, Integer> directSequenceHash, Hashtable<Integer, Integer> reverseDirectSequenceHash) {
        this.stateManager = stateManager;
        this.directSequenceHash = directSequenceHash;
        this.reverseDirectSequenceHash = reverseDirectSequenceHash;
        this.routeStateHash = routeStateHash;
    }

    @Override
    public ConstraintsStatus fulfilled(JobInsertionContext iFacts, TourActivity prevAct, TourActivity newAct,
            TourActivity nextAct, double prevActDepTime) {
        if(directSequenceHash.containsKey(newAct.getIndex())) {
            VehicleRoute linkedActRoute = stateManager.getProblemState(routeStateHash.get(directSequenceHash.get(newAct.getIndex())), VehicleRoute.class);
            if(linkedActRoute == null)
                return ConstraintsStatus.FULFILLED;
            if(linkedActRoute != iFacts.getRoute())
                return ConstraintsStatus.NOT_FULFILLED_BREAK;
            if(nextAct.getIndex() != directSequenceHash.get(newAct.getIndex())){
                return ConstraintsStatus.NOT_FULFILLED;
            }
        }
        if(reverseDirectSequenceHash.containsKey(newAct.getIndex())){
            VehicleRoute linkedActRoute = stateManager.getProblemState(routeStateHash.get(reverseDirectSequenceHash.get(newAct.getIndex())), VehicleRoute.class);
            if(linkedActRoute == null)
                return ConstraintsStatus.FULFILLED;
            if(linkedActRoute != iFacts.getRoute())
                return ConstraintsStatus.NOT_FULFILLED_BREAK;
            if(prevAct.getIndex() != reverseDirectSequenceHash.get(newAct.getIndex())){
                return ConstraintsStatus.NOT_FULFILLED;
            }
        }
        if(directSequenceHash.containsKey(prevAct.getIndex()) && directSequenceHash.get(prevAct.getIndex()) == nextAct.getIndex()) {
            return ConstraintsStatus.NOT_FULFILLED;
        }
        return ConstraintsStatus.FULFILLED;
    }


}
