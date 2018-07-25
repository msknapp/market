package knapp.simulation;

import java.time.LocalDate;

public class PurchaseInfo {
    private final int quantity;
    private final double priceDollars;
    private final LocalDate dateExchanged;
    private final Asset asset;

    public PurchaseInfo(int quantity, double price, LocalDate dateExchanged, Asset asset) {
        if (quantity < 1) {
            throw new IllegalArgumentException("Quantity must be positive.");
        }
        if (price < 0) {
            throw new IllegalArgumentException("Price must be positive");
        }
        if (asset == null) {
            throw new IllegalArgumentException("Asset must not be null");
        }
        if (dateExchanged == null) {
            throw new IllegalArgumentException("date exchanged must not be null");
        }
        this.quantity = quantity;
        this.priceDollars = price;
        this.dateExchanged = dateExchanged;
        this.asset = asset;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getPriceDollars() {
        return priceDollars;
    }

    public LocalDate getDateExchanged() {
        return dateExchanged;
    }

    public Asset getAsset() {
        return asset;
    }

    public PurchaseInfo lessQuantity(int quantity) {
        if (quantity > this.quantity) {
            throw new IllegalArgumentException("Removing more quantity than we have.");
        }
        return new PurchaseInfo(this.quantity - quantity, priceDollars, dateExchanged, asset);
    }

    public Order toSellAllOrder() {
        return new Order(this.getQuantity(),this.getAsset(),this.getDateExchanged());
    }
}
