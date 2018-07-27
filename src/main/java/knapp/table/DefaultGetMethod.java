package knapp.table;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiFunction;

public class DefaultGetMethod implements BiFunction<Table, Integer, TableImpl.GetMethod> {
    private Set<String> extrapolated = new HashSet<>(Arrays.asList("indpro", "m1sl", "cpiaucsl"));
    private Set<String> interpolated = new HashSet<>(Arrays.asList("market", "nasdaq", "market price"));

    @Override
    public TableImpl.GetMethod apply(Table table, Integer columnNumber) {
        String cname = table.getColumn(columnNumber);
        for (String ex : extrapolated) {
            if (cname.toLowerCase().endsWith(ex.toLowerCase())) {
                return TableImpl.GetMethod.EXTRAPOLATE;
            }
        }
        for (String ex : interpolated) {
            if (cname.toLowerCase().endsWith(ex.toLowerCase())) {
                return TableImpl.GetMethod.INTERPOLATE;
            }
        }
        return TableImpl.GetMethod.LAST_KNOWN_VALUE;
    }
}
