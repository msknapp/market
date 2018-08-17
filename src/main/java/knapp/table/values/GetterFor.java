package knapp.table.values;

public class GetterFor {

    public static TableValueGetter getterFor(GetMethod getMethod) {
        if (getMethod == GetMethod.LAST_KNOWN_VALUE) {
            return new LastValueGetter();
        } else if (getMethod == GetMethod.EXACT) {
            return new ExactValueGetter();
        } else if (getMethod == GetMethod.EXTRAPOLATE) {
            return new ExtrapolatedValuesGetter();
        } else if (getMethod == GetMethod.INTERPOLATE) {
            return new InterpolatedValuesGetter();
        }
        return null;
    }
}
