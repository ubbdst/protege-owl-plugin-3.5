/*
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License");  you may not use this file except in 
 * compliance with the License.  You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 *
 * The Original Code is Protege-2000.
 *
 * The Initial Developer of the Original Code is Stanford University. Portions
 * created by Stanford University are Copyright (C) 2007.  All Rights Reserved.
 *
 * Protege was developed by Stanford Medical Informatics
 * (http://www.smi.stanford.edu) at the Stanford University School of Medicine
 * with support from the National Library of Medicine, the National Science
 * Foundation, and the Defense Advanced Research Projects Agency.  Current
 * information about Protege can be obtained at http://protege.stanford.edu.
 *
 */

package edu.stanford.smi.protegex.owl.ui.components.literaltable;

import edu.stanford.smi.protegex.owl.model.*;
import edu.stanford.smi.protegex.owl.model.impl.DefaultRDFSLiteral;
import edu.stanford.smi.protegex.owl.model.triplestore.TripleStoreModel;
import edu.stanford.smi.protegex.owl.ui.OWLLabeledComponent;
import edu.stanford.smi.protegex.owl.ui.ProtegeUI;
import edu.stanford.smi.protegex.owl.ui.components.AddResourceAction;
import edu.stanford.smi.protegex.owl.ui.components.AddablePropertyValuesComponent;
import edu.stanford.smi.protegex.owl.ui.editors.PropertyValueEditor;
import edu.stanford.smi.protegex.owl.ui.editors.PropertyValueEditorManager;
import edu.stanford.smi.protegex.owl.ui.icons.OWLIcons;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.Iterator;

/**
 * @author Holger Knublauch  <holger@knublauch.com>
 */
public class LiteralTableComponent extends AddablePropertyValuesComponent {


    private Action addAction = new AbstractAction("Add new value", OWLIcons.getAddIcon()) {
        public void actionPerformed(ActionEvent e) {
            handleAddAction();
        }
    };


    private Action deleteAction = new AbstractAction("Delete selected value", OWLIcons.getDeleteIcon()) {
        public void actionPerformed(ActionEvent e) {
            handleDeleteAction();
        }
    };

    private LiteralTable table;


    private Action viewAction = new AbstractAction("View/edit selected value...", OWLIcons.getViewIcon()) {
        public void actionPerformed(ActionEvent e) {
            handleViewAction();
        }

    };


    public LiteralTableComponent(RDFProperty predicate) {
    	this(predicate, null);
    }

    public LiteralTableComponent(RDFProperty predicate, String label) {
        super(predicate, label, false);
    }
    
    public LiteralTableComponent(RDFProperty predicate, String label, boolean isReadOnly) {
        super(predicate, label, isReadOnly);
        table = new LiteralTable(predicate);
        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                updateActionsState();
            }
        });
        table.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    handleDoubleClick();
                }
            }
        });
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(Color.white);
        OWLLabeledComponent lc = new OWLLabeledComponent((label == null ? getLabel():label), scrollPane);
        lc.addHeaderButton(viewAction);
        lc.addHeaderButton(addAction);
        if (!(predicate instanceof OWLProperty) && predicate.getRange() == null) {
            lc.addHeaderButton(new AddResourceAction(this, false));
        }
        lc.addHeaderButton(deleteAction);
        add(lc);
        updateActionsState();
    }


    private Object createDefaultValue() {
        RDFProperty property = getPredicate();
        Iterator it = PropertyValueEditorManager.listEditors();
        while (it.hasNext()) {
            PropertyValueEditor plugin = (PropertyValueEditor) it.next();
            if (plugin.canEdit(getSubject(), property, null)) {
                Object defaultValue = plugin.createDefaultValue(getSubject(), property);
                if (defaultValue != null) {
                    return defaultValue;
                }
            }
        }
        RDFResource range = property.getRange();
        if (range instanceof RDFSDatatype) {
            return ((RDFSDatatype) range).getDefaultValue();
        }
        else if (range instanceof OWLDataRange) {
            OWLDataRange dataRange = (OWLDataRange) range;
            RDFList oneOf = dataRange.getOneOf();
            Object defaultValue = null;
            if (oneOf != null) {
                RDFResource subj = getSubject();
                RDFProperty pred = getPredicate();
                Iterator values = oneOf.getValues().iterator();
                while (values.hasNext() && defaultValue == null) {
                    Object value = values.next();
                    if (!subj.hasPropertyValue(pred, value)){
                        defaultValue = value;
                    }
                }
                if (defaultValue == null){
                    ProtegeUI.getModalDialogFactory().showMessageDialog(getOWLModel(),
                                                                        "You have already assigned all allowed values");
                }
            }
            return defaultValue;
        }
        else if (property instanceof OWLObjectProperty) {
            return null;
        }
        else {
            return "";
        }
    }


    private void removeEmptyStringIfExists(){
        if(getObjects().contains("")) {
            getSubject().removePropertyValue(getPredicate(), "");
        }
    }
     private void handleAddAction() {
        //There was an issue when empty string exists, the widget would  not respond
        removeEmptyStringIfExists();
        Object defaultValue = createDefaultValue();
        if (defaultValue != null && !getObjects().contains(defaultValue)) {
            getSubject().addPropertyValue(getPredicate(), defaultValue);
            table.setSelectedRow(defaultValue);

            if (defaultValue instanceof DefaultRDFSLiteral && ((DefaultRDFSLiteral)defaultValue)
                    .getDatatype().equals(getOWLModel().getXSDstring())) {
                table.editCell(defaultValue);
                return;
            }
            if (!defaultValue.equals("")) {
                final Iterator it = PropertyValueEditorManager.listEditors();
                while (it.hasNext()) {
                    PropertyValueEditor editor = (PropertyValueEditor) it.next();
                    if (editor.canEdit(getSubject(), getPredicate(), null)) {
                        Object newValue = editor.editValue(this, getSubject(), getPredicate(), defaultValue);
                        if (newValue != null) {
                            getSubject().removePropertyValue(getPredicate(), defaultValue);
                            getSubject().addPropertyValue(getPredicate(), newValue);
                            table.setSelectedRow(newValue);
                        }
                        return;
                    }
                }
            }
            table.editCell(defaultValue);
        }
    }


    /**
     * This is a modified version of the method such that, editor panel is brought
     * forward when creating value. The method was experimental.
     */
    /*private void handleAddAction() {
        Object defaultValue = createDefaultValue();
        if (defaultValue != null && !getObjects().contains(defaultValue)) {
            //Add this to show the position of the cursor
            getSubject().addPropertyValue(getPredicate(), defaultValue);
            table.setSelectedRow(defaultValue);

            if (!defaultValue.equals("")) {
                //By default, bring up the editor panel.
                final Iterator it = PropertyValueEditorManager.listEditors();
                while (it.hasNext()) {
                    PropertyValueEditor editor = (PropertyValueEditor) it.next();
                    if (editor.canEdit(getSubject(), getPredicate(), null)) {
                        Object newValue = editor.editValue(this, getSubject(), getPredicate(), defaultValue);
                        if (newValue != null && !newValue.equals(defaultValue)) {
                            getSubject().addPropertyValue(getPredicate(), newValue);
                            table.setSelectedRow(newValue);
                        }
                        //Remove cursor position from the cell
                        getSubject().removePropertyValue(getPredicate(), defaultValue);
                        return;
                    }
                }
            }
            table.editCell(defaultValue);
        }
    }*/


    private void handleDeleteAction() {
        Collection values = getOWLModel().asRDFSLiterals(getObjects());
        int[] sels = table.getSelectedRows();
        for (int i = 0; i < sels.length; i++) {
            int row = sels[i];
            if (row >= 0 && row < table.getTableModel().getRowCount()) {
                Object value = table.getTableModel().getObject(row);
                values.remove(value);
            }
        }
        getSubject().setPropertyValues(getPredicate(), values);
    }


    protected void handleDoubleClick() {
        int[] sel = table.getSelectedRows();
        if (sel.length == 1 && viewAction.isEnabled()) {
            if (!table.getTableModel().isCellEditable(sel[0], LiteralTableModel.COL_VALUE)) {
                handleViewAction();
            }
        }
    }


    protected void handleViewAction() {
        int[] sel = table.getSelectedRows();
        if (sel.length > 0) {
            table.stopEditing();
            Object oldValue = table.getTableModel().getObject(sel[0]);
            if (oldValue instanceof RDFResource) {
                RDFResource resource = (RDFResource) oldValue;
                resource.getProject().show(resource);
            }
            else {
                PropertyValueEditor editor = getEditor(oldValue);
                if (editor != null) {
                    Object newValue = editor.editValue(null, getSubject(), getPredicate(), oldValue);
                    if (newValue != null) {
                        /*
                        //Unescape HTML for this value, which is coming from HTMEditorPanel
                        if(newValue instanceof String) {
                            newValue = StringEscapeUtils.unescapeHtml((String)newValue);
                        }
                        else if (newValue instanceof RDFSLiteral){
                            String lang = ((RDFSLiteral) newValue).getLanguage();
                            String plainValue =  StringEscapeUtils.unescapeHtml(((RDFSLiteral) newValue).getString());
                            newValue = getOWLModel().createRDFSLiteral(StringEscapeUtils.unescapeHtml(plainValue, lang);
                        }
                        */
                        if (oldValue instanceof RDFSLiteral) {
                            Object neo = ((RDFSLiteral) oldValue).getPlainValue();
                            if (neo != null) {
                                oldValue = neo;
                            }
                        }
                        getSubject().removePropertyValue(getPredicate(), oldValue);
                        getSubject().addPropertyValue(getPredicate(), newValue);
                        table.setSelectedRow(newValue);
                    }
                }
            }
        }
    }


    public void setSubject(RDFResource subject) {
        super.setSubject(subject);
        table.setSubject(subject);
    }


    private void updateActionsState() {
        int[] sel = table.getSelectedRows();
        boolean deleteEnabled = !isReadOnly() && table.getTableModel().isDeleteEnabled(sel);
        deleteAction.setEnabled(deleteEnabled);

        if (isReadOnly()) {
        	viewAction.setEnabled(false);
        } else {        
	        boolean viewEnabled = false;
	        if (sel.length == 1) {
	            Object object = table.getTableModel().getObject(sel[0]);
	            if (object instanceof RDFResource) {
	                viewEnabled = true;
	            }
	            else {
	                if (getSubject().getAllValuesFromOnTypes(getPredicate()) instanceof OWLDataRange) {
	                    viewEnabled = false;
	                }
	                else {
	                    PropertyValueEditor editor = getEditor(object);
	                    if (editor != null) {
	                    	TripleStoreModel tsm = getOWLModel().getTripleStoreModel();
	                        viewEnabled = tsm.isActiveTriple(getSubject(), getPredicate(), object);
	                    }
	                }
	            }
	        }
	        viewAction.setEnabled(viewEnabled);
        }
        
        addAction.setEnabled(!isReadOnly());        
    }


    public void valuesChanged() {
        table.getTableModel().updateValues();
    }
}