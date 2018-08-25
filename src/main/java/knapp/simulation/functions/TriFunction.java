package knapp.simulation.functions;

import java.util.Random;

public class TriFunction implements Evolvable {
    private static final Random random = new Random();
    private double x, y, z, i;

    public static TriFunction initialTriFunction() {
        return new TriFunction(1,-1,1, 0);
//        return new TriFunction(1,1,1);
//        return new TriFunction(.718,1.173,1.603);
    }

    public TriFunction(double x, double y, double z, double i) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.i = i;
    }

    public double apply(double a, double b, double c) {
        return a*x + b*y + c*z + i;
    }

    @Override
    public Evolvable deviateRandomly(double deviation) {
        return new TriFunction(
                x + (random.nextDouble()-0.5) * deviation,
                y + (random.nextDouble()-0.5) * deviation,
                z + (random.nextDouble()-0.5) * deviation,
                i + (random.nextDouble()-0.5) * deviation);
    }

    @Override
    public String describe() {
        return String.format("percent stock = sigma * %.3f + risePct * %.3f + riseEst * %.3f + %.3f",x,y,z, i);
    }
}
