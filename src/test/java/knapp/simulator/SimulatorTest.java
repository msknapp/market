package knapp.simulator;

import knapp.simulation.*;
import knapp.simulator.function.NormalTest;
import knapp.table.Frequency;
import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDate;

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
        // TODO test this.
    }
}
