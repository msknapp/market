package knapp.simulation;

import java.time.LocalDate;

public class AccountSnapshot {
    private final LocalDate date;
    private final USDollars value;
    private final int sharesOfStockMarket;
    private final int sharesOfBondMarket;

    public AccountSnapshot(LocalDate date, int sharesOfStockMarket, int sharesOfBondMarket, USDollars value) {
        this.date = date;
        this.sharesOfBondMarket = sharesOfBondMarket;
        this.sharesOfStockMarket = sharesOfStockMarket;
        this.value = value;
    }

    public LocalDate getDate() {
        return date;
    }

    public USDollars getValue() {
        return value;
    }

    public int getSharesOfStockMarket() {
        return sharesOfStockMarket;
    }

    public int getSharesOfBondMarket() {
        return sharesOfBondMarket;
    }
}