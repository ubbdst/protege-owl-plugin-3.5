package edu.stanford.smi.protegex.owl.ui.widget;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.widget.TextFieldWidget;
import edu.stanford.smi.protegex.owl.model.*;
import edu.stanford.smi.protegex.owl.util.InstanceUtil;
import org.apache.commons.validator.routines.UrlValidator;

import java.util.Collection;
import java.util.logging.Logger;

/**
 * A widget to generate class hierarchy URI for the individuals upon creation.
 *
 * @author Hemed Al Ruwehy
 * @author Øyvind Gjesdal
 *
 * The University of Bergen Library
 * 2017-04-10
 */
public class ClassHierarchyURIWidget extends TextFieldWidget {
    private static String PATH_SEPARATOR = "/";
    private static transient Logger log = Log.getLogger(ClassHierarchyURIWidget.class);


    /*
      This widget will show up only if the range defined in the given slot is of datatype xsd:anyURI.
     */
    /*public static boolean isSuitable(Cls cls, Slot slot, Facet facet) {
        if(slot instanceof RDFProperty){
            RDFProperty classHierarchySlot = (RDFProperty)slot;
            if(classHierarchySlot.isRangeDefined()) {
                if(classHierarchySlot.getRangeDatatype().equals(classHierarchySlot.getOWLModel().getXSDanyURI())) {
                    return true;
                }

            }
        }
        return false;
    }*/

    public static boolean isSuitable(Class clazz, Cls cls, Slot slot) {
       return OWLWidgetMapper.isSuitable(clazz, cls, slot);
    }


    /**
     * Check for URI validity without fragment
     *
     * @param name a name to be checked for validity
     */
    public static boolean isValidUriAndWithoutSegment(String name) {
        String[] schemes = {"http", "https"};
        UrlValidator urlValidator = new UrlValidator(schemes, UrlValidator.NO_FRAGMENTS);
        //UrlValidator urlValidator = new UrlValidator(schemes);
        return urlValidator.isValid(name);

    }

    @Override
    public void initialize() {
        super.initialize(false, 4, 1);
    }

    /**
     * Get OWL model
     */
    public OWLModel getOWLModel() {
        return (OWLModel) getKnowledgeBase();
    }


    /**
     * Method for setting values of the property where this widget is assigned.
     * This method is called during creation of the instance also, when instance browser
     * is refreshed for example by navigating to another instance via UI.
     *
     * @param values values that need to be assigned to the property
     */
    @Override
    public void setValues(Collection values) {
        String savedValue = (String) CollectionUtilities.getFirstItem(values);
        if (savedValue == null) {
            //Create class URI
            String classURI = constructClassHierarchyURI();
            //Show it to user UI
            setText(classURI);
            //Convert it to XSD:anyURI data type
            Object value = createXsdAnyURILiteral(classURI);
            //Assign new property value to this instance
            assignPropertyValueToInstance(value);
        } else {
            super.setValues(values);
        }
        //Disable this widget from being edited by user
        getTextField().setEnabled(false);
    }


    @Override
    public void setText(String s) {
        String plainText = InstanceUtil.stripDatatype(s);
        super.setText(plainText);
    }


    /**
     * Assign slot value to a corresponding individual.
     */
    private void assignPropertyValueToInstance(Object newValue) {
        if (newValue != null) {
            Collection oldValues = getSubject().getPropertyValues(getPredicate(), true);
            if (!oldValues.contains(newValue)) {
                getSubject().setPropertyValue(getPredicate(), newValue);
            }
        }

    }

    /**
     * Get RDF resource of this slot
     */
    private RDFResource getSubject() {
        return (RDFResource) getInstance();
    }

    /**
     * Get RDF property (this slot)
     */
    private RDFProperty getPredicate() {
        return getOWLModel().getRDFProperty(UBBOntologyNamespaces.CLASS_HIERARCHY_URI);
    }

    /**
     * Get default datatype for this widget
     */
    private RDFSDatatype getXsdAnyURI() {
        return getOWLModel().getXSDanyURI();
    }

    /**
     * Build URI for the class hierarchy
     */
    private String constructClassHierarchyURI() {
        String prefix = InstanceUtil.getClassURIPrefix(getInstance());
        return (prefix + InstanceUtil.encodeUrl(getId()));
    }

    /**
     * Get identifier. Check identifier slot if it has a value, otherwise return instance UUID.
     */
    private String getId() {
        //Process identifier slot
        Instance instance = this.getInstance();
        Slot identifierSlot = getKnowledgeBase().getSlot(UBBOntologyNamespaces.IDENTIFIER);
        if (identifierSlot != null) {
            Object slotValue = instance.getDirectOwnSlotValue(identifierSlot);
            if (slotValue != null) {
                return slotValue.toString();
            }
        }
        return UUIDWidget.getUUIDFromInstanceURI(instance);
    }


    /**
     * Create literal with XSD:anyURI datatype (which is default for this widget)
     */
    public RDFSLiteral createXsdAnyURILiteral(String text){
        return getOWLModel().createRDFSLiteral(text, getXsdAnyURI());

    }

}
