package knapp.predict;

public class ParameterInfo {
    private final String name;
    private final double standardError;
    private final double value;

    public ParameterInfo(String name, double standardError, double value) {
        this.name = name;
        this.standardError = standardError;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public double getStandardError() {
        return standardError;
    }

    public double getValue() {
        return value;
    }
}
