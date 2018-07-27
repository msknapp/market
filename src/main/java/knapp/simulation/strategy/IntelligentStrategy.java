package knapp.simulation.strategy;

import jdk.internal.org.objectweb.asm.tree.analysis.Analyzer;
import knapp.Model;
import knapp.TrendFinder;
import knapp.history.Frequency;
import knapp.simulation.*;
import knapp.table.Table;

import java.time.LocalDate;
import java.util.Set;

import static java.time.temporal.ChronoUnit.DAYS;

public class IntelligentStrategy extends AllocationStrategy implements InvestmentStrategy {

    private final TrendFinder trendFinder;

    public IntelligentStrategy(TrendFinder trendFinder) {
        this.trendFinder = trendFinder;
    }

    @Override
    public InvestmentAllocation chooseAllocation(LocalDate presentDay, Account account, Table inputs,
                                Table stockMarket, Table bondMarket, CurrentPrices currentPrices) {
        LocalDate end = presentDay;
        LocalDate start = end.minusYears(10);

        TrendFinder.Analasys analysis = trendFinder.startAnalyzing().start(start).
                end(end).inputs(inputs).market(stockMarket).
                frequency(Frequency.Monthly).build();
        Model model = analysis.deriveModel();

        double[] lastInputs = inputs.getExactValues(inputs.getLastDate());
        double lastMarketValue = stockMarket.getExactValues(stockMarket.getLastDate())[0];
        Model.Estimate estimate = model.produceEstimate(lastInputs,lastMarketValue);

        // a positive sigmas means the estimate is higher than the actual value,
        // so the market is undervalued.
        if (estimate.getSigmas() > 2) {
            return new InvestmentAllocation(100,0,0);
        } else if (estimate.getSigmas() > 1) {
            return new InvestmentAllocation(85,15,0);
        } else if (estimate.getSigmas() > 0) {
            return new InvestmentAllocation(75,25,0);
        } else if (estimate.getSigmas() > -1) {
            return new InvestmentAllocation(50,50,0);
        } else if (estimate.getSigmas() > -2) {
            return new InvestmentAllocation(35,65,0);
        } else {
            // the stock market appears to be very overvalued.
            return new InvestmentAllocation(25,75,0);
        }

//        if (estimate.getEstimatedValue() > estimate.getRealValue()) {
//            // go all in
////            System.out.println(String.format("On %s we are buying all the stock we can.",presentDay.toString()));
//            Set<Order> orders = StrategyUtil.sellAllYourBonds(presentDay,account,inputs,stockMarket,bondMarket,currentPrices);
//            double availableDollars = determineFinalMoney(account,presentDay,orders, currentPrices);
//            double sharesDouble = (availableDollars - (account.getTradeFeeCents()/100.0) ) / currentPrices.getStockPriceDollars();
//            int shares = (int) Math.floor(sharesDouble);
//            if (shares > 0) {
//                Order order = Order.BuyStock(shares);
//                orders.add(order);
//            }
//            return orders;
////            return StrategyUtil.buyAsMuchStockAsPossible(presentDay,account,inputs,stockMarket,bondMarket,currentPrices);
//            // TODO with spare money buy up bonds.
//        } else {
////            System.out.println(String.format("On %s we are switching as much to bonds as possible.", presentDay.toString()));
//            Set<Order> orders = StrategyUtil.sellAllYourStock(presentDay,account,inputs,stockMarket,bondMarket,currentPrices);
//
//            // buy all the bonds you can! go all out.
//            double availableDollars = determineFinalMoney(account,presentDay,orders, currentPrices);
//            double sharesDouble = (availableDollars - (account.getTradeFeeCents()/100.0) ) / currentPrices.getBondPriceDollars();
//            int shares = (int) Math.floor(sharesDouble);
//            if (shares > 0) {
//                Order order = Order.BuyBonds(shares);
//                orders.add(order);
//            }
//            return orders;
//        }
    }

    @Override
    public int getMinimumPercentChange() {
        return 10;
    }

//    private double determineFinalMoney(Account account, LocalDate date, Set<Order> sales,
//                                       CurrentPrices currentPrices) {
//        double dollars = ((double)account.getCurrentCents() / 100.0);
//        for (Order order : sales) {
//            if (order.isPurchase()) {
//                throw new IllegalArgumentException("This only works with sales.");
//            }
//            PurchaseInfo purchaseInfo = (order.getAsset() == Asset.STOCK) ?
//                    account.getOwnedStockShares().get(order.getDateSharesWerePurchased()) :
//                    account.getOwnedBondShares().get(order.getDateSharesWerePurchased());
//            double priceDollars = (order.getAsset() == Asset.STOCK) ? currentPrices.getStockPriceDollars() :
//                    currentPrices.getBondPriceDollars();
//            double incomeDollars = order.getQuantity() * priceDollars - (account.getTradeFeeCents() / 100.0);
//            double costBasis = order.getQuantity() * purchaseInfo.getPriceDollars();
//            double profit = incomeDollars - costBasis;
//            boolean longTerm = DAYS.between(order.getDateSharesWerePurchased(),date) > 365;
//            double taxRate = (longTerm) ? account.getLongTermTaxRate() : account.getShortTermTaxRate();
//            double taxDollars = profit * taxRate;
//
//            dollars += incomeDollars - taxDollars;
//
//        }
//        return dollars;
//    }
}
