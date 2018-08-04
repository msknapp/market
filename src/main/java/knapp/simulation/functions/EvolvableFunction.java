package knapp.simulation.functions;


import java.util.function.Function;

public interface EvolvableFunction extends Function<Double,Double> {

    EvolvableFunction deviateRandomly(double deviation);

    String describe();
    String describe(double value);
}
