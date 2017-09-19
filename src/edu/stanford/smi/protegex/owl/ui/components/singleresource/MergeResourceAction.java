package edu.stanford.smi.protegex.owl.ui.components.singleresource;


import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.model.UBBOntologyNamespaces;
import edu.stanford.smi.protegex.owl.ui.ProtegeUI;
import edu.stanford.smi.protegex.owl.ui.components.PropertyValuesComponent;
import edu.stanford.smi.protegex.owl.ui.icons.OWLIcons;
import edu.stanford.smi.protegex.owl.util.InstanceUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import static edu.stanford.smi.protegex.owl.util.InstanceUtil.getTrashClass;

/**
 * A class that performs merge action
 *
 * @author Hemed Al Ruwehy
 * <p>
 * Universitetsbiblioteket i Bergen
 * 04.09.2017
 */
public class MergeResourceAction extends SetResourceAction {

    private PropertyValuesComponent component;

    public MergeResourceAction(PropertyValuesComponent component) {
        super("Select resource to merge", OWLIcons.getAddIcon(OWLIcons.MERGE_INDIVIDUAL_ICON), component);
        this.component = component;
    }

    /**method@2x.png
     * Moves a given instance to class Trash
     */
    private static void moveToTrash(RDFResource resource) {
        resource.setProtegeType(getTrashClass(resource.getOWLModel()));
    }

    /**
     * Checks if a given collection has values
     */
    public static boolean hasValues(Collection collection) {
        return collection != null && !collection.isEmpty();
    }

    /**
     * Deletes a given resource
     */
    public static void deleteResource(RDFResource resource) {
        resource.delete();
    }

    @Override
    public void resourceSelected(RDFResource resource) {
        String text = "Are you sure you want to merge \"" + resource.getBrowserText() + "\" into \""
                + getSubject().getBrowserText() + "\"?";

        if (isMergeConfirmed(resource, text)) {
            onMerge(resource);
            Log.getLogger().info("Instance " + resource.getName() + " merged into " + getSubject().getName());
            showMessageDialog(resource, "Merging was successful and the merged instance has been moved to Trash");
        }
    }

    /**
     * Performs merge, step by step.
     *
     * @param resource a resource to be merged
     */
    public void onMerge(RDFResource resource) {
        beforeMerge(resource);
        doMerge(resource);
        afterMerge(resource);
    }

    /**
     * Pre-validates instance before merge and provide useful message to the user
     *
     * @param resource a resource to be merged
     */
    public void beforeMerge(RDFResource resource) {
        if (resource.equals(getSubject())) {
            showMessageDialog(resource, "Instance cannot be merged to itself");
            throw new UnsupportedOperationException("Cannot merge instance to itself: [" + resource.getName() + "]");
        } else if (!resource.getProtegeType().equals(getSubject().getProtegeType())) {
            showMessageDialog(resource, "Cannot merge instances that belong to different classes");
            throw new UnsupportedOperationException("Cannot merge instances from different classes " +
                    "[" + resource.getName() + " , " + getSubject().getName() + "]");
        }
    }

    protected void doMerge(RDFResource resource) {
        copyInstanceReferences(resource);
        copyInstanceValues(resource);
    }

    /*
         TODO: After merge, do the following:-
         a) Do not delete object, rather move it to class trash and give that information to user
            via Dialog box.
         b) Copy resource UUID and identifier to previousIdentifier of the target resource,
            in the form of "uuid:76547646" and "identifier:ubb-ms-0001"
         c) Copy resource URI to ubbont:previousURI of the target resource
    */
    protected void afterMerge(RDFResource resource) {
        copyPreviousIdentifiers(resource);
        moveToTrash(resource);
        assignPropertyValue(getPredicate(), resource);
    }


    /**
     * Copies previous UUID, identifier and URI to the new resource
     *
     * @param resource a resource by which its identifiers need to be copied
     */
    private void copyPreviousIdentifiers(RDFResource resource) {
        Object uuid = resource.getPropertyValue(getProperty(UBBOntologyNamespaces.UUID));
        Object signature = resource.getPropertyValue(getProperty(UBBOntologyNamespaces.IDENTIFIER));
        Object uri = resource.getName();

        getSubject().addPropertyValue(getProperty(UBBOntologyNamespaces.HAS_BEEN_MERGED_WITH), uri);

        if(uuid != null) {
            getSubject().addPropertyValue(getProperty(UBBOntologyNamespaces.PREVIOUS_IDENTIFIER),
                    "uuid:" + uuid);
        }
        if(signature != null) {
            getSubject().addPropertyValue(getProperty(UBBOntologyNamespaces.PREVIOUS_IDENTIFIER),
                    "identifier:" + signature);
        }
    }


