package knapp.simulator;

import knapp.simulation.*;
import knapp.simulation.strategy.InvestmentStrategy;
import knapp.simulator.function.NormalTest;
import knapp.simulator.strategy.AllocationStrategyTest;
import knapp.table.Frequency;
import knapp.table.Table;
import knapp.table.util.TableParser;
import knapp.table.util.TableUtil;
import knapp.util.InputLoader;
import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDate;
import java.util.*;

public class SimulatorTest {

    @Test
    public void averageROI() {
        double d = Simulater.calculateROI(USDollars.dollars(11),USDollars.dollars(10),1);
        Assert.assertTrue(Math.abs(d - .1) < 1e-3);

        double roi = 0.158;
        int years = 7;
        int initialFunds = 98523;
        int finalFunds = (int) (Math.pow( (1 + roi), years) * initialFunds);

        double calculatedRoi = Simulater.calculateROI(USDollars.dollars(finalFunds),USDollars.dollars(initialFunds),years);
        Assert.assertTrue(Math.abs(calculatedRoi - roi) < 1e-3);
    }

    @Test
    public void determineBondPaymentsDollars() {
        double bondROI = 0.02;
        Account account = BasicAccount.createAccount(10000,0.2);
        LocalDate start = LocalDate.of(2010,1,1);
        CurrentPrices currentPrices = new CurrentPrices(USDollars.dollars(80),USDollars.dollars(100));
        USDollars gained = Simulater.determineBondPaymentsDollars(account,start,Frequency.Weekly,bondROI);
        Assert.assertEquals(0,gained.getTotalInCents());

        Order order = Order.BuyStock(49);
        account = account.executeOrder(order,currentPrices,start);
        order = Order.BuyBonds(50);
        account = account.executeOrder(order,currentPrices,start);

        gained = Simulater.determineBondPaymentsDollars(account,start,Frequency.Weekly,bondROI);
        Assert.assertEquals(0,gained.getTotalInCents());

        // 50 shares, 100 $/share, 2% ROI, paid semi-annually so divide by two.
        USDollars expectedPayment = USDollars.dollars(100).times(50*0.02 / 2.0);

        LocalDate date = start.plusMonths(6).minusDays(2);
        gained = Simulater.determineBondPaymentsDollars(account,date,Frequency.Weekly,bondROI);
        Assert.assertEquals(0,gained.getTotalInCents());
        date = date.plusWeeks(1);
        gained = Simulater.determineBondPaymentsDollars(account,date,Frequency.Weekly,bondROI);

        Assert.assertEquals(expectedPayment,gained);
        date = date.plusWeeks(1);
        gained = Simulater.determineBondPaymentsDollars(account,date,Frequency.Weekly,bondROI);
        Assert.assertEquals(0,gained.getTotalInCents());
        date = date.plusWeeks(1);
        gained = Simulater.determineBondPaymentsDollars(account,date,Frequency.Weekly,bondROI);
        Assert.assertEquals(0,gained.getTotalInCents());

        // one year later
        date = start.plusMonths(12).minusDays(2);
        gained = Simulater.determineBondPaymentsDollars(account,date,Frequency.Weekly,bondROI);
        Assert.assertEquals(0,gained.getTotalInCents());
        date = date.plusWeeks(1);
        gained = Simulater.determineBondPaymentsDollars(account,date,Frequency.Weekly,bondROI);

        Assert.assertEquals(expectedPayment,gained);
        date = date.plusWeeks(1);
        gained = Simulater.determineBondPaymentsDollars(account,date,Frequency.Weekly,bondROI);
        Assert.assertEquals(0,gained.getTotalInCents());
    }

    @Test
    public void runOneDate() {
        Table market = InputLoader.loadTableFromClasspath("/market/IVE.csv");
        Table inputs = InputLoader.loadInputsTableFromClasspath(Arrays.asList("M1SL","UNRATE","IPMAN"));
        Table bondMarket = TableParser.produceConstantTable(100,LocalDate.of(1990,1,1),
                LocalDate.of(2018,1,1),Frequency.Weekly);
        Simulater simulater = new Simulater.SimulaterBuilder().bondMarket(bondMarket).bondROI(0.02)
                .frameYears(10).inputs(inputs).stockMarket(market).build();

        Map<LocalDate, HistoricRecord> history = new HashMap<>();
        AllocationStrategyTest.MyTestStrat strategy = new AllocationStrategyTest.MyTestStrat();
        strategy.response = new InvestmentAllocation(75,15,10);
        Simulater.AccountPointer pointer = new Simulater.AccountPointer();

        LocalDate present = LocalDate.of(2000,1,1);
        CurrentPrices firstPrices = new CurrentPrices(USDollars.dollars(90),USDollars.dollars(80));
        Account account = BasicAccount.createAccount(10000,0.2);

        Order order = Order.BuyStock(10);
        account = account.executeOrder(order,firstPrices,present.minusMonths(1));
        order = Order.BuyBonds(110);
        account = account.executeOrder(order,firstPrices,present.minusMonths(1));
        pointer.account = account;

        LocalDate testDate = LocalDate.of(2000,1,1);
        HistoricRecord historicRecord = simulater.runOneDate(strategy, pointer, testDate);
        history.put(testDate, historicRecord);

        Assert.assertEquals(2,historicRecord.getTransactions().size());
        Assert.assertEquals(1,history.size());
        Stance stance = history.get(testDate).getStance();
        Assert.assertTrue(Math.abs(75-stance.getPercentStock()) <= 1);
        Assert.assertEquals(USDollars.dollars(11429.52),stance.getNetWorthDollars());

        LocalDate testDate2 = testDate.plusMonths(3);
        strategy.response = new InvestmentAllocation(15,80,5);
        historicRecord = simulater.runOneDate(strategy, pointer, testDate2);
        history.put(testDate2,historicRecord);
        Assert.assertEquals(2,historicRecord.getTransactions().size());

//        Assert.assertEquals(4,transactions.size());
        Assert.assertEquals(2,history.size());
        stance = history.get(testDate2).getStance();
        Assert.assertTrue(Math.abs(15-stance.getPercentStock()) <= 1);
        Assert.assertEquals(USDollars.dollars(11903.71),stance.getNetWorthDollars());
    }
}
