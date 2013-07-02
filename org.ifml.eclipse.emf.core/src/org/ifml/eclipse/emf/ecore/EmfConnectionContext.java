package org.ifml.eclipse.emf.ecore;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;

/**
 * Describes the context of a connection creation.
 */
public final class EmfConnectionContext {

    private final EObject sourceObj;

    private final EObject targetObj;

    private final EClass connClass;

    /**
     * Creates a new context.
     * 
     * @param sourceObj
     *            the source object.
     * @param targetObj
     *            the target object.
     * @param connClass
     *            the connection class.
     */
    public EmfConnectionContext(EObject sourceObj, EObject targetObj, EClass connClass) {
        this.sourceObj = sourceObj;
        this.targetObj = targetObj;
        this.connClass = connClass;
    }

    /**
     * Returns the source object.
     * 
     * @return the source object.
     */
    public EObject getSourceObject() {
        return sourceObj;
    }

    /**
     * Returns the target object.
     * 
     * @return the target object.
     */
    public EObject getTargetObject() {
        return targetObj;
    }

    /**
     * Returns the connection class.
     * 
     * @return the connection class.
     */
    public EClass getConnectionClass() {
        return connClass;
    }

}
