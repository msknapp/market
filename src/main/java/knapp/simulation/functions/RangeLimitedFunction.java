package knapp.simulation.functions;

public class RangeLimitedFunction implements EvolvableFunction {

    private final EvolvableFunction core;
    private final double min, max;

    public RangeLimitedFunction(EvolvableFunction core) {
        this.core = core;
        this.min = 0;
        this.max = 1;
    }

    public RangeLimitedFunction(EvolvableFunction core, double min, double max) {
        this.core = core;
        this.min = min;
        this.max = max;
    }

    public EvolvableFunction getCore() {
        return core;
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    @Override
    public EvolvableFunction deviateRandomly(double deviation) {
        return new RangeLimitedFunction(core.deviateRandomly(deviation), min, max);
    }

    @Override
    public String describe() {
        return core.describe();
    }

    @Override
    public String describe(double value) {
        return core.describe(value);
    }

    @Override
    public Double apply(Double aDouble) {
        double x = core.apply(aDouble);
        if (x < min) {
            return min;
        }
        if (x > max) {
            return max;
        }
        return x;
    }
}
