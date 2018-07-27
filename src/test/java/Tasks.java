import knapp.Market;
import knapp.MarketContext;
import knapp.TrendFinder;
import knapp.history.Frequency;
import knapp.simulation.Account;
import knapp.simulation.Simulater;
import knapp.simulation.strategy.AllStockStrategy;
import knapp.simulation.strategy.IntelligentStrategy;
import knapp.simulation.strategy.InvestmentStrategy;
import knapp.table.DefaultGetMethod;
import knapp.table.Table;
import knapp.table.TableParser;
import knapp.util.InputLoader;
import org.junit.Test;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

public class Tasks {

    @Test
    public void download() throws IOException {
        MarketContext marketContext = Market.createContext();
        Market market = new Market(marketContext);
        market.retrieveData();
    }

    @Test
    public void testStrategy() throws IOException {
        LocalDate tableStart = LocalDate.of(1969,01,01);
        LocalDate simulationStart = LocalDate.of(1990,01,01);
        LocalDate simulationEnd = LocalDate.of(2018,06,01);
        String stockMarketText = InputLoader.loadTextFromClasspath("/market/s-and-p-500-weekly.csv");
        Table stockMarket = TableParser.parse(stockMarketText,true,Frequency.Weekly);
        stockMarket = stockMarket.retainColumns(Collections.singleton("Adj Close"));

        Table bondMarket = TableParser.produceConstantTable(100.0,tableStart,
                simulationEnd,Frequency.Monthly);

        List<String> series = Arrays.asList("INDPRO","UNRATE","TCU","WPRIME","WTB3MS");
        Table inputs = InputLoader.loadInputsTableFromClasspath(series,tableStart,simulationEnd,Frequency.Monthly);

        Simulater simulater = new Simulater.SimulaterBuilder().stockMarket(stockMarket)
                .bondMarket(bondMarket).bondROI(0.04).frameYears(20).inputs(inputs).build();

        DefaultGetMethod defaultGetMethod = new DefaultGetMethod();
        TrendFinder trendFinder = new TrendFinder(defaultGetMethod);
        InvestmentStrategy strategy = new IntelligentStrategy(trendFinder);

        InvestmentStrategy holdForeverStrategy = new AllStockStrategy();

        Account holdForeverEndingAccount = simulater.simulate(simulationStart,simulationEnd,10000,holdForeverStrategy);
        System.out.println("The guy who held it forever has: "+(holdForeverEndingAccount.getCurrentCents() / 100));
        Account endingAccount = simulater.simulate(simulationStart,simulationEnd,10000,strategy);
        System.out.println("The guy who used trade signals has: "+(endingAccount.getCurrentCents() / 100));
    }



}
