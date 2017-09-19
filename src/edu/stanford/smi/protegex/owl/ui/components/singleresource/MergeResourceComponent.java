package edu.stanford.smi.protegex.owl.ui.components.singleresource;


import edu.stanford.smi.protege.ui.FrameRenderer;
import edu.stanford.smi.protege.util.*;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.model.event.PropertyValueAdapter;
import edu.stanford.smi.protegex.owl.model.event.PropertyValueListener;
import edu.stanford.smi.protegex.owl.ui.OWLLabeledComponent;
import edu.stanford.smi.protegex.owl.ui.actions.ResourceActionManager;
import edu.stanford.smi.protegex.owl.ui.components.AbstractPropertyValuesComponent;
import edu.stanford.smi.protegex.owl.ui.icons.OWLIcons;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Collection;

/**
 * @author Holger Knublauch  <holger@knublauch.com>
 * @author Hemed Al Ruwehy
 * <p>
 * 04.09.2017
 * Universitetsbiblioteket i Bergen
 */
public class MergeResourceComponent extends AbstractPropertyValuesComponent implements Disposable {

    private RDFResource resource;
    private JList list;
    private Action mergeAction;
    private Action removeAction;


    private PropertyValueListener browserTextListener = new PropertyValueAdapter() {
        @Override
        public void browserTextChanged(RDFResource resource) {
            list.repaint();
        }
    };

    public MergeResourceComponent(RDFProperty predicate) {
        this(predicate, null);
    }

    public MergeResourceComponent(RDFProperty predicate, String label) {
        this(predicate, label, false);
    }

    public MergeResourceComponent(RDFProperty predicate, String label, boolean isReadOnly) {
        super(predicate, label, isReadOnly);
        list = ComponentFactory.createSingleItemList(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                handleDoubleClick();
            }
        });
        list.setCellRenderer(FrameRenderer.createInstance());
        list.addMouseListener(new PopupMenuMouseListener(list) {
            @Override
            protected JPopupMenu getPopupMenu() {
                return createPopupMenu();
            }

            @Override
            protected void setSelection(JComponent c, int x, int y) {
            }
        });
        OWLLabeledComponent lc = new OWLLabeledComponent((label == null ? getLabel() : label), list);

        mergeAction = createMergeAction();
        if (mergeAction != null) {
            lc.addHeaderButton(mergeAction);
        }
        removeAction = createRemoveAction();
        if (removeAction != null) {
            lc.addHeaderButton(removeAction);
        }
        add(BorderLayout.CENTER, lc);
    }


    /**
     * Create merge action
     */
    protected Action createMergeAction() {
        mergeAction = new MergeResourceAction(this);
        return mergeAction;
    }


    /**
     * Create remove action
     */
    protected Action createRemoveAction() {
        removeAction = new AbstractAction("Remove current value", OWLIcons.getRemoveIcon(OWLIcons.RDF_INDIVIDUAL)) {
            public void actionPerformed(ActionEvent e) {
                handleRemove();
            }
        };
        return removeAction;
    }


    public void dispose() {
        removeBrowserTextListener();
    }


    public RDFResource getResource() {
        return resource;
    }


    protected void handleDoubleClick() {
        if (resource != null) {
            getOWLModel().getProject().show(resource);
        }
    }


    protected void handleRemove() {
        getSubject().setPropertyValue(getPredicate(), null);
    }


    public boolean isRemoveEnabled() {
        if (!hasHasValueRestriction()) {
            return getObject() != null && hasOnlyEditableValues();
        } else {
            return false;
        }
    }

    public boolean isSetEnabled() {
        return !hasHasValueRestriction() && hasOnlyEditableValues();
    }


    private void removeBrowserTextListener() {
        if (resource != null) {
            resource.removePropertyValueListener(browserTextListener);
        }
    }


    private void updateActions() {
        boolean isEditable = !isReadOnly();
        //createAction.setEnabled(isEditable && isCreateEnabled());
        mergeAction.setEnabled(isEditable && isSetEnabled());
        removeAction.setEnabled(isEditable && isRemoveEnabled());
    }


    private void updateList() {
        ComponentUtilities.setListValues(list, CollectionUtilities.createCollection(resource));
    }

    protected JPopupMenu createPopupMenu() {
        if (resource != null) {
            JPopupMenu menu = new JPopupMenu();
            ResourceActionManager.addResourceActions(menu, this, resource);
            if (menu.getComponentCount() > 0) {
                return menu;
            }
        }
        return null;
    }

    @Override
    public void valuesChanged() {
        removeBrowserTextListener();
        Object value = getSubject().getPropertyValue(getPredicate(), true);
        if (value == null) {
            Collection hs = getSubject().getHasValuesOnTypes(getPredicate());
            if (!hs.isEmpty()) {
                value = hs.iterator().next();
            }
        }
        if (value instanceof RDFResource) {
            resource = (RDFResource) value;
            resource.addPropertyValueListener(browserTextListener);
        } else {
            resource = null;
        }
        updateActions();
        updateList();
    }
}
