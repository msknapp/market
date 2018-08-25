package knapp.simulation.strategy;

import knapp.predict.TrendFinder;
import knapp.simulation.InvestmentAllocation;

import java.util.Map;

public class SimpleSlopeStrategy extends SlopeStrategy {

    public SimpleSlopeStrategy(TrendFinder trendFinder, Map<String, Integer> lags) {
        super(trendFinder, lags);
    }

    @Override
    public AllocationAndThoughts decide(InvestmentAllocation current, double sigma, double risePercent,
                                        double riseInEstimatePercent) {
        int base = (int) (50 + 500*risePercent);
        int p = (int) (base + 1000 * riseInEstimatePercent);
        p = Math.min(100,Math.max(0,p));
        InvestmentAllocation investmentAllocation = new InvestmentAllocation(p,100-p,0);
        MarketThoughts marketThoughts = new MarketThoughts();
        marketThoughts.setDecisionComment(String.format("It rose %.2f and estimate rose %.2f",risePercent,
                riseInEstimatePercent));
        return new AllocationAndThoughts(investmentAllocation, marketThoughts);
    }
}