    /**
     * Makes all individuals that had a given resource as an object, have a new merged resource as an object
     * for that property.
     * Since we move the resource to Trash after merging, we don't have to remove the old link.
     * It will be automatically removed when the resource is deleted from Trash.
     */
    private void copyInstanceReferences(RDFResource resource) {
        Map<RDFResource, RDFProperty> references = InstanceUtil.getInstanceReferences(resource);
        for(Map.Entry<RDFResource, RDFProperty> entry : references.entrySet()){
            RDFResource referenceInstance = entry.getKey();
            RDFProperty referenceProperty = entry.getValue();
            //Replace the the merged resource to the new resource if the property does not allow more than two values
            if(referenceProperty.isFunctional() && referenceInstance.getPropertyValues(referenceProperty).contains(resource)) {
                referenceInstance.setPropertyValue(referenceProperty, getSubject());
            }
            else {//Otherwise, keep the merged resource and just add new resource to the list
                referenceInstance.addPropertyValue(referenceProperty, getSubject());
            }
        }
    }


    /**
     * Assigns a given object as a value to a property where this action takes place.
     */
    private void assignPropertyValue(RDFProperty property, Object value) {
        //If its functional, override any existing value
        if(getPredicate().isFunctional()) {
            getSubject().setPropertyValue(property, value);
        }
        else {//Otherwise, append it to the list
            getSubject().addPropertyValue(property, value);
        }
    }


    /**
     * Copies all values from given resource to a target resource, except values for properties {@code ct:identifier},
     * {@code ubbont:uuid} and {@code ubbont:classHierarchyURI}.
     *
     * If a predicate is of functional property, and there exist values for such property in the target resource,
     * do not copy, user must decide which value to pick manually.
     *
     * @param resource a resource in which values of its properties need to be copied
     */
    @SuppressWarnings("unchecked")
    private void copyInstanceValues(RDFResource resource) {
        Collection<RDFProperty> properties = resource.getRDFProperties();
        for (RDFProperty property : properties) {
            if (!property.getName().equals(UBBOntologyNamespaces.UUID) &&
                    /*!property.equals(getOWLModel().getRDFTypeProperty()) &&*/
                    !property.getName().equals(UBBOntologyNamespaces.IDENTIFIER) &&
                    !property.getName().equals(UBBOntologyNamespaces.CLASS_HIERARCHY_URI)) {

                Collection existingValues = getSubject().getPropertyValues(property);
                Collection newValues = resource.getPropertyValues(property);
                    /*
                     Skip the iteration if we meet a functional property and
                     there exist values for such property in the target resource.
                     Otherwise, we wont be able to chose which value to keep.
                     */
                if (property.isFunctional() && hasValues(existingValues)) {
                    continue;
                }
                Collection combinedValues = new ArrayList();
                if (hasValues(existingValues)) {
                    combinedValues.addAll(existingValues);
                }
                if (hasValues(newValues)) {
                    combinedValues.addAll(newValues);
                }
                if (hasValues(combinedValues)) {
                    //Remove duplicates and assign values to the target resource
                    getSubject().setPropertyValues(property, new HashSet(combinedValues));
                }
            }
        }

    }

    /**
     * Gets subject where this action takes place
     */
    protected RDFResource getSubject() {
        return component.getSubject();
    }


    protected boolean isDeleteConfirmed(RDFResource resource, String text) {
        return ProtegeUI.getModalDialogFactory().showConfirmDialog(resource.getOWLModel(), text, "Confirm deletion");
    }


    private RDFProperty getProperty(String s) {
        return getOWLModel().getRDFProperty(s);
    }

    protected OWLModel getOWLModel() {
        return getSubject().getOWLModel();
    }

    /**
     * Gets the property where this action takes place
     */
    protected RDFProperty getPredicate() {
        return component.getPredicate();
    }


    private void showMessageDialog(RDFResource source, String text) {
        ProtegeUI.getModalDialogFactory().showMessageDialog(source.getOWLModel(), text);
    }

    /**
     * Shows a dialog with Yes/No options.
     *
     * @param source an instance to be merged
     */
    private boolean isMergeConfirmed(RDFResource source, String text) {
        return ProtegeUI.getModalDialogFactory().showConfirmDialog(source.getOWLModel(), text, "Confirm merge");
    }
}

