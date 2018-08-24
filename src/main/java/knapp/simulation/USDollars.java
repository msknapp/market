package knapp.simulation;

public class USDollars implements Comparable<USDollars> {

    private final long cents;

    private USDollars(long cents) {
        this.cents = cents;
    }

    public static USDollars cents(long cents) {
        return new USDollars(cents);
    }

    public static USDollars dollars(int dollars) {
        return new USDollars(100L * dollars);
    }

    public static USDollars dollars(double dollars) {
        return new USDollars(Math.round(100.0 * dollars));
    }

    public long getTotalInCents() {
        return cents;
    }

    public long getRemainingCents() {
        return cents % 100;
    }

    public int getDollarsInt() {
        return (int)Math.floor((double)cents / 100.0);
    }

    public double getDollars() {
        return ((double)cents) / 100.0;
    }

    public String toString() {
        return String.format("$%.2f",getDollars());
    }

    public USDollars plus(USDollars dollars) {
        return new USDollars(cents + dollars.cents);
    }
    public USDollars minus(USDollars dollars) {
        return new USDollars(cents - dollars.cents);
    }

    public USDollars plusDollars(int dollars) {
        return new USDollars(cents + 100L*dollars);
    }

    public USDollars minusDollars(int dollars) {
        return plusDollars( - dollars);
    }

    public USDollars plusCents(long cnts) {
        return new USDollars(cents + cnts);
    }

    public USDollars minusCents(long cnts) {
        return plusCents( - cnts);
    }

    public USDollars times(int amount) {
        return new USDollars(cents * amount);
    }

    public USDollars times(double amount) {
        long x = Math.round((double)cents * amount);
        return USDollars.cents(x);
    }

    public USDollars dividedBy(int amount) {
        double newCents = (double)cents / (double)amount;
        return cents(Math.round(newCents));
    }

    public USDollars dividedBy(double amount) {
        double newCents = (double)cents / amount;
        return cents(Math.round(newCents));
    }

    public double dividedBy(USDollars amount) {
        return getDollars() / amount.getDollars();
    }

    public int hashCode() {
        return (int) cents;
    }

    public boolean isGreaterThan(USDollars x) {
        return this.cents > x.cents;
    }

    public boolean isGreaterThanOrEqualTo(USDollars x) {
        return this.cents >= x.cents;
    }

    public boolean isLessThan(USDollars x) {
        return this.cents < x.cents;
    }

    public boolean isLessThanOrEqualTo(USDollars x) {
        return this.cents <= x.cents;
    }

    @Override
    public boolean equals(Object obj) {
        return cents == ((USDollars)obj).cents;
    }

    @Override
    public int compareTo(USDollars o) {
        return (int) (cents - o.cents);
    }

    public boolean isDebt() {
        return cents < 0;
    }

    public USDollars negate() {
        return USDollars.cents(-cents);
    }
}