package knapp.simulation.strategy;

import jdk.internal.org.objectweb.asm.tree.analysis.Analyzer;
import knapp.Model;
import knapp.TrendFinder;
import knapp.history.Frequency;
import knapp.simulation.Account;
import knapp.simulation.CurrentPrices;
import knapp.simulation.Order;
import knapp.table.Table;

import java.time.LocalDate;
import java.util.Set;

public class IntelligentStrategy implements InvestmentStrategy {

    private final TrendFinder trendFinder;

    public IntelligentStrategy(TrendFinder trendFinder) {
        this.trendFinder = trendFinder;
    }

    @Override
    public Set<Order> rebalance(LocalDate presentDay, Account account, Table inputs,
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

        if (estimate.getEstimatedValue() > estimate.getRealValue()) {
            // go all in
            return StrategyUtil.buyAsMuchStockAsPossible(presentDay,account,inputs,stockMarket,bondMarket,currentPrices);
        } else {
            return StrategyUtil.sellAllYourStock(presentDay,account,inputs,stockMarket,bondMarket,currentPrices);
            // go all out.
        }
    }
}
