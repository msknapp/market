package knapp.simulation;

import java.time.LocalDate;

public class PurchaseInfo {
    // the initial quantity is needed to determine the cost basis.
    private final int initialQuantity;

    // the current quantity is the amount held right now.
    private final int currentQuantity;
    private final USDollars priceDollars;
    private final LocalDate dateExchanged;
    private final Asset asset;

    public PurchaseInfo(int initialQuantity, int currentQuantity, USDollars price, LocalDate dateExchanged, Asset asset) {
        if (initialQuantity < 1) {
            throw new IllegalArgumentException("Quantity must be positive.");
        }
        if (currentQuantity < 1) {
            throw new IllegalArgumentException("Quantity must be positive.");
        }
        if (initialQuantity < currentQuantity) {
            throw new IllegalArgumentException("Initial quantity must not be less than current.");
        }
        if (price == null) {
            throw new IllegalArgumentException("Price must not be null");
        }
        if (asset == null) {
            throw new IllegalArgumentException("Asset must not be null");
        }
        if (asset == Asset.CASH) {
            throw new IllegalArgumentException("Can't have cash as a purchase.");
        }
        if (dateExchanged == null) {
            throw new IllegalArgumentException("date exchanged must not be null");
        }
        this.currentQuantity = currentQuantity;
        this.initialQuantity = initialQuantity;
        this.priceDollars = price;
        this.dateExchanged = dateExchanged;
        this.asset = asset;
    }

    public int getInitialQuantity() {
        return initialQuantity;
    }

    public int getCurrentQuantity() {
        return currentQuantity;
    }

    public USDollars getPriceDollars() {
        return priceDollars;
    }

    public LocalDate getDateExchanged() {
        return dateExchanged;
    }

    public Asset getAsset() {
        return asset;
    }

    public PurchaseInfo lessQuantity(int quantity) {
        if (quantity > this.currentQuantity) {
            throw new IllegalArgumentException("Removing more quantity than we have.");
        }

        return new PurchaseInfo(this.initialQuantity, this.currentQuantity - quantity, priceDollars, dateExchanged, asset);
    }

    public Order toSellAllOrder() {
        return new Order(this.getCurrentQuantity(),this.getAsset(),this.getDateExchanged());
    }

    public USDollars getCostBasis(USDollars tradeFee) {
        return getCostBasis(tradeFee, getCurrentQuantity());
    }

    public USDollars getCostBasis(USDollars tradeFee, int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Negative quantity");
        }
        if (quantity > this.getCurrentQuantity()) {
            throw new IllegalArgumentException("Requested quantity is more than we have.");
        }

        USDollars fractionalFee = quantity == getInitialQuantity() ? tradeFee :
                tradeFee.times(quantity).dividedBy(getInitialQuantity());
        return priceDollars.times(quantity).plus(fractionalFee);
    }
}
