package knapp.simulation;

import knapp.simulation.strategy.MarketThoughts;
import knapp.simulation.strategy.StrategyOrders;
import knapp.table.Frequency;
import knapp.simulation.strategy.InvestmentStrategy;
import knapp.table.values.GetMethod;
import knapp.table.Table;
import knapp.util.Util;

import java.time.LocalDate;
import java.util.*;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.YEARS;

public class Simulater {
    private final Table stockMarket;
    private final Table bondMarket;
    private final Table inputs;
    private final int frameYears;
    private final double bondROI;
    private final Frequency tradeFrequency;

    private Simulater(Table stockMarket,Table bondMarket,Table inputs, int frameYears, double bondROI,
                      Frequency tradeFrequency) {
        if (bondROI < 0.01) {
            throw new IllegalArgumentException("bond yield is too low.");
        }
        if (bondROI > 0.06) {
            throw new IllegalArgumentException("bond yield is too high.");
        }
        if (!stockMarket.isExact()) {
            throw new IllegalArgumentException("Stock market data must be exact.");
        }
        if (!inputs.isExact()) {
            throw new IllegalArgumentException("The inputs data must be exact.");
        }
        if (tradeFrequency == null) {
            tradeFrequency = Frequency.Monthly;
        }
        this.stockMarket = stockMarket;
        this.bondMarket = bondMarket;
        this.inputs = inputs;
        this.frameYears = frameYears;
        this.bondROI = bondROI;
        this.tradeFrequency = tradeFrequency;
    }

    public SimulationResults simulate(LocalDate start, LocalDate end, USDollars initialDollars,
                                   InvestmentStrategy investmentStrategy) {
        Account initialAccount = BasicAccount.createAccount(initialDollars,0.2);
        final AccountPointer accountPointer = new AccountPointer();
        accountPointer.account = initialAccount;
        List<Transaction> transactions = new ArrayList<>();
        Map<LocalDate, HistoricRecord> history = new HashMap<>();
        Util.doWithDate(start, end, tradeFrequency, date -> {
            HistoricRecord record = runOneDate(investmentStrategy, accountPointer, date);
            history.put(date, record);
        });


        double stockPrice = stockMarket.getValue(end,0,GetMethod.INTERPOLATE);
        double bondPrice = bondMarket.getValue(end,0,GetMethod.INTERPOLATE);
        if (stockPrice < 1e-3 || bondPrice < 1e-3) {
            throw new IllegalStateException("Couldn't determine the true price of assets.");
        }
        CurrentPrices currentPrices = new CurrentPrices(USDollars.dollars(stockPrice),USDollars.dollars(bondPrice));
        accountPointer.account = accountPointer.account.cashOut(currentPrices,end);

        Account endingAccount =  accountPointer.account;
        USDollars endingDollars = endingAccount.getCurrentCash();

        int years = (int) YEARS.between(start,end);
        double averageROI = calculateROI(endingDollars,initialDollars,years);

        return new SimulationResults(endingDollars,endingAccount,averageROI,history,tradeFrequency);
    }

    public HistoricRecord runOneDate(InvestmentStrategy investmentStrategy, AccountPointer accountPointer,
                           LocalDate date) {

        double stockPrice = stockMarket.getValue(date,0,GetMethod.INTERPOLATE);
        double bondPrice = bondMarket.getValue(date,0,GetMethod.INTERPOLATE);
        if (stockPrice < 1e-3 || bondPrice < 1e-3) {
            throw new IllegalStateException("Couldn't determine the true price of assets.");
        }
        CurrentPrices currentPrices = new CurrentPrices(USDollars.dollars(stockPrice),USDollars.dollars(bondPrice));

        HistoricRecord historicRecord = runOneDateCore(currentPrices,investmentStrategy,accountPointer,date);

        Account account = accountPointer.account;
        USDollars netValue = account.netValue(currentPrices,date);
        int pctStock = determinePercentStock(account,currentPrices);
        Stance stance = new Stance(pctStock, netValue);

        return new HistoricRecord(date,historicRecord.getTransactions(),historicRecord.getMarketThoughts(),stance);
    }

