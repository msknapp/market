package knapp.simulation;

import knapp.table.Frequency;

import java.time.LocalDate;
import java.util.*;

public class SimulationResults {
    private final USDollars finalDollars;
    private final List<Transaction> transactions;
    private final Account account;
    private final double averageROI;
    private final Map<LocalDate,Stance> worthOverTime;
    private final Frequency tradeFrequency;

    public SimulationResults(USDollars finalDollars, Account account, List<Transaction> transactions, double averageROI,
                             Map<LocalDate,Stance> worthOverTime, Frequency tradeFrequency) {
        if (finalDollars == null) {
            throw new IllegalArgumentException("Final dollars is null");
        }
        if (account == null) {
            throw new IllegalArgumentException("Account is null");
        }
        if (transactions == null) {
            throw new IllegalArgumentException("transactions is null.");
        }
        this.finalDollars = finalDollars;
        this.account = account;
        this.transactions = Collections.unmodifiableList(new ArrayList<>(transactions));
        this.averageROI = averageROI;
        this.worthOverTime = Collections.unmodifiableMap(new HashMap<>(worthOverTime));
        this.tradeFrequency = tradeFrequency;
    }

    public Frequency getTradeFrequency() {
        return tradeFrequency;
    }

    public USDollars getFinalDollars() {
        return finalDollars;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public Account getAccount() {
        return account;
    }

    public double getAverageROI() {
        return averageROI;
    }

    public Map<LocalDate, Stance> getWorthOverTime() {
        return worthOverTime;
    }
}
