package knapp.table;

import java.time.LocalDate;

public class RealDeriver implements ValueDeriver {
    private final Table cpi;
    private final LocalDate baseDate;
    private final String coreColumnName;
    private final int coreColumnNumber;
    private final double baseCpi;

    public RealDeriver(Table cpi, LocalDate baseDate, String coreColumnName, int coreColumnNumber) {
        this.cpi = cpi;
        this.baseDate = baseDate;
        this.coreColumnName = coreColumnName;
        this.baseCpi = cpi.getValue(baseDate,0,TableImpl.GetMethod.EXACT);
        this.coreColumnNumber = coreColumnNumber;
    }

    @Override
    public String getColumnName() {
        return "Real "+coreColumnName;
    }

    @Override
    public double deriveValue(Table core, LocalDate date, TableImpl.GetMethod getMethod) {
        double cpiThatDay = cpi.getValue(date,0,TableImpl.GetMethod.INTERPOLATE);
        double coreValue = core.getValue(date,coreColumnNumber,getMethod);

        return coreValue * baseCpi / cpiThatDay;
    }
}
