package knapp.predict;

import java.util.List;
import java.util.stream.Collectors;

public interface NormalModel extends Model {
    default List<String> getParameterNames() {
        return getParameters().stream().map(p -> p.getName()).collect(Collectors.toList());
    }

    List<ParameterInfo> getParameters();
    double getStandardDeviation();
    double getRsquared();
}