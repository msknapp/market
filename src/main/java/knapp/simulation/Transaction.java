package knapp.simulation;

import java.time.LocalDate;

public class Transaction {
    private final boolean purchase;
    private final LocalDate date;
    private final int quantity;
    private final USDollars price;

    public Transaction(LocalDate date, int quantity, USDollars price, boolean purchase) {
        if (date == null) {
            throw new IllegalArgumentException("Date can't be null");
        }
        if (quantity < 0){
            throw new IllegalArgumentException("Quantity can't be negative");
        }
        if (price == null || price.isDebt()) {
            throw new IllegalArgumentException("Price cannot be null or negative");
        }
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

    public USDollars getPrice() {
        return price;
    }
}
