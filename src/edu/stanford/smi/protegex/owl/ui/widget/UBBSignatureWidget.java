package edu.stanford.smi.protegex.owl.ui.widget;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.Assert;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.widget.TextFieldWidget;
import edu.stanford.smi.protegex.owl.model.*;
import edu.stanford.smi.protegex.owl.ui.ProtegeUI;
import edu.stanford.smi.protegex.owl.util.InstanceUtil;

import java.awt.*;
import java.util.Collection;
import java.util.logging.Logger;

/**
 * A widget that holds identifier value and
 * modifies value for classHierarchyURI slot (http://data.ub.uib.no/ontology/classHierarchyURI")
 * based on change of it's value
 *
 * @author Hemed Al Ruwehy
 * University of Bergen Library
 * 2017-04-27
 */
public class UBBSignatureWidget extends TextFieldWidget {
    private static transient Logger log = Log.getLogger(UBBSignatureWidget.class);
    private static final String EMPTY_STRING = "";

    public static boolean isSuitable(Cls cls, Slot slot, Facet facet) {
        //Check if a slot accept values of type String (has range of data type String),
        //if it does then show this widget in the dropdown list as one of the it's options.
        boolean isString = cls.getTemplateSlotValueType(slot) == ValueType.STRING;

        return isString;
    }

    /**
      This method is called on value change.
     */
    @Override
    public Collection getValues() {
        String newValue = getText();
        //System.out.println("Slot values: " + slotValue);
        validateSignature(newValue);
        //Value change will be executed in this given slot
        RDFProperty classHierarchySlot = getSlot(UBBOntologyNamespaces.CLASS_HIERARCHY_URI);
        prepareValueChange(classHierarchySlot, newValue);

        return super.getValues();
    }

    /**
     * Identifier slot will get it's value when editing,
     * we don't have to do anything when instance is created
     */
    @Override
    public void setValues(Collection collection) {
        //System.out.println("Signature Collection values: " + getSlotValues());
        super.setValues(getSlotValues());
    }

    /**
     * Get values for this slot, or null if no value
     */
    private Collection getSlotValues() {
        return getInstance().getDirectOwnSlotValues(getSlot());
    }


    private RDFProperty getSlot(String name) {
        return getOWLModel().getRDFProperty(name);
    }

    private OWLModel getOWLModel() {
        return (OWLModel) getKnowledgeBase();
    }


    /**
     * Validate signature. Signature is not UUID, and is not a URI, it is UiB specific number
     */
    private void validateSignature(String signature) {
        if (UUIDWidget.isValidUUID(signature)) {
            showErrorMessage("Encountered UUID [" + signature + "] which is not valid signature. Try another one");
            //Do not continue
            throw new IllegalArgumentException("Encountered UUID [" + signature + "] which is not valid signature");
        /*} else if (startsWithScheme(signature) || isValidUri(signature)) {
            showErrorMessage("Encountered URI [" + signature + "] which is not valid signature. Try another one");
            //Do not continue
            throw new IllegalArgumentException("URI [" + signature + "] is not a valid signature");
        */
        } else if (slotValueExists(getSlot(UBBOntologyNamespaces.IDENTIFIER), signature)) {
            showErrorMessage("Signature \"" + signature + "\" already exists. Try another one");
            //Do not continue
            throw new IllegalArgumentException("Signature already exists for value [" + signature + "]");
        }
    }


    /**
     * Prepare value change for the a given slot
     *
     * @param slot  a slot that it's value need to be changed
     * @param newValue a new value.
     */
    private void prepareValueChange(RDFProperty slot, String newValue) {
        if (slot != null) {
            //Old value is in the form of "http://data.ub.uib.no/{class_name}/{id}"
            Object oldValue = getSubject().getPropertyValue(slot);
            if(oldValue == null){
                //Assign old value a default value to avoid null
                oldValue = createLiteral(EMPTY_STRING, getDefaultDatatype());
            }
            //Get the current selected instance
            Instance instance = getInstance();
            //Get UUID from instance URI
            String uuid = UUIDWidget.getUUIDFromInstanceURI(instance);
            //Get corresponding class URI prefix
            String classHierarchyPrefix = InstanceUtil.getClassURIPrefix(instance);
            //Create a full URI for the new slot value
            if(newValue == null){
                newValue = classHierarchyPrefix + uuid;
            }
            else {//Should we encode URI?, doing so it may skip validation.
                newValue = classHierarchyPrefix + newValue.toLowerCase();
            }
            //Do not proceed if new value is not a valid URI
            if(isValidUri(newValue)) {
                RDFSLiteral newValueLiteral = createLiteral(newValue, getDefaultDatatype());
                replaceSlotValue(slot, oldValue, newValueLiteral);
            }
        }
    }