    public HistoricRecord runOneDateCore(CurrentPrices currentPrices, InvestmentStrategy investmentStrategy,
                               AccountPointer accountPointer, LocalDate date) {

        // bonds pay dividends
        Account account = accountPointer.account;
        USDollars bondPayedDollars = determineBondPaymentsDollars(account,date);

        if (bondPayedDollars.getTotalInCents() > 0) {
            // always considered short term.
            USDollars tax = bondPayedDollars.times(account.getShortTermTaxRate());
            USDollars netPayment = bondPayedDollars.minus(tax);
            account = account.addCash(netPayment);
        }

        // the end date is exclusive in these functions, but I think we can argue that
        // you know what happened today.  I add one to the end date so the present day data is known.
        Table currentKnownStockMarket = stockMarket.untilExclusive(date.plusDays(1));
        Table currentKnownBondMarket = bondMarket.untilExclusive(date.plusDays(1));

        // IMPORTANT: You must maintain the lag associated with each parameter.
        // often times the inputs are not reported until three months later.
        Table currentKnownInputs = inputs.untilExclusive(date.plusDays(1));

        List<Transaction> outputTransactions = new ArrayList<>();

        StrategyOrders orders = investmentStrategy.rebalance(date, account, currentKnownInputs,
                currentKnownStockMarket, currentKnownBondMarket, currentPrices);
        if (orders.getMarketThoughts() == null) {
            throw new IllegalStateException("Market thoughts cannot be null.");
        }
        // always do all sales first.
        account = executeAllOrders(currentPrices, date, account, outputTransactions, orders.getOrders());
        accountPointer.account = account;
        return new HistoricRecord(date,outputTransactions, orders.getMarketThoughts(),null);
    }

    public static Account executeAllOrders(CurrentPrices currentPrices, LocalDate date, Account account,
                                           List<Transaction> outputTransactions, Set<Order> orders) {
        for (Order order : orders) {
            if (!order.isPurchase()) {
                account = account.executeOrder(order, currentPrices, date);
                USDollars price = order.getAsset() == Asset.STOCK ? currentPrices.getStockPrice() :
                        currentPrices.getBondPrice();
                Transaction transaction = new Transaction(date,order.getQuantity(),price,order.isPurchase());
                outputTransactions.add(transaction);
            }
        }
        for (Order order : orders) {
            if (order.isPurchase()) {
                account = account.executeOrder(order, currentPrices, date);
                USDollars price = order.getAsset() == Asset.STOCK ? currentPrices.getStockPrice() :
                        currentPrices.getBondPrice();
                Transaction transaction = new Transaction(date,order.getQuantity(),price,order.isPurchase());
                outputTransactions.add(transaction);
            }
        }
        return account;
    }

    public static final int determinePercentStock(Account account, CurrentPrices currentPrices) {
        USDollars stockValue = currentPrices.getStockPrice().times(account.getCurrentSharesStock());
        USDollars bondValue = currentPrices.getBondPrice().times(account.getCurrentSharesBonds());
        USDollars totalValue = account.getCurrentCash().plus(stockValue).plus(bondValue);
        return (int) Math.round(100.0 * stockValue.getDollars() / totalValue.getDollars());
    }

    public static double calculateROI(USDollars endingDollars, USDollars initialDollars, int years) {
        double ratio = endingDollars.dividedBy(initialDollars);
        double lnration = Math.log(ratio);
        double d = Math.exp(lnration / years)-1;
        return d;
    }

    private USDollars determineBondPaymentsDollars(Account account, LocalDate date) {
        return determineBondPaymentsDollars(account,date,tradeFrequency,bondROI);
    }

    public static USDollars determineBondPaymentsDollars(Account account, LocalDate date, Frequency tradeFrequency, double bondROI) {
        USDollars paymentDollars = USDollars.cents(0);
        for (PurchaseInfo purchaseInfo : account.getOwnedBondShares().values()) {
            double elapsedDays = (double)DAYS.between(purchaseInfo.getDateExchanged(),date);
            if (elapsedDays < 160) {
                // don't pay them immediately after they get it.
                continue;
            }
            LocalDate lastTradeDate = null;
            if (tradeFrequency == Frequency.Monthly) {
                lastTradeDate = date.minusMonths(1);
            } else if (tradeFrequency == Frequency.Weekly) {
                lastTradeDate = date.minusWeeks(1);
            }
            double elapsedDaysLastTime = (double)DAYS.between(purchaseInfo.getDateExchanged(),lastTradeDate);
            double remNow = elapsedDays % (365.25 / 2);
            double remLastTime = elapsedDaysLastTime % (365.25 / 2);
            if (remNow < remLastTime) {
                // paid semi-annually.
                USDollars thisPayment = purchaseInfo.getPriceDollars()
                        .times(purchaseInfo.getCurrentQuantity())
                        .times(bondROI / 2);
                paymentDollars = paymentDollars.plus(thisPayment);
            }
        }
        return paymentDollars;
    }

    public static class AccountPointer {
        public Account account;
    }

    public static class SimulaterBuilder {
        private Table stockMarket;
        private Table bondMarket;
        private Table inputs;
        private int frameYears = 20;
        private double bondROI = 0.04;
        private Frequency frequency;

        public SimulaterBuilder() {

        }

        public SimulaterBuilder stockMarket(Table market) {
            this.stockMarket = market;
            return this;
        }

        public SimulaterBuilder frequency(Frequency frequency) {
            this.frequency = frequency;
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
            return new Simulater(stockMarket,bondMarket,inputs,frameYears,bondROI, frequency);
        }
    }

}
