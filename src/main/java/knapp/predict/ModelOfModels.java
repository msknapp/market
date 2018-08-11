package knapp.predict;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ModelOfModels implements Model {

    private final List<Model> models;

    public ModelOfModels(List<Model> models) {
        if (models == null || models.isEmpty()) {
            throw new IllegalArgumentException("The input models must not be null");
        }
        for (Model model : models) {
            if (model.getWeight() < 1e-4) {
                throw new IllegalArgumentException("An input model has insufficient weight.");
            }
        }
        this.models = Collections.unmodifiableList(new ArrayList<>(models));
    }

    @Override
    public double estimateValue(MarketSlice marketSlice) {
        if (marketSlice == null) {
            throw new IllegalArgumentException("market slice is null");
        }
        double est = 0;
        double cumulativeWeight = 0;
        for (Model model : models) {
            cumulativeWeight += model.getWeight();
            est += model.estimateValue(marketSlice) * model.getWeight();
        }
        return est / cumulativeWeight;
    }

    @Override
    public double getWeight() {
        return 1;
    }

    @Override
    public double getTrustScore() {
        return 1;
    }

    public List<Model> getModels() {
        return models;
    }
}
