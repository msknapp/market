package knapp.table;

import knapp.table.values.GetMethod;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiFunction;

public class DefaultGetMethod implements BiFunction<Table, Integer, GetMethod> {
    private Set<String> extrapolated = new HashSet<>(Arrays.asList("indpro", "m1sl", "cpiaucsl"));
    private Set<String> interpolated = new HashSet<>(Arrays.asList("market", "nasdaq", "market price"));

    @Override
    public GetMethod apply(Table table, Integer columnNumber) {
        String cname = table.getColumn(columnNumber);
        for (String ex : extrapolated) {
            if (cname.toLowerCase().endsWith(ex.toLowerCase())) {
                return GetMethod.EXTRAPOLATE;
            }
        }
        for (String ex : interpolated) {
            if (cname.toLowerCase().endsWith(ex.toLowerCase())) {
                return GetMethod.INTERPOLATE;
            }
        }
        return GetMethod.LAST_KNOWN_VALUE;
    }
}