    /**
     * Replace old value to a new value for a given slot. Note that this method does not perform just simple replace,
     * rather a replacement such that a new value does not exist anywhere in the knowledgebase.
     * In other words, the method ensures uniqueness of the new value and
     * checks to the entire knowledgebase whether the value exists before replacement.
     *
     * Feasible usecase would be, for example, when you want to modify values for an identifier slot
     * in which you don't want to end up with same identifier for different resources.
     *
     * @param oldValue an old value to be replaced with a new value.
     * @param replacement a new value. Cannot be <tt>null</tt>
     */
    protected void replaceSlotValue(RDFProperty slot, Object oldValue, Object replacement) {
        //Ensure non-null values
        Assert.assertNotNull("Value cannot be null", oldValue);
        Assert.assertNotNull("Replacement cannot be null", replacement);
        //Update value of a given slot iff it is not the same with the old one.
        if (!replacement.equals(oldValue)) {
            //Extract string representation of this object
            Object stringValue = replacement;
            if(replacement instanceof RDFSLiteral){
                stringValue = ((RDFSLiteral) replacement).getString();
            }
            //Check whether this value has been used somewhere in the knowledgebase.
            if (!slotValueExists(slot, stringValue)) {
                //If all is well, execute change
                getSubject().setPropertyValue(slot, replacement);
            }
        }
    }

    /**
     * A wrapper method for replacing a slot value.
     * @see #replaceSlotValue(RDFProperty, Object, Object)
     *
     * @param oldValue an old value, cannot be <tt>null</tt>
     * @param newValue a new value, cannot be <tt>null</tt>
     */
     protected void replaceValue(RDFProperty slot, String oldValue, String newValue) {
        replaceSlotValue(slot, oldValue, newValue);
    }


    /**
     * Check if a value exists for a slot in a given instance list.
     *
     * @param property  RDF property to check
     * @param value value to be checked
     */
    @SuppressWarnings("unchecked")
    protected boolean slotValueExists(RDFProperty property, Object value) {
        if (value != null) {
            Collection<RDFResource> resources = getOWLModel().getRDFResourcesWithPropertyValue(property, value);
            //Check if slot value exists for the resource other than this one.
             for (RDFResource resource : resources) {
                if (!resource.getName().equalsIgnoreCase(getInstance().getName())) {
                    log.info("Value [" + value + "] already exists for property " +
                            "[" + property.getName() + "]" + " of resource [" + resource.getName() + "]");
                    return true;
                }

            }
        }
        return false;
    }


    /**
     * Wrapper for creating RDFSLiteral.
     */
    protected RDFSLiteral createLiteral(String value,  RDFSDatatype datatype){
        return getOWLModel().createRDFSLiteral(value, datatype);
    }

    /**
      * A wrapper for validating URI
     */
    protected static boolean isValidUri(String name){
        return ClassHierarchyURIWidget.isValidUriAndWithoutSegment(name);
    }

    /**
     * Check if a string starts with URI schemes
     *
     * @param name a name to check
     * @return true if a string contains http, https, file, ftp,
     */
    private boolean startsWithScheme(String name){
        return  name.startsWith("http") || name.startsWith("ftp")  ||
                name.startsWith("file");
    }

    /**
     * Get RDF resource
     */
    private RDFResource getSubject() {
        return (RDFResource) getInstance();
    }


    /**
     * Get default datatype for this widget
     */
    private RDFSDatatype getDefaultDatatype() {
        return getOWLModel().getXSDanyURI();
    }


    /**
     * Check if a value exists for a slot in a given instance list.
     * Using streams is faster for large collection.
     * For JDK >= 1.8
     */
     /*private boolean slotValueExists(Collection<Instance> instances, Slot slot, String value) {
        //Collection<Instance> instances = getKnowledgeBase().getDirectInstances(getInstance().getDirectType());
        return instances
                .parallelStream()
                .map(instance -> instance.getDirectOwnSlotValue(slot))
                .anyMatch(slotValue -> slotValue != null && slotValue.toString().equalsIgnoreCase(value));
     }*/


    /**
     * Display popup error window with a given message
     */
    private void showErrorMessage(String msg) {
        //Set red color font
        getTextField().setForeground(Color.RED);
        //Set red borders
        setInvalidValueBorder();
        //Display error message in a popup window
        ProtegeUI.getModalDialogFactory().showErrorMessageDialog(getOWLModel(), msg);
    }

}

