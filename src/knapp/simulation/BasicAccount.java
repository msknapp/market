package knapp.simulation;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static java.time.temporal.ChronoUnit.DAYS;

public class BasicAccount implements Account {
    private final long cents;
    private final Map<LocalDate, PurchaseInfo> stockShares;
    private final Map<LocalDate, PurchaseInfo> bondShares;
    private final double effectiveTaxRate;

    public static BasicAccount createAccount(int dollars, double effectiveTaxRate) {
        return new BasicAccount(100L * dollars, Collections.emptyMap(), Collections.emptyMap(), effectiveTaxRate);
    }

    private BasicAccount(long cents,Map<LocalDate, PurchaseInfo> stockShares,Map<LocalDate, PurchaseInfo> bondShares,
                         double effectiveTaxRate) {
        if (cents < 0) {
            throw new IllegalArgumentException("Can't have negative cents.");
        }
        if (effectiveTaxRate < 0.15) {
            throw new IllegalArgumentException("The effective tax rate cannot be less than 15%.");
        }
        for (PurchaseInfo purchaseInfo : stockShares.values()) {
            if (purchaseInfo.getAsset() != Asset.STOCK) {
                throw new IllegalArgumentException("The stock shares has bonds in it.");
            }
        }
        for (PurchaseInfo purchaseInfo : bondShares.values()) {
            if (purchaseInfo.getAsset() != Asset.BONDS) {
                throw new IllegalArgumentException("The bonds shares has stocks in it.");
            }
        }
        this.cents = cents;
        this.stockShares = Collections.unmodifiableMap(new HashMap<>(stockShares));
        this.bondShares = Collections.unmodifiableMap(new HashMap<>(bondShares));
        this.effectiveTaxRate = effectiveTaxRate;
    }

    public final Account executeOrder(final Order order, final CurrentPrices currentPrices, LocalDate presentDay) {
        Map<LocalDate, PurchaseInfo> assetShares = (order.getAsset() == Asset.STOCK) ? stockShares : bondShares;
        if (assetShares.get(presentDay) != null) {
            throw new IllegalArgumentException("A trade already exists for that asset on that date.");
        }
        double priceDollars = (order.getAsset() == Asset.STOCK) ? currentPrices.getStockPriceDollars() :
                currentPrices.getBondPriceDollars();
        Map newAssets = new HashMap(assetShares);
        if (order.isPurchase()) {
            double expenseDollars = priceDollars * order.getQuantity() + (getTradeFeeCents()/100);
            long expenseCents = Math.round(expenseDollars * 100);
            if (expenseCents > cents) {
                throw new IllegalArgumentException("Insufficient funds");
            }
            PurchaseInfo purchaseInfo = new PurchaseInfo(order.getQuantity(), priceDollars,presentDay,order.getAsset());

            newAssets.put(presentDay,purchaseInfo);
            long newCents = cents - expenseCents;
            if (order.getAsset() == Asset.STOCK) {
                return new BasicAccount(newCents, newAssets, bondShares, effectiveTaxRate);
            } else {
                return new BasicAccount(newCents, stockShares,newAssets, effectiveTaxRate);
            }
        } else {
            PurchaseInfo purchaseInfo = assetShares.get(order.getDateSharesWerePurchased());
            if (order.getQuantity() > purchaseInfo.getQuantity()) {
                throw new IllegalArgumentException("Selling more assets than we have.");
            }
            if (order.getQuantity() == purchaseInfo.getQuantity()) {
                newAssets.remove(purchaseInfo.getDateExchanged());
            } else {
                newAssets.put(purchaseInfo.getDateExchanged(),purchaseInfo.lessQuantity(order.getQuantity()));
            }
            boolean longTerm = DAYS.between(purchaseInfo.getDateExchanged(), presentDay) > 365;

            // trade fees are paid before capital gains taxes.
            double gainedDollars = priceDollars * order.getQuantity() - (getTradeFeeCents() / 100.0);

            double capitalGainDollars = gainedDollars -
                    (purchaseInfo.getQuantity() * purchaseInfo.getPriceDollars());
            double taxRate = (longTerm ? getLongTermTaxRate() : getShortTermTaxRate());
            // we always assume that we pay tax immediately.
            double tax = capitalGainDollars * taxRate;
            long gainCents = Math.round((gainedDollars - tax) * 100);
            long newCents = gainCents + cents;
            if (order.getAsset() == Asset.STOCK) {
                return new BasicAccount(newCents, newAssets, bondShares, effectiveTaxRate);
            } else {
                return new BasicAccount(newCents, stockShares,newAssets, effectiveTaxRate);
            }
        }
    }

