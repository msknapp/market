package knapp.predict;

public interface Model {
    double estimateValue(MarketSlice marketSlice);

    default double getWeight() {
        return 1.0;
    }

    double getTrustScore();

}
