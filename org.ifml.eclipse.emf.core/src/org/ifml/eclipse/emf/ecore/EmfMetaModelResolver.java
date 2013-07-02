package org.ifml.eclipse.emf.ecore;

import java.util.Map;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

/**
 * An EMF model inspector able to return (and cache) various information about EMF classes.
 */
public final class EmfMetaModelResolver {

    private final LoadingCache<EClassPair, Optional<EReference>> containmentFeatures;

    private final Map<EReference, Predicate<EClassPair>> containmentPredicates;

    private final Map<EReference, Predicate<EmfConnectionContext>> connectionPredicates;

    private LoadingCache<EClass, Optional<EReference>> connectionSourceReferences;

    private LoadingCache<EClass, Optional<EReference>> connectionTargetReferences;

    private EmfMetaModelResolver(Builder builder) {
        containmentPredicates = ImmutableMap.copyOf(builder.containmentPredicates);
        containmentFeatures = CacheBuilder.newBuilder().build(new CacheLoader<EClassPair, Optional<EReference>>() {
            @Override
            public Optional<EReference> load(EClassPair input) {
                for (EReference ref : input.getFirst().getEAllContainments()) {
                    if (ref.getEReferenceType().isSuperTypeOf(input.getSecond())) {
                        Predicate<EClassPair> predicate = containmentPredicates.get(ref);
                        if ((predicate == null) || predicate.apply(input)) {
                            return Optional.of(ref);
                        }
                    }
                }
                return Optional.absent();
            }
        });
        connectionPredicates = ImmutableMap.copyOf(builder.connectionPredicates);
        connectionSourceReferences = CacheBuilder.newBuilder().build(new CacheLoader<EClass, Optional<EReference>>() {
            @Override
            public Optional<EReference> load(EClass input) {
                for (EClass superType : input.getEAllSuperTypes()) {
                    Optional<EReference> ref = connectionSourceReferences.getUnchecked(superType);
                    if (ref.isPresent()) {
                        return ref;
                    }
                }
                return Optional.absent();
            }
        });
        connectionSourceReferences.asMap().putAll(builder.connectionSourceReferences);
        connectionTargetReferences = CacheBuilder.newBuilder().build(new CacheLoader<EClass, Optional<EReference>>() {
            @Override
            public Optional<EReference> load(EClass input) {
                for (EClass superType : input.getEAllSuperTypes()) {
                    Optional<EReference> ref = connectionTargetReferences.getUnchecked(superType);
                    if (ref.isPresent()) {
                        return ref;
                    }
                }
                return Optional.absent();
            }
        });
        connectionTargetReferences.asMap().putAll(builder.connectionTargetReferences);
    }

    /**
     * Returns the containment feature of a super-class designed to contain a sub-class.
     * 
     * @param superType
     *            the super-type.
     * @param subType
     *            the sub-type.
     * @return the containment feature.
     */
    public Optional<EReference> getContainmentReference(EClass superType, EClass subType) {
        return containmentFeatures.getUnchecked(EClassPair.of(superType, subType));
    }

    /**
     * Returns whether a connection can connect the given source and target objects.
     * 
     * @param sourceObj
     *            the source object.
     * @param targetObj
     *            the target object.
     * @param connClass
     *            the connection class.
     * @return {@code true} if a new connection of type {@code connClass} can be used to connect {@code sourceObj} with
     *         {@code targetObj}
     */
    public boolean canConnect(EObject sourceObj, EObject targetObj, EClass connClass) {
        Optional<EReference> connRef = getContainmentReference(sourceObj.eClass(), connClass);
        if (!connRef.isPresent()) {
            return false;
        }
        EReference targetRef = getConnectionTargetReference(connClass).get();
        if (!targetRef.getEReferenceType().isInstance(targetObj)) {
            return false;
        }
        Predicate<EmfConnectionContext> pred = connectionPredicates.get(connRef.get());
        if (pred == null) {
            return true;
        } else {
            return pred.apply(new EmfConnectionContext(sourceObj, targetObj, connClass));
        }
    }

    /**
     * Returns the source reference of a connection.
     * 
     * @param connClass
     *            the connection class.
     * @return the source reference.
     */
    public Optional<EReference> getConnectionSourceReference(EClass connClass) {
        return connectionSourceReferences.getUnchecked(connClass);
    }

    /**
     * Returns the target reference of a connection.
     * 
     * @param connClass
     *            the connection class.
     * @return the target reference.
     */
    public Optional<EReference> getConnectionTargetReference(EClass connClass) {
        return connectionTargetReferences.getUnchecked(connClass);
    }

    /**
     * A builder for the {@link EmfMetaModelResolver}.
     */
    public static class Builder {

        private final Map<EReference, Predicate<EClassPair>> containmentPredicates = Maps.newHashMap();

        private final Map<EReference, Predicate<EmfConnectionContext>> connectionPredicates = Maps.newHashMap();

        private final Map<EClass, Optional<EReference>> connectionSourceReferences = Maps.newHashMap();

        private final Map<EClass, Optional<EReference>> connectionTargetReferences = Maps.newHashMap();

        /**
         * Constructs the {@link EmfMetaModelResolver}.
         * 
         * @return the meta model resolver.
         */
        public EmfMetaModelResolver build() {
            return new EmfMetaModelResolver(this);
        }

        /**
         * Adds a predicate for filtering the result of the {@link #containmentPredicate(EReference, Predicate)} method.
         * 
         * @param ref
         *            the reference.
         * @param predicate
         *            the predicate which further checks if the containment reference {@code ref} is valid.
         * @return this builder.
         */
        public Builder containmentPredicate(EReference ref, Predicate<EClassPair> predicate) {
            containmentPredicates.put(ref, predicate);
            return this;
        }

        /**
         * Adds a predicate for checking if a connection can be created.
         * 
         * @param ref
         *            the reference.
         * @param predicate
         *            the predicate which further checks if the connection can be created.
         * @return this builder.
         */
        public Builder connectionPredicate(EReference ref, Predicate<EmfConnectionContext> predicate) {
            connectionPredicates.put(ref, predicate);
            return this;
        }

        /**
         * Registers the source and target references for a connection.
         * 
         * @param connClass
         *            the connection class.
         * @param sourceRef
         *            the source reference.
         * @param targetRef
         *            the target reference.
         * @return this builder.
         */
        public Builder connectionEndpointReferences(EClass connClass, EReference sourceRef, EReference targetRef) {
            connectionSourceReferences.put(connClass, Optional.fromNullable(sourceRef));
            connectionTargetReferences.put(connClass, Optional.fromNullable(targetRef));
            return this;
        }

    }

}
