package knapp.simulation.strategy;

import knapp.simulation.Order;

import java.util.Set;

public class StrategyOrders {
    private final Set<Order> orders;
    private final MarketThoughts marketThoughts;

    public StrategyOrders(Set<Order> orders, MarketThoughts marketThoughts) {
        this.orders = orders;
        this.marketThoughts = marketThoughts;
    }

    public Set<Order> getOrders() {
        return orders;
    }

    public MarketThoughts getMarketThoughts() {
        return marketThoughts;
    }
}
