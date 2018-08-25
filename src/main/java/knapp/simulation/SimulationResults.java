package knapp.simulation;

import knapp.table.Frequency;

import java.time.LocalDate;
import java.util.*;

public class SimulationResults {
    private final USDollars finalDollars;
    private final Account account;
    private final double averageROI;
    private final Map<LocalDate,HistoricRecord> history;
    private final Frequency tradeFrequency;

    public SimulationResults(USDollars finalDollars, Account account, double averageROI,
                             Map<LocalDate,HistoricRecord> history, Frequency tradeFrequency) {
        if (finalDollars == null) {
            throw new IllegalArgumentException("Final dollars is null");
        }
        if (account == null) {
            throw new IllegalArgumentException("Account is null");
        }
        this.finalDollars = finalDollars;
        this.account = account;
        this.averageROI = averageROI;
        this.history = Collections.unmodifiableMap(new HashMap<>(history));
        this.tradeFrequency = tradeFrequency;
    }

    public Frequency getTradeFrequency() {
        return tradeFrequency;
    }

    public USDollars getFinalDollars() {
        return finalDollars;
    }

    public int getTransactionCount() {
        int i = 0;
        for (HistoricRecord historicRecord : history.values()) {
            i += historicRecord.getTransactions().size();
        }
        return i;
    }

    public Account getAccount() {
        return account;
    }

    public double getAverageROI() {
        return averageROI;
    }

    public Map<LocalDate, HistoricRecord> getHistory() {
        return history;
    }
}
