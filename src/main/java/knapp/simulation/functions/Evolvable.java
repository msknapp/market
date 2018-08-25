package knapp.simulation.functions;

public interface Evolvable {

    Evolvable deviateRandomly(double deviation);
    String describe();
}
