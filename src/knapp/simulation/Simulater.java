package knapp.simulation;

import knapp.history.Frequency;
import knapp.simulation.strategy.InvestmentStrategy;
import knapp.table.Table;
import knapp.table.TableImpl;
import knapp.util.Util;

import java.time.LocalDate;
import java.util.Set;

import static java.time.temporal.ChronoUnit.DAYS;

public class Simulater {
    private final Table stockMarket;
    private final Table bondMarket;
    private final Table inputs;
    private final int frameYears;
    private final double bondROI;

    private Simulater(Table stockMarket,Table bondMarket,Table inputs, int frameYears, double bondROI) {
        if (bondROI < 0.01) {
            throw new IllegalArgumentException("bond yield is too low.");
        }
        if (bondROI > 0.06) {
            throw new IllegalArgumentException("bond yield is too high.");
        }
        this.stockMarket = stockMarket;
        this.bondMarket = bondMarket;
        this.inputs = inputs;
        this.frameYears = frameYears;
        this.bondROI = bondROI;
    }

    public Account simulate(LocalDate start, LocalDate end, int initialDollars,
                                   InvestmentStrategy investmentStrategy) {
        Account initialAccount = BasicAccount.createAccount(initialDollars,0.2);
        final AccountPointer accountPointer = new AccountPointer();
        accountPointer.account = initialAccount;
        Util.doWithDate(start, end, Frequency.Monthly, date -> {
            // bonds pay dividends

            double stockPrice = stockMarket.getValue(date,0,TableImpl.GetMethod.INTERPOLATE);
            double bondPrice = bondMarket.getValue(date,0,TableImpl.GetMethod.INTERPOLATE);
            if (stockPrice < 1e-3 || bondPrice < 1e-3) {
                throw new IllegalStateException("Couldn't determine the true price of assets.");
            }
            CurrentPrices currentPrices = new CurrentPrices(stockPrice,bondPrice);
            Account account = accountPointer.account;
            double bondPayedDollars = determineBondPaymentsDollars(account,date);

            if (bondPayedDollars > .01) {
                // always considered short term.
                double taxDollars = account.getShortTermTaxRate() * bondPayedDollars;
                double netDollars = bondPayedDollars - taxDollars;
                long newCents = Math.round(netDollars * 100);
                account = account.addCash(newCents);
            }

            LocalDate timeFrameStart = date.minusYears(frameYears);
            Table currentKnownStockMarket = stockMarket.inTimeFrame(timeFrameStart, date);
            Table currentKnownBondMarket = bondMarket.inTimeFrame(timeFrameStart, date);
            Table currentKnownInputs = inputs.inTimeFrame(timeFrameStart, date);
            Set<Order> orders = investmentStrategy.rebalance(date, account, currentKnownInputs,
                    currentKnownStockMarket, currentKnownBondMarket, currentPrices);
            // always do all sales first.
            for (Order order : orders) {
                if (!order.isPurchase()) {
                    account = account.executeOrder(order, currentPrices, date);
                }
            }
            for (Order order : orders) {
                if (order.isPurchase()) {
                    account = account.executeOrder(order, currentPrices, date);
                }
            }
            accountPointer.account = account;
            long netValue = account.netValueCents(currentPrices,date);
            double netDollars = netValue / 100;
//            System.out.println(String.format("On %s, the account is worth %f",date.toString(),netDollars));
        });


        double stockPrice = stockMarket.getValue(end,0,TableImpl.GetMethod.INTERPOLATE);
        double bondPrice = bondMarket.getValue(end,0,TableImpl.GetMethod.INTERPOLATE);
        if (stockPrice < 1e-3 || bondPrice < 1e-3) {
            throw new IllegalStateException("Couldn't determine the true price of assets.");
        }
        CurrentPrices currentPrices = new CurrentPrices(stockPrice,bondPrice);
        accountPointer.account = accountPointer.account.cashOut(currentPrices,end);
        return accountPointer.account;
    }

    private double determineBondPaymentsDollars(Account account, LocalDate date) {
        double paymentDollars = 0;
        for (PurchaseInfo purchaseInfo : account.getOwnedBondShares().values()) {
            double elapsedDays = (double)DAYS.between(purchaseInfo.getDateExchanged(),date);
            if (elapsedDays < 160) {
                // don't pay them immediately after they get it.
                continue;
            }
            LocalDate lastMonth = date.minusMonths(1);
            double elapsedDaysLastMonth = (double)DAYS.between(purchaseInfo.getDateExchanged(),lastMonth);
            double remNow = elapsedDays % (365.25 / 2);
            double remLastMonth = elapsedDaysLastMonth % (365.25 / 2);
            if (remNow < remLastMonth) {
                // paid semi-annually.
                paymentDollars += (bondROI / 2) * purchaseInfo.getPriceDollars() * purchaseInfo.getQuantity();
            }
        }
        return paymentDollars;
    }

    private static class AccountPointer {
        Account account;
    }

    public static class SimulaterBuilder {
        private Table stockMarket;
        private Table bondMarket;
        private Table inputs;
        private int frameYears = 20;
        private double bondROI = 0.04;

        public SimulaterBuilder() {

        }

        public SimulaterBuilder stockMarket(Table market) {
            this.stockMarket = market;
            return this;
        }

        public SimulaterBuilder inputs(Table inputs) {
            this.inputs = inputs;
            return this;
        }

        public SimulaterBuilder bondMarket(Table market) {
            this.bondMarket = market;
            return this;
        }

        public SimulaterBuilder frameYears(int x) {
            this.frameYears = x;
            return this;
        }

        public SimulaterBuilder bondROI(double x) {
            this.bondROI = x;
            return this;
        }

        public Simulater build() {
            return new Simulater(stockMarket,bondMarket,inputs,frameYears,bondROI);
        }
    }
}
