package knapp.simulation;

import java.time.LocalDate;

public class AccountSnapshot {
    private final LocalDate date;
    private final long cents;
    private final int sharesOfStockMarket;
    private final int sharesOfBondMarket;

    public AccountSnapshot(LocalDate date, int sharesOfStockMarket, int sharesOfBondMarket, long cents) {
        this.date = date;
        this.sharesOfBondMarket = sharesOfBondMarket;
        this.sharesOfStockMarket = sharesOfStockMarket;
        this.cents = cents;
    }

    public LocalDate getDate() {
        return date;
    }

    public long getCents() {
        return cents;
    }

    public int getSharesOfStockMarket() {
        return sharesOfStockMarket;
    }

    public int getSharesOfBondMarket() {
        return sharesOfBondMarket;
    }
}