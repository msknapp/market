package knapp.simulation;

import java.time.LocalDate;

public class Order {
    private final int quantity;
    private final LocalDate dateSharesWerePurchased;
    private final Asset asset;

    Order(int quantity, Asset asset, LocalDate datePurchased) {
        if (quantity < 1) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
        if (asset == Asset.CASH) {
            throw new IllegalArgumentException("You can't purchase/sell cash.");
        }
        this.quantity = quantity;
        this.asset = asset;
        this.dateSharesWerePurchased = datePurchased;
    }

    public static Order SellStock(int quantity, LocalDate purchased) {
        return new Order(quantity,Asset.STOCK,purchased);
    }

    public static Order SellBonds(int quantity, LocalDate purchased) {
        return new Order(quantity,Asset.BONDS,purchased);
    }

    public static Order SellAsset(int quantity, LocalDate purchased, Asset asset) {
        if (asset == Asset.BONDS){
            return SellBonds(quantity, purchased);
        } else if (asset == Asset.STOCK) {
            return SellStock(quantity, purchased);
        }
        throw new IllegalArgumentException("You can only sell bonds or stock.");
    }

    public static Order BuyStock(int quantity) {
        return new Order(quantity,Asset.STOCK,null);
    }

    public static Order BuyBonds(int quantity) {
        return new Order(quantity,Asset.BONDS,null);
    }

    public static Order BuyAsset(int sharesToBuy, Asset asset) {
        if (asset == Asset.STOCK) {
            return BuyStock(sharesToBuy);
        } else if (asset == Asset.BONDS) {
            return BuyBonds(sharesToBuy);
        }
        throw new IllegalArgumentException("You can only buy stocks or bonds.");
    }

    public int getQuantity() {
        return quantity;
    }

    public LocalDate getDateSharesWerePurchased() {
        return dateSharesWerePurchased;
    }

    public boolean isPurchase() {
        return dateSharesWerePurchased == null;
    }

    public Asset getAsset() {
        return asset;
    }
}
