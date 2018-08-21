package knapp.predict;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MarketSlice {
    private final Map<String,Double> values;

    public MarketSlice(Map<String,Double> values) {
        this.values = Collections.unmodifiableMap(new HashMap<>(values));
    }

    public Map<String, Double> getValues() {
        return values;
    }

    public double getValue(String columnName) {
        if (!values.containsKey(columnName)) {
            throw new IllegalArgumentException("Requested a column name this doesn't have.");
        }
        return this.values.get(columnName);
    }

    public double[] getValues(List<String> inputs) {
        double[] x = new double[inputs.size()];
        int i = 0;
        for (String in : inputs) {
            x[i++] = getValue(in);
        }
        return x;
    }

    public boolean contains(String name) {
        return values.containsKey(name);
    }
}
