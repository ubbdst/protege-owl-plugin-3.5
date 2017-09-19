package edu.stanford.smi.protegex.owl.ui.components;

import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.ui.ProtegeUI;
import edu.stanford.smi.protegex.owl.ui.dialogs.DefaultSelectionDialogFactory;

import java.util.Collection;


/**
 * @author Hemed Al Ruwehy
 * <p>
 * 01-09-2017
 * University of Bergen Library
 */

public class MergeResourceWithBrowserTextAction extends AddResourceAction {

    private static final long serialVersionUID = -3841713833632888555L;
    private AddablePropertyValuesComponent component;

    public MergeResourceWithBrowserTextAction(AddablePropertyValuesComponent component, boolean symmetric) {
        super(component, symmetric);
        this.component = component;
    }

    @Override
    protected Collection selectResourcesByType(OWLModel owlModel, Collection clses) {
        return new DefaultSelectionDialogFactory().
                selectResourcesWithBrowserTextByType(component, owlModel, clses, "Select Resources..");

    }


    @Override
    public void resourceSelected(RDFResource resource) {
        if (isMergeConfirmed(resource)) {
            super.resourceSelected(resource);
        }
    }

    /**
     * Get subject (this is a selected instance in the instance browser hierarchy)
     */
    private RDFResource getSubject() {
        return component.getSubject();
    }


    private RDFProperty getPredicate() {
        return component.getPredicate();
    }


    /**
     * Shows a dialog with Yes/No options.
     *
     * @param source an instance to be merged to another instance
     */
    private boolean isMergeConfirmed(RDFResource source) {
        String text = "Are you sure you want to merge " + source.getBrowserText() + " into "
                + getSubject().getBrowserText() + "?";

        return ProtegeUI.getModalDialogFactory()
                .showConfirmDialog(source.getOWLModel(), text, "Confirm merge");
    }
}