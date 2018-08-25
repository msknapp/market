package knapp.simulation;

import knapp.simulation.strategy.MarketThoughts;

import java.time.LocalDate;
import java.util.List;

public class HistoricRecord {
    private final LocalDate date;
    private final List<Transaction> transactions;
    private final MarketThoughts marketThoughts;
    private final Stance stance;

    public HistoricRecord(LocalDate date, List<Transaction> transactions, MarketThoughts marketThoughts, Stance stance) {
        this.date = date;
        this.transactions = transactions;
        this.marketThoughts = marketThoughts;
        this.stance = stance;
    }

    public LocalDate getDate() {
        return date;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public MarketThoughts getMarketThoughts() {
        return marketThoughts;
    }

    public Stance getStance() {
        return stance;
    }
}
