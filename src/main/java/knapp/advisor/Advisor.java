package knapp.advisor;

import knapp.table.Table;

import java.util.List;
import java.util.Map;

public interface Advisor {
    void initialize();
    Advice getAdvice(List<String> inputs);
    Map<String,Table> getAllInputs();
    Table getAllInputsTable();
}
