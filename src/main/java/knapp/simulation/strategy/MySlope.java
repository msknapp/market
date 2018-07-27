package knapp.simulation.strategy;

import java.util.Random;
import java.util.function.Function;

public class MySlope implements Function<Double, Double> {
    private static Random random = new Random();
    public double intercept = 0.55;
    public double slope = 0.15;


    @Override
    public Double apply(Double aDouble) {
        // a positive sigmas means the estimate is higher than the actual value,
        // so the market is undervalued.
        // generally your slope should be positive, so
        // as the estimate is increasingly higher than the actual,
        // the sigma increases,
        // and your percent stock increases.
        // the input is typically between +/- 3.
        // so it's smart to craft it so +3 returns 100.
        return intercept + slope * aDouble;
    }

    public static MySlope randomSlope() {
        MySlope mySlope = new MySlope();
        mySlope.intercept = random.nextDouble()*0.4 + 0.55; // between 0.55 and 0.95
        mySlope.slope = random.nextDouble()*0.2 + 0.3; // between 0.3 and 0.5
        return mySlope;
    }
}
