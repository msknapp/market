package knapp.table;

public class DoubleRange {

    private final double min;
    private final double max;

    public DoubleRange(double min, double max) {
        if (max < min) {
            throw new IllegalArgumentException("max cannot be less than min");
        }
        this.min = min;
        this.max = max;
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }
}
