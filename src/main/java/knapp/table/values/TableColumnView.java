package knapp.table.values;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

public interface TableColumnView {
    List<LocalDate> getAllDates();
    double getExactValue(LocalDate date);

    LocalDate getLastDate();
    LocalDate getFirstDate();

    LocalDate getDateBefore(LocalDate date);
    LocalDate getDateOnOrBefore(LocalDate date);
    LocalDate getDateAfter(LocalDate date);
    LocalDate getDateOnOrAfter(LocalDate date);

    boolean containsDate(LocalDate date);

}
