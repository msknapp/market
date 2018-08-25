package knapp.simulation;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static java.time.temporal.ChronoUnit.DAYS;

public class BasicAccount implements Account {
    private final USDollars cash;
    private final Map<LocalDate, PurchaseInfo> stockShares;
    private final Map<LocalDate, PurchaseInfo> bondShares;
    private final double effectiveTaxRate;
    private final USDollars tradeFee = USDollars.dollars(7);

    public static BasicAccount createAccount(int dollars, double effectiveTaxRate) {
        return new BasicAccount(USDollars.dollars(dollars), Collections.emptyMap(), Collections.emptyMap(), effectiveTaxRate);
    }

    public static BasicAccount createAccount(USDollars dollars, double effectiveTaxRate) {
        return new BasicAccount(dollars, Collections.emptyMap(), Collections.emptyMap(), effectiveTaxRate);
    }

    private BasicAccount(USDollars cash,Map<LocalDate, PurchaseInfo> stockShares,Map<LocalDate, PurchaseInfo> bondShares,
                         double effectiveTaxRate) {
        if (cash == null) {
            throw new IllegalArgumentException("Can't have null cash");
        }
        if (cash.isDebt()) {
            throw new IllegalArgumentException("Can't have a negative amount of cash in an account.");
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
        this.cash = cash;
        this.stockShares = Collections.unmodifiableMap(new HashMap<>(stockShares));
        this.bondShares = Collections.unmodifiableMap(new HashMap<>(bondShares));
        this.effectiveTaxRate = effectiveTaxRate;
    }

    public final Account executeOrder(final Order order, final CurrentPrices currentPrices, LocalDate presentDay) {
        Map<LocalDate, PurchaseInfo> assetShares = (order.getAsset() == Asset.STOCK) ? stockShares : bondShares;
        if (assetShares.get(presentDay) != null) {
            throw new IllegalArgumentException("A trade already exists for that asset on that date.");
        }
        USDollars price = (order.getAsset() == Asset.STOCK) ? currentPrices.getStockPrice() :
                currentPrices.getBondPrice();
        Map newAssets = new HashMap(assetShares);
        USDollars newCash = null;
        if (order.isPurchase()) {
            USDollars expense = price.times(order.getQuantity()).plus(getTradeFee());
            if (expense.isGreaterThan(cash)) {
                throw new IllegalArgumentException("Insufficient funds");
            }
            PurchaseInfo purchaseInfo = new PurchaseInfo(order.getQuantity(), order.getQuantity(),
                    price,presentDay,order.getAsset());

            newAssets.put(presentDay,purchaseInfo);
            newCash = cash.minus(expense);
        } else {
            PurchaseInfo purchaseInfo = assetShares.get(order.getDateSharesWerePurchased());

            USDollars totalGain = calculateNetGainToSellPosition(presentDay, purchaseInfo, price, order.getQuantity());
            newCash = cash.plus(totalGain);

            if (order.getQuantity() > purchaseInfo.getCurrentQuantity()) {
                throw new IllegalArgumentException("Insufficient shares");
            }
            if (order.getQuantity() == purchaseInfo.getCurrentQuantity()) {
                newAssets.remove(purchaseInfo.getDateExchanged());
            } else {
                newAssets.put(purchaseInfo.getDateExchanged(),purchaseInfo.lessQuantity(order.getQuantity()));
            }
        }
        if (order.getAsset() == Asset.STOCK) {
            return new BasicAccount(newCash, newAssets, bondShares, effectiveTaxRate);
        } else {
            return new BasicAccount(newCash, stockShares,newAssets, effectiveTaxRate);
        }
    }

    @Override
    public Account cashOut(final CurrentPrices currentPrices, LocalDate presentDay) {
        return new BasicAccount(netValue(currentPrices,presentDay),Collections.emptyMap(),
                Collections.emptyMap(),effectiveTaxRate);
    }

    @Override
    public Account addCash(USDollars cash) {
        if (cash == null) {
            throw new IllegalArgumentException("Can't have null cash");
        }
        if (cash.getTotalInCents() == 0) {
            return this;
        }
        return new BasicAccount(this.cash.plus(cash),this.stockShares,this.bondShares,this.effectiveTaxRate);
    }

    @Override
    public USDollars netValue(CurrentPrices currentPrices, LocalDate presentDay) {
        USDollars netCash = cash;
        for (PurchaseInfo purchaseInfo : stockShares.values()) {
            USDollars price = currentPrices.getStockPrice();
            USDollars totalGain = calculateNetGainToClosePosition(presentDay, purchaseInfo, price);
            netCash = netCash.plus(totalGain);
        }
        for (PurchaseInfo purchaseInfo : bondShares.values()) {
            USDollars price = currentPrices.getBondPrice();
            USDollars totalGain = calculateNetGainToClosePosition(presentDay, purchaseInfo, price);
            netCash = netCash.plus(totalGain);
        }
        return netCash;
    }

    public USDollars calculateNetGainToClosePosition(LocalDate presentDay, PurchaseInfo purchaseInfo, USDollars price) {
        return calculateNetGainToSellPosition(presentDay,purchaseInfo, price,purchaseInfo.getCurrentQuantity());
    }

    public USDollars calculateNetGainToSellPosition(LocalDate presentDay, PurchaseInfo purchaseInfo, USDollars price, int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative.");
        }
        if (quantity == 0) {
            return USDollars.cents(0);
        }
        if (quantity > purchaseInfo.getCurrentQuantity()){
            throw new IllegalArgumentException("Trying to sell more than we have.");
        }
        if (presentDay == null) {
            throw new IllegalArgumentException("present day cannot be null.");
        }
        if (purchaseInfo == null) {
            throw new IllegalArgumentException("purchase info is null");
        }
        if (price == null) {
            throw new IllegalArgumentException("price is null");
        }
        USDollars value = price.times(quantity);
        USDollars valueAfterFee = value.minus(tradeFee);
        USDollars costBasis = purchaseInfo.getCostBasis(tradeFee,quantity);
        USDollars capitalGains = valueAfterFee.minus(costBasis);

        boolean longTerm = DAYS.between(purchaseInfo.getDateExchanged(),presentDay) >= 365;
        double taxRate = (longTerm) ? getLongTermTaxRate() : getShortTermTaxRate();
        USDollars tax = capitalGains.times(taxRate);
        USDollars netGain = valueAfterFee.minus(tax);
        return netGain;
    }

    @Override
    public int getCurrentSharesStock() {
        int bonds = 0;
        for (PurchaseInfo purchaseInfo : stockShares.values()) {
            bonds += purchaseInfo.getCurrentQuantity();
        }
        return bonds;
    }

    @Override
    public int getCurrentSharesBonds() {
        int bonds = 0;
        for (PurchaseInfo purchaseInfo : bondShares.values()) {
            bonds += purchaseInfo.getCurrentQuantity();
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
    public USDollars getCurrentCash() {
        return cash;
    }

    @Override
    public USDollars getTradeFee() {
        return tradeFee;
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
