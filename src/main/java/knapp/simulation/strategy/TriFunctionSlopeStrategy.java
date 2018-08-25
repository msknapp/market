package knapp.simulation.strategy;

import knapp.predict.TrendFinder;
import knapp.simulation.InvestmentAllocation;
import knapp.simulation.functions.TriFunction;

import java.util.Map;

public class TriFunctionSlopeStrategy extends SlopeStrategy {

    private final TriFunction triFunction;

    public TriFunctionSlopeStrategy(TrendFinder trendFinder, Map<String, Integer> lags, TriFunction triFunction) {
        super(trendFinder, lags);
        this.triFunction = triFunction;
    }

    @Override
    public AllocationAndThoughts decide(InvestmentAllocation current, double sigma, double risePercent,
                                        double riseInEstimatePercent) {
        // experimentally it seems that the rise in estimate percent is the most important predictor
        // actual rise in value is the second best predicter.
        // the sigma is the least effective predicter of future value.
        double out = triFunction.apply(sigma,risePercent, riseInEstimatePercent);
        int p = (int) Math.round(out*100);
        p = Math.min(100,Math.max(0,p));
        InvestmentAllocation investmentAllocation = new InvestmentAllocation(p,100-p,0);
        MarketThoughts marketThoughts = new MarketThoughts();
        return new AllocationAndThoughts(investmentAllocation, marketThoughts);
    }
}
