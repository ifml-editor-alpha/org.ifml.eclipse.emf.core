package org.ifml.eclipse.emf.ecore;

import org.eclipse.emf.ecore.EClass;
import org.ifml.base.ImmutablePair;

/**
 * A pair of {@link EClass} elements.
 */
public final class EClassPair {

    private final ImmutablePair<EClass, EClass> pair;

    private EClassPair(EClass first, EClass second) {
        this.pair = ImmutablePair.of(first, second);
    }

    /**
     * Returns the first class.
     * 
     * @return the first class.
     */
    public EClass getFirst() {
        return pair.first;
    }

    /**
     * Returns the second class.
     * 
     * @return the second class.
     */
    public EClass getSecond() {
        return pair.second;
    }

    /**
     * Obtains a pair.
     * 
     * @param first
     *            first class.
     * @param second
     *            second class.
     * @return constructed pair
     */
    public static EClassPair of(EClass first, EClass second) {
        return new EClassPair(first, second);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof EClassPair)) {
            return false;
        }
        return pair.equals(((EClassPair) obj).pair);
    }

    @Override
    public int hashCode() {
        return pair.hashCode();
    }

    @Override
    public String toString() {
        return pair.toString();
    }

}
