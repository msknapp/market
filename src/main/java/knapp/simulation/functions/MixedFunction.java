package knapp.simulation.functions;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MixedFunction implements EvolvableFunction {

    private final Map<EvolvableFunction,Double> weightedFunctions;

    public MixedFunction(Map<EvolvableFunction,Double> weightedFunctions) {
        this.weightedFunctions = Collections.unmodifiableMap(new HashMap<>(weightedFunctions));
    }


    @Override
    public EvolvableFunction deviateRandomly(double deviation) {
        throw new UnsupportedOperationException("Sorry I don't do that.");
    }

    @Override
    public String describe() {
        return "ugh... It's too complicated.";
    }

    @Override
    public String describe(double value) {
        return "ugh... It's too complicated.";
    }

    @Override
    public Double apply(Double aDouble) {
        double est = 0;
        double cumulativeWeight = 0;
        for (EvolvableFunction f : weightedFunctions.keySet()) {
            double weight = weightedFunctions.get(f);
            double v = f.apply(aDouble);
            est += weight * v;
        }
        return est/cumulativeWeight;
    }
}
