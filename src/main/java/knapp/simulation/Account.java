package knapp.simulation;

import java.time.LocalDate;
import java.util.Map;

public interface Account {
    int getCurrentSharesStock();
    int getCurrentSharesBonds();
    Map<LocalDate,PurchaseInfo> getOwnedStockShares();
    Map<LocalDate,PurchaseInfo> getOwnedBondShares();
    USDollars getCurrentCash();
    USDollars getTradeFee();
    double getShortTermTaxRate();
    double getLongTermTaxRate();
    Account executeOrder(final Order order, final CurrentPrices currentPrices, LocalDate presentDay);
    Account cashOut(final CurrentPrices currentPrices, final LocalDate presentDay);
    Account addCash(USDollars cash);
    USDollars netValue(final CurrentPrices currentPrices, final LocalDate presentDay);
}