    @Override
    public Account cashOut(final CurrentPrices currentPrices, LocalDate presentDay) {
        return new BasicAccount(netValueCents(currentPrices,presentDay),Collections.emptyMap(),
                Collections.emptyMap(),effectiveTaxRate);
    }

    @Override
    public Account addCash(long cents) {
        if (cents < 0) {
            throw new IllegalArgumentException("Can't subtract funds");
        }
        if (cents == 0) {
            return this;
        }
        return new BasicAccount(this.cents + cents,this.stockShares,this.bondShares,this.effectiveTaxRate);
    }

    @Override
    public long netValueCents(CurrentPrices currentPrices, LocalDate presentDay) {
        long totalCents = cents;
        for (PurchaseInfo purchaseInfo : stockShares.values()) {
            double valueDollars = currentPrices.getStockPriceDollars() * purchaseInfo.getQuantity();
            double valueDollarsAfterFee = valueDollars - (getTradeFeeCents()/100);
            double capitalGainsDollars = valueDollarsAfterFee - (purchaseInfo.getQuantity() * purchaseInfo.getPriceDollars());
            boolean longTerm = DAYS.between(purchaseInfo.getDateExchanged(),presentDay) >= 365;
            double taxRate = (longTerm) ? getLongTermTaxRate() : getShortTermTaxRate();
            double taxDollars = taxRate * capitalGainsDollars;
            totalCents += Math.round((valueDollarsAfterFee - taxDollars) * 100);
        }
        for (PurchaseInfo purchaseInfo : bondShares.values()) {
            double valueDollars = currentPrices.getBondPriceDollars() * purchaseInfo.getQuantity();
            double valueDollarsAfterFee = valueDollars - (getTradeFeeCents()/100);
            double capitalGainsDollars = valueDollarsAfterFee - (purchaseInfo.getQuantity() * purchaseInfo.getPriceDollars());
            boolean longTerm = DAYS.between(purchaseInfo.getDateExchanged(),presentDay) >= 365;
            double taxRate = (longTerm) ? getLongTermTaxRate() : getShortTermTaxRate();
            double taxDollars = taxRate * capitalGainsDollars;
            totalCents += Math.round((valueDollarsAfterFee - taxDollars) * 100);
        }
        return totalCents;
    }

    @Override
    public int getCurrentSharesStock() {
        int bonds = 0;
        for (PurchaseInfo purchaseInfo : stockShares.values()) {
            bonds += purchaseInfo.getQuantity();
        }
        return bonds;
    }

    @Override
    public int getCurrentSharesBonds() {
        int bonds = 0;
        for (PurchaseInfo purchaseInfo : bondShares.values()) {
            bonds += purchaseInfo.getQuantity();
        }
        return bonds;
    }

    @Override
    public Map<LocalDate, PurchaseInfo> getOwnedStockShares() {
        return stockShares;
    }

    @Override
    public Map<LocalDate, PurchaseInfo> getOwnedBondShares() {
        return bondShares;
    }

    @Override
    public long getCurrentCents() {
        return cents;
    }

    @Override
    public long getTradeFeeCents() {
        return 700;
    }

    @Override
    public double getShortTermTaxRate() {
        return effectiveTaxRate;
    }

    @Override
    public double getLongTermTaxRate() {
        return 0.15;
    }
}
