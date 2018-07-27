package knapp.simulation;

import java.time.LocalDate;

public class Transaction {
    private final boolean purchase;
    private final LocalDate date;
    private final int quantity;
    private final double price;

    public Transaction(LocalDate date, int quantity, double price, boolean purchase) {
        this.purchase = purchase;
        this.price = price;
        this.quantity = quantity;
        this.date = date;
    }

    public boolean isPurchase() {
        return purchase;
    }

    public LocalDate getDate() {
        return date;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getPrice() {
        return price;
    }
}
