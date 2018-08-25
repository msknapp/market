package knapp.simulation.strategy;

import knapp.predict.MarketSlice;
import knapp.predict.NormalModel;
import knapp.predict.TrendFinder;
import knapp.simulation.Account;
import knapp.simulation.CurrentPrices;
import knapp.simulation.InvestmentAllocation;
import knapp.table.Table;
import knapp.table.values.TableColumnView;

import java.time.LocalDate;
import java.util.Map;

public abstract class SlopeStrategy extends SigmaBasedStrategy {


    public SlopeStrategy(TrendFinder trendFinder, Map<String, Integer> lags) {
        super(trendFinder, lags);
    }

    @Override
    public final AllocationAndThoughts chooseAllocation(LocalDate presentDay, Account account, Table inputs, Table stockMarket,
                                                  Table bondMarket, CurrentPrices currentPrices,
                                                  InvestmentAllocation current, double sigma, NormalModel model) {
        TableColumnView tableColumnView = stockMarket.getTableColumnView(0);
        LocalDate last = tableColumnView.getLastDate();
        LocalDate secondLast = tableColumnView.getDateBefore(last);
        double lastValue = tableColumnView.getExactValue(last);
        double secondLastValue = tableColumnView.getExactValue(secondLast);
        MarketSlice lastMS = inputs.getMarketSlice(last,getLags());
        MarketSlice secondLastMS = inputs.getMarketSlice(secondLast,getLags());
        double estimate = model.estimateValue(lastMS);
        double previousEstimate = model.estimateValue(secondLastMS);
        double rise = lastValue / secondLastValue - 1.0;
        double riseInEstimate = estimate / previousEstimate - 1.0;

        AllocationAndThoughts allocationAndThoughts = decide(current,sigma,rise,riseInEstimate);
        allocationAndThoughts.getMarketThoughts().setOvervalued(sigma < 0);
        allocationAndThoughts.getMarketThoughts().setRising(rise > 0);
        return allocationAndThoughts;
    }

    public abstract AllocationAndThoughts decide(InvestmentAllocation current, double sigma, double risePercent, double riseInEstimatePercent);

    @Override
    public boolean canEvolve() {
        return false;
    }
}
