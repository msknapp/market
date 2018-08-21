package knapp.table.wraps;

import knapp.table.Table;
import knapp.table.UnevenTable;
import knapp.table.values.GetMethod;
import knapp.table.values.GetterFor;
import knapp.table.values.TableColumnView;
import knapp.table.values.TableValueGetter;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class UntilTable extends AbstractWrappingTable {
    private LocalDate maxDateExclusive;

    private Map<Integer,TableColumnView> views = new HashMap<>();

    public UntilTable(Table core, LocalDate max) {
        super(core);
        this.maxDateExclusive = max;
    }

    @Override
    public double getValue(LocalDate date, String column, GetMethod getMethod) {
        return getValue(date, getColumn(column), getMethod);
    }

    @Override
    public double getValue(LocalDate date, int column, GetMethod getMethod) {
        TableValueGetter tableValueGetter = GetterFor.getterFor(getMethod);
        TableColumnView view = getTableColumnView(column);
        return tableValueGetter.getValue(date,view);
    }

    @Override
    public double[] getExactValues(LocalDate date) {
        if (date.equals(maxDateExclusive) || date.isAfter(maxDateExclusive)) {
            throw new IllegalArgumentException("DNE");
        }
        return core.getExactValues(date);
    }

    @Override
    public TableColumnView getTableColumnView(int column) {
        if (!views.containsKey(column)) {
            views.put(column,new BeforeTableColumnView(core.getTableColumnView(column), maxDateExclusive));
        }
        return views.get(column);
    }

    public LocalDate getLastDateOf(int column) {
        if (core instanceof UnevenTable) {
            // this is a faster optimization
            return core.getTableColumnView(column).getDateBefore(maxDateExclusive);
        }
        return getLastDateOf(getColumn(column));
    }

    public LocalDate getFirstDateOf(int column) {
        return core.getTableColumnView(column).getFirstDate();
    }

    @Override
    public double getValue(LocalDate date, int column, TableValueGetter getter) {
        return getter.getValue(date,getTableColumnView(column));
    }
}
