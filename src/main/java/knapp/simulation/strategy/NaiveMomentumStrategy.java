package knapp.simulation.strategy;

import knapp.simulation.Account;
import knapp.simulation.CurrentPrices;
import knapp.simulation.InvestmentAllocation;
import knapp.table.Table;
import knapp.table.values.TableColumnView;

import java.time.LocalDate;

public class NaiveMomentumStrategy extends AllocationStrategy {

    private boolean first = true;
    private boolean inverse;

    public NaiveMomentumStrategy(boolean inverse) {
        this.inverse = inverse;
    }

    @Override
    public AllocationAndThoughts chooseAllocation(LocalDate presentDay, Account account, Table inputs, Table stockMarket,
                                                 Table bondMarket, CurrentPrices currentPrices, InvestmentAllocation current) {
        MarketThoughts marketThoughts = new MarketThoughts();
        if (first) {
            first = false;
            InvestmentAllocation investmentAllocation = new InvestmentAllocation(70,30,0);
            marketThoughts.setDecisionComment("Just entering the market");
            return new AllocationAndThoughts(investmentAllocation,marketThoughts);
        }
        TableColumnView view = stockMarket.getTableColumnView(0);
        LocalDate last = view.getLastDate();
        LocalDate secondLast = view.getDateBefore(last);
        double lastVal = view.getExactValue(last);
        double secondLastVal = view.getExactValue(secondLast);
        marketThoughts.setRising(lastVal > secondLastVal);

        double chg = lastVal / secondLastVal - 1.0;

        InvestmentAllocation investmentAllocation = null;
        if (chg > 0.01) {
            if (inverse) {
                marketThoughts.setDecisionComment("The market grew over 1% let's assume it has momentum and buy more.");
                investmentAllocation = increaseStock(current);
            } else {
                marketThoughts.setDecisionComment("The market grew over 1% let's sell off a bit of stock");
                investmentAllocation = decreaseStock(current);
            }
        } else if (chg < -0.01) {
            if (inverse) {
                marketThoughts.setDecisionComment("The market dropped over 1% let's sell off a bit of stock");
                investmentAllocation = decreaseStock(current);
            } else {
                marketThoughts.setDecisionComment("The market dropped over 1% let's buy more stock.");
                investmentAllocation = increaseStock(current);
            }
        }
        return new AllocationAndThoughts(investmentAllocation, marketThoughts);
    }

    public InvestmentAllocation increaseStock(InvestmentAllocation current) {
        int stck = current.getPercentStock()+5;
        stck = (stck > 100) ? 100 : stck;
        int bnd = 100-stck;
        return new InvestmentAllocation(stck, bnd,0);
    }

    public InvestmentAllocation decreaseStock(InvestmentAllocation current) {
        int stck = current.getPercentStock()-5;
        stck = (stck < 0) ? 0 : stck;
        int bnd = 100-stck;
        return new InvestmentAllocation(stck, bnd,0);
    }

    @Override
    public int getMinimumPercentChange() {
        return 2;
    }

    @Override
    public boolean canEvolve() {
        return false;
    }
}
