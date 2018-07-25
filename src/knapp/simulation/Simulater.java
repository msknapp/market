package knapp.simulation;

import knapp.history.Frequency;
import knapp.simulation.strategy.InvestmentStrategy;
import knapp.table.Table;
import knapp.table.TableImpl;
import knapp.util.Util;

import java.time.LocalDate;
import java.util.Set;

public class Simulater {
    private final Table stockMarket;
    private final Table bondMarket;
    private final Table inputs;
    private final int frameYears;

    private Simulater(Table stockMarket,Table bondMarket,Table inputs, int frameYears) {
        this.stockMarket = stockMarket;
        this.bondMarket = bondMarket;
        this.inputs = inputs;
        this.frameYears = frameYears;
    }

    public Account simulate(LocalDate start, LocalDate end, int initialDollars,
                                   InvestmentStrategy investmentStrategy) {
        Account initialAccount = BasicAccount.createAccount(initialDollars,0.2);
        final AccountPointer accountPointer = new AccountPointer();
        accountPointer.account = initialAccount;
        Util.doWithDate(start, end, Frequency.Monthly, date -> {
            double stockPrice = stockMarket.getValue(date,0,TableImpl.GetMethod.INTERPOLATE);
            double bondPrice = bondMarket.getValue(date,0,TableImpl.GetMethod.INTERPOLATE);
            if (stockPrice < 1e-3 || bondPrice < 1e-3) {
                throw new IllegalStateException("Couldn't determine the true price of assets.");
            }
            CurrentPrices currentPrices = new CurrentPrices(stockPrice,bondPrice);
            Account account = accountPointer.account;

            if (date.equals(end)) {
                account = account.cashOut(currentPrices,date);
            } else {
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
            }
            accountPointer.account = account;
        });
        return accountPointer.account;
    }

    private static class AccountPointer {
        Account account;
    }

    public static class SimulaterBuilder {
        private Table stockMarket;
        private Table bondMarket;
        private Table inputs;
        private int frameYears = 20;

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

        public Simulater build() {
            return new Simulater(stockMarket,bondMarket,inputs,frameYears);
        }
    }
}
