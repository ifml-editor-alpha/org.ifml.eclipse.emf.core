package org.ifml.eclipse.emf.ecore;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;

import com.google.common.base.Predicate;

/**
 * Provides useful {@link Predicate} objects working on EMF concepts.
 */
public final class EmfPredicates {

    private EmfPredicates() {
    }

    /**
     * Returns a predicate which evaluates {@code true} if the {@link EObject} being tested belongs to a specific {@link EClass}.
     * 
     * @param eClass
     *            the {@link EClass}.
     * @return a predicate.
     */
    public static final Predicate<EObject> isInstance(EClass eClass) {
        return new IsInstancePredicate(eClass);
    }

    private static final class IsInstancePredicate implements Predicate<EObject> {

        private final EClass eClass;

        public IsInstancePredicate(EClass eClass) {
            this.eClass = eClass;
        }

        @Override
        public boolean apply(EObject input) {
            return eClass.isInstance(input);
        }

    }

}
