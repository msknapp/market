package knapp.simulation.strategy;

import knapp.simulation.InvestmentAllocation;

public class AllocationAndThoughts {
    private final InvestmentAllocation investmentAllocation;
    private final MarketThoughts marketThoughts;

    public AllocationAndThoughts(InvestmentAllocation investmentAllocation, MarketThoughts marketThoughts) {
        this.investmentAllocation = investmentAllocation;
        this.marketThoughts = marketThoughts;
    }

    public InvestmentAllocation getInvestmentAllocation() {
        return investmentAllocation;
    }

    public MarketThoughts getMarketThoughts() {
        return marketThoughts;
    }
}
