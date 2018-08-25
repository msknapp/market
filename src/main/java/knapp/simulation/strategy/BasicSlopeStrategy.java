package knapp.simulation.strategy;

import knapp.predict.TrendFinder;
import knapp.simulation.InvestmentAllocation;

import java.util.Map;

public class BasicSlopeStrategy extends SlopeStrategy {

    public BasicSlopeStrategy(TrendFinder trendFinder, Map<String, Integer> lags) {
        super(trendFinder, lags);
    }

    @Override
    public AllocationAndThoughts decide(InvestmentAllocation current, double sigma, double risePercent, double riseInEstimatePercent) {

        MarketThoughts marketThoughts = new MarketThoughts();
        InvestmentAllocation investmentAllocation = null;
        if (risePercent > .01 && riseInEstimatePercent > 0.01) {
            investmentAllocation = new InvestmentAllocation(100,0,0);
            marketThoughts.setDecisionComment("Market is rising - estimate is rising - go all in!");
            return new AllocationAndThoughts(investmentAllocation, marketThoughts);
        }
        if (risePercent < -0.01 && riseInEstimatePercent < -0.01) {
            investmentAllocation = new InvestmentAllocation(0,100,0);
            marketThoughts.setDecisionComment("Market is falling - estimate is falling - get out!");
            return new AllocationAndThoughts(investmentAllocation, marketThoughts);
        }
        if (sigma > 0) {
            // it is undervalued.
            if (risePercent > 0) {
                if (riseInEstimatePercent > 0) {

                } else {

                }
            } else {
                if (riseInEstimatePercent > 0) {

                } else {

                }
            }
        } else {
            // it is overvalued.
            if (risePercent > 0) {
                // price is increasing
                if (riseInEstimatePercent > 0) {
                    // estimate is increasing.
                    // in spite of being overvalued, I buy more.
                    int p = current.getPercentStock() + 5;
                    p = Math.min(100,p);
                    investmentAllocation = new InvestmentAllocation(p,100-p,0);
                    marketThoughts.setDecisionComment("Market is falling - estimate is falling - get out!");
                    return new AllocationAndThoughts(investmentAllocation, marketThoughts);
                } else {
                    int p = current.getPercentStock() - 5;
                    p = Math.max(0,p);
                    investmentAllocation = new InvestmentAllocation(p,100-p,0);
                    marketThoughts.setDecisionComment("Market is falling - estimate is falling - get out!");
                    return new AllocationAndThoughts(investmentAllocation, marketThoughts);
                }
            } else {
                // price is decreasing
                if (riseInEstimatePercent > 0) {
                    // estimate is increasing.
                    // in spite of being overvalued, I buy more.
                    int p = current.getPercentStock() + 5;
                    p = Math.min(100,p);
                    investmentAllocation = new InvestmentAllocation(p,100-p,0);
                    marketThoughts.setDecisionComment("Market is falling - estimate is falling - get out!");
                    return new AllocationAndThoughts(investmentAllocation, marketThoughts);
                } else {
                    int p = current.getPercentStock() - 5;
                    p = Math.max(0,p);
                    investmentAllocation = new InvestmentAllocation(p,100-p,0);
                    marketThoughts.setDecisionComment("Market is falling - estimate is falling - get out!");
                    return new AllocationAndThoughts(investmentAllocation, marketThoughts);
                }
            }

        }
        // TODO
        return null;
    }
}
