package edu.stanford.smi.protegex.owl.ui.actions;


import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.resource.LocalizedText;
import edu.stanford.smi.protege.resource.ResourceKey;
import edu.stanford.smi.protege.util.*;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.ui.dialogs.ModalDialogFactory;
import edu.stanford.smi.protegex.owl.util.InstanceUtil;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Collection;


/**
 * Actions for either deleting or moving an instance to Trash.
 *
 * The idea is that, if instance belongs directly to class Trash, and deletion is triggered, then
 * delete the instance entirely. On the other hand, if an instance belongs to a class other than Trash,
 * and deletion is triggered, move the instance to class Trash.
 * This came as a request from the University of Bergen Library to avoid deletion mistakes.
 *
 * @author Hemed Al Ruwehy
 * 30-08-2017
 * University of Bergen Library
 */

public class DeleteInstanceOrMoveToTrashAction extends AllowableAction {
    private static final long serialVersionUID = -1874566858726067173L;

    public DeleteInstanceOrMoveToTrashAction(ResourceKey key, Selectable selectable) {
        super(key, selectable);
    }


    @Override
    public void actionPerformed(ActionEvent event) {
        if (isAllowed()) {
            WaitCursor waitCursor = new WaitCursor((JComponent)this.getSelectable());
            try {
                onDeleteOrMove();
            } finally {
                waitCursor.hide();
            }
        }
    }

    /**
     * Show modal dialog with Yes/No options
     */
    private boolean isDeleteConfirmed(String text) {
        int option = ModalDialog.showMessageDialog((JComponent)this.getSelectable(),
                text, "Confirm deletion",  ModalDialogFactory.MODE_YES_NO);
        return option == ModalDialogFactory.OPTION_YES;
    }


    /**
     * Show modal dialog with OK/Cancel options
     */
    private boolean isMoveToTrashConfirmed(Instance instance) {
        int option = ModalDialog.showMessageDialog((JComponent)this.getSelectable(),
                "The instance " +   "\"" + instance.getBrowserText() + "\"" + " will be moved to Trash", "Move to trash", ModalDialogFactory.MODE_OK_CANCEL);
        return option == ModalDialogFactory.OPTION_OK;
    }


    private boolean canDelete(Instance instance) {
        boolean var2 = true;
        if (instance instanceof Cls) {
            Cls var3 = (Cls) instance;
            int var4 = var3.getInstanceCount();
            var2 = var4 == 0;
            if (!var2) {
                String var5 = LocalizedText.getText(ResourceKey.DELETE_CLASS_FAILED_DIALOG_TEXT);
                ModalDialog.showMessageDialog((JComponent) this.getSelectable(), var5);
            }
        }
        return var2;
    }


    public void onDelete(Object var1) {
        Instance instance = (Instance) var1;
        if (canDelete(instance)) {
            if (isDeleteConfirmed("Are you sure you want to permanently delete instance " +
                    "\"" + instance.getBrowserText() + "\"?")) {
                onAboutToDelete(instance);
                deleteInstance(instance);
                onAfterDelete(instance);
            }
        }
    }

    /**
     * Moves instance to a specified class
     *
     * @param instance an instance to move
     * @param clazz a destination class
     */
    public static void moveInstance(Instance instance, OWLNamedClass clazz) {
        String targetClassName = clazz.getLocalName();
        instance.setDirectType(clazz);
        Log.getLogger().info("Instance " + instance.getName() + " moved to " + targetClassName);
    }


    /**
     * Deletes this instance from the model. This will first remove all references of the
     * resource to other resources and then destroy the object.
     */
    public static void deleteInstance(Instance instance) {
        instance.getKnowledgeBase().deleteFrame(instance);
        Log.getLogger().info("Instance " + instance.getName() + " has been deleted");
    }


    @Override
    public void onSelectionChange() {
        boolean isAllowed = true;
        for (Object selection : getSelection()) {
            Instance instance = (Instance) selection;
            if (!instance.isEditable()) {
                isAllowed = false;
                break;
            }
        }
        this.setAllowed(isAllowed);
    }


    /**
     * For each selected instance, decide whether to move to Trash or delete permanently
     */
    protected void onDeleteOrMove() {
        Collection selectedResources = getSelection();
        for (Object selection : selectedResources) {
            Instance selectedInstance = (Instance) selection;
            deleteOrMove(selectedInstance);
        }
    }


    /**
     * Get model for the given instance
     */
    private OWLModel getOWLModel(Instance instance){
        return (OWLModel)instance.getKnowledgeBase();
    }


    protected void onAboutToDelete(Object var1) { }

    protected void onAfterDelete(Object var1) { }


    /**
     * Delete or move instance based on which class it belongs.
     *
     * Idea: If instance has direct type of class Trash, and deletion is confirmed,
     * then delete permanently. Otherwise, move instance to Trash class.
     *
     * @param sourceInstance instance to move or delete
     */
    protected void deleteOrMove(Instance sourceInstance) {
        OWLNamedClass targetCls = InstanceUtil.getTrashClass(getOWLModel(sourceInstance));
        //If instance belongs to class Trash and deletion is confirmed,
        //then delete permanently
        if (sourceInstance.hasDirectType(targetCls)) {
            if (canDelete(sourceInstance)) {
                onDelete(sourceInstance);
            }
        } else {//Otherwise, move instance to class Trash
            if (sourceInstance instanceof Cls) {
                if (targetCls.isMetaclass()) {
                    if(isMoveToTrashConfirmed(sourceInstance)){
                        moveInstance(sourceInstance, targetCls);
                    }
                }
            } else if (!targetCls.isMetaclass()) {
                if(isMoveToTrashConfirmed(sourceInstance)) {
                    moveInstance(sourceInstance, targetCls);
                }
            }
        }
    }
}
