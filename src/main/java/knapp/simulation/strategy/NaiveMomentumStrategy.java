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
    public InvestmentAllocation chooseAllocation(LocalDate presentDay, Account account, Table inputs, Table stockMarket,
                                                 Table bondMarket, CurrentPrices currentPrices, InvestmentAllocation current) {
        if (first) {
            first = false;
            return new InvestmentAllocation(70,30,0);
        }
        TableColumnView view = stockMarket.getTableColumnView(0);
        LocalDate last = view.getLastDate();
        LocalDate secondLast = view.getDateBefore(last);
        double lastVal = view.getExactValue(last);
        double secondLastVal = view.getExactValue(secondLast);

        double chg = lastVal / secondLastVal - 1.0;

        if (chg > 0.01) {
            if (inverse) {
                increaseStock(current);
            } else {
                decreaseStock(current);
            }
        } else if (chg < -0.01) {
            if (inverse) {
                decreaseStock(current);
            } else {
                increaseStock(current);
            }
        }
        return null;
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
