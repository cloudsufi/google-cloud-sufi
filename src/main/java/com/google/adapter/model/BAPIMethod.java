/*
 * To change this template, choose Tools | Templates and open the template in the editor.
 */

package com.google.adapter.model;

/**
 *
 * @author dhiraj.kumar
 *
 */
public class BAPIMethod extends RFCMethod {
    private String objectType;
    private String method;
    private String methodName;
    private String objectName;

    /*
     * Example 1: object type: BUS6035, method: CHECK, method name: Check, function:
     * BAPI_ACC_DOCUMENT_CHECK, short text: Accounting: Check Document
     *
     * Example 2: object type: BUS6035, method: CHECKREVERSAL, method name: CheckReversal, function:
     * BAPI_ACC_DOCUMENT_REV_CHECK, short text:Accounting: Check Reversal
     */

    /**
     * Constructor to initialize objects
     * @param objectType object Type
     * @param method Method
     * @param methodName Method Name
     * @param function Function
     * @param shortText Description
     */
    public BAPIMethod(String objectType, String method, String methodName, String function,
            String shortText) {
        super(function, shortText, "");
        this.objectType = objectType;
        this.method = method;
        this.methodName = methodName;
    }

    /**
     * Get object type
     * @return objectType
     */
    public String getObjectType() {
        return objectType;
    }

    /**
     * Get Method
     * @return Method
     */
    public String getMethod() {
        return method;
    }

    /**
     * Get method name
     * @return Method name
     */
    public String getMethodName() {
        return methodName;
    }

    /**
     * To string method to return name
     * @return Method name
     */
    @Override
    public String toString() {
        return getMethodName();
    }

    /**
     * @return the objectName
     */
    public String getObjectName() {
        return objectName;
    }

    /**
     * @param objectName the objectName to set
     */
    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    /**
     * @return the display Name
     */
    public String getDisplayName() {
        return objectName + "." + methodName;
    }
}
