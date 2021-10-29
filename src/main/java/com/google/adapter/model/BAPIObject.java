package com.google.adapter.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import java.util.ArrayList;
import java.util.List;

/**
 * @author dhiraj.kumar
 */
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id",
        scope = BAPIObject.class)
public class BAPIObject {
    /*
     * Example: id=001691, type=0, level=04, parent=001667, child=000000, name=BUS6035,
     * ext_name=AcctngDocument short_text=Accounting Document
     */
    private String id;
    private String name;
    private String extName;
    private String shortText;
    private BAPIObject parent;
    private List<BAPIObject> children;
    private List<BAPIMethod> methods;

    /**
     * Constructor to initialize objects
     * @param id ID
     * @param name Name
     * @param extName Ext name
     * @param shortText Description
     */
    public BAPIObject(String id, String name, String extName, String shortText) {
        this.id = id;
        this.name = name;
        this.extName = extName;
        this.shortText = shortText;
    }

    /**
     * Get ID
     * @return ID
     */
    public String getID() {
        return id;
    }

    /**
     * Get Name
     * @return Name
     */
    public String getName() {
        return name;
    }

    /**
     * Get Ext Name
     * @return Ext Name
     */
    public String getExtName() {
        return extName;
    }

    /**
     * Get description
     * @return Description
     */
    public String getShortText() {
        return shortText;
    }

    /**
     * Get Parent
     * @return parent
     */
    public BAPIObject getParent() {
        return parent;
    }

    /**
     * Set parent object
     * @param parent Bapi Object
     */
    public void setParent(BAPIObject parent) {
        this.parent = parent;
    }

    /**
     * Add child object
     * @param childObject BAPI Child Object
     */
    public void addChild(BAPIObject childObject) {
        if (childObject != null) {
            if (children == null) {
                children = new ArrayList<>();
            }
            children.add(childObject);
            childObject.setParent(this);
        }
    }

    /**
     * Remove child object
     * @param childObject Child object
     */
    public void removeChild(BAPIObject childObject) {
        childObject.setParent(null);
    }

    /**
     * Add BAPI method
     * @param method BAPI method
     */
    public void addMethod(BAPIMethod method) {
        if (method != null) {
            if (methods == null) {
                methods = new ArrayList<>();
            }
            methods.add(method);
        }
    }

    /**
     * Check BAPI object for leaf
     * @return boolean
     */
    public boolean isLeaf() {
        return children == null || children.isEmpty();
    }

    /**
     * Return list of BAPI
     * @return Children object
     */
    public List<BAPIObject> getChildren() {
        return children;
    }

    /**
     * Return list of BAPI Method
     * @return BAPI method
     */
    public List<BAPIMethod> getMethods() {
        return methods;
    }

    /**
     * Check BAPI for method
     * @return boolean
     */
    public boolean hasMethods() {
        return methods != null && !methods.isEmpty();
    }

    /**
     * Check if it is child or method
     * @return boolean
     */
    public boolean childOrSelfHasMethods() {
        if (isLeaf()) {
            return hasMethods();
        } else {
            return checkChild();
        }
    }

    /**
     * Check for Child Object
     * @return boolean
     */
    public boolean checkChild(){
        if (hasMethods()) {
            getMethods();//"non leaf node has methods: "
        }
        for (BAPIObject child : getChildren()) {
            if (child.childOrSelfHasMethods()) {
                return true;
            }
        }
        return false;
    }

    /**
     * To string method to return qualified name
     * @return Name
     */
    @Override
    public String toString() {
        String pid = (parent == null) ? null : parent.getID();
        return "BAPIObject [id=" + getID() + ", name=" + getName() + ", extName=" + getExtName()
                + ", shortText=" + getShortText() + ", parent=" + pid + "]";
    }
}
