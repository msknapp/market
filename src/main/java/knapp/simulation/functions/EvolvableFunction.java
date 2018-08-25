package knapp.simulation.functions;

import java.util.function.Function;

public interface EvolvableFunction extends Function<Double,Double>, Evolvable {

    EvolvableFunction deviateRandomly(double deviation);

    String describe(double value);
}
