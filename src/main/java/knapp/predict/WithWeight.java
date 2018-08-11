package knapp.predict;

public class WithWeight implements Model {
    private final Model core;
    private final double weight;

    public WithWeight(Model model, double weight) {
        this.core = model;
        this.weight = weight;
    }

    @Override
    public double estimateValue(MarketSlice marketSlice) {
        return this.core.estimateValue(marketSlice);
    }

    @Override
    public double getTrustScore() {
        return this.core.getTrustScore();
    }

    public double getWeight() {
        return weight;
    }
}
