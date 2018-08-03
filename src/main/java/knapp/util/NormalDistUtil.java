package knapp.util;

import org.apache.commons.math.MathException;
import org.apache.commons.math.distribution.NormalDistribution;
import org.apache.commons.math.distribution.NormalDistributionImpl;

public class NormalDistUtil {

    public static double calculatePercentile(double actual, double mean, double standardDeviation) {
        try {

            NormalDistribution normalDistribution = new NormalDistributionImpl(mean, standardDeviation);
            return normalDistribution.cumulativeProbability(actual);
        } catch (MathException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
