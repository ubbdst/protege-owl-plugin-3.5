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
import edu.stanford.smi.protegex.owl.ui.components.ComponentUtil;
import edu.stanford.smi.protegex.owl.ui.editors.PropertyValueEditor;
import edu.stanford.smi.protegex.owl.ui.editors.PropertyValueEditorManager;

import javax.swing.table.AbstractTableModel;
import java.util.*;

import static edu.stanford.smi.protegex.owl.util.InstanceUtil.isValidXSDLanguage;

/**
 * A TableModel managing a list of RDFSLiterals which are predicate values of
 * a given subject/predicate pair.  This is used by the corresponding widget.
 * The TableModel has one row for each literal, and one column for the value
 * (which is entered as string), and one for the type of each value.  The latter
 * can optionally be switched off, if the predicate has a fixed range.
 *
 * @author Holger Knublauch  <holger@knublauch.com>
 */
public class LiteralTableModel extends AbstractTableModel {

    public final static int COL_VALUE = 0;

    public final static int COL_LANG = 1;

    public final static int COL_TYPE = 1;

    public final static int COL_COUNT = 2;

    private OWLModel owlModel;

    private RDFProperty predicate;

    private Boolean stringProperty = null;

    private RDFResource subject;

    private List values = Collections.EMPTY_LIST;


    public LiteralTableModel(RDFProperty predicate) {
        this.owlModel = predicate.getOWLModel();
        this.predicate = predicate;
    }


    public int getColumnCount() {
        return COL_COUNT;
    }


    public Class getColumnClass(int columnIndex) {
        if (columnIndex == COL_VALUE) {
            return Object.class;
        }
        else {
            if (isStringProperty()) {
                return String.class;
            }
            else {
                return RDFSDatatype.class;
            }
        }
    }


    public String getColumnName(int column) {
        if (column == COL_VALUE) {
            return "Value";
        }
        else {
            if (isStringProperty()) {
                return "Lang";
            }
            else {
                return "Type";
            }
        }
    }


    protected PropertyValueEditor getEditor(Object value) {
        return PropertyValueEditorManager.getEditor(subject, predicate, value);
    }


    private Collection getNewValues(int rowOfChangedValue) {
        List newValues = new ArrayList();
        for (int i = 0; i < values.size(); i++) {
            Object value = values.get(i);
            if (value instanceof RDFSLiteral) {
                Object simpleValue = ((RDFSLiteral) value).getPlainValue();
                if (simpleValue != null) {
                    value = simpleValue;
                }
            }
            if (i == rowOfChangedValue || (i != rowOfChangedValue && subject.hasPropertyValue(predicate, value))) {
                newValues.add(value);
            }
        }
        return newValues;
    }


    public Object getObject(int row) {
        return values.get(row);
    }


    public RDFProperty getPredicate() {
        return predicate;
    }


    private RDFSLiteral getRDFSLiteral(int rowIndex) {
        return (RDFSLiteral) values.get(rowIndex);
    }


    public int getRow(Object value) {
        if (!(value instanceof RDFSLiteral)) {
            value = owlModel.createRDFSLiteral(value);
        }
        return values.indexOf(value);
    }


    public int getRowCount() {
        return values.size();
    }


    public RDFResource getSubject() {
        return subject;
    }


    public Object getValueAt(int rowIndex, int columnIndex) {
        if (columnIndex == COL_VALUE) {
            Object value = getObject(rowIndex);
            if (value instanceof RDFResource) {
                return value; // ((RDFResource) value).getBrowserText();
            }
            else {
                RDFSLiteral literal = getRDFSLiteral(rowIndex);
                return literal.toString().trim();
            }
        }
        else {
            if (isStringProperty()) {
                RDFSLiteral literal = getRDFSLiteral(rowIndex);
                return literal.getLanguage();
            }
            else {
                Object value = getObject(rowIndex);
                if (value instanceof RDFResource) {
                    return null;
                }
                else {
                    RDFSLiteral literal = getRDFSLiteral(rowIndex);
                    return literal.getDatatype();
                }
            }
        }
    }


    public boolean isCellEditable(int rowIndex, int columnIndex) {
        if (getObject(rowIndex) instanceof RDFResource) {
            return false;
        }
        RDFSLiteral literal = getRDFSLiteral(rowIndex);
        Object v = literal.getPlainValue();
        if (v == null) {
            v = literal;
        }
        boolean active = owlModel.getTripleStoreModel().isActiveTriple(subject, predicate, v);
        if (active) {
            if (columnIndex == COL_VALUE) {
                if (active) {
                    RDFSLiteral value = getRDFSLiteral(rowIndex);
                    PropertyValueEditor editor = getEditor(value);
                    if (editor != null && editor.mustEdit(subject, predicate, value)) {
                        return false;
                    }
                    else {
                        return true;
                    }
                }
            }
            else {
                if (isStringProperty()) {
                    return true;
                }
                else {
                    return !ComponentUtil.isRangeDefined(subject, predicate);
                }
            }
        }
        return false;
    }


    public boolean isDeleteEnabled(int[] selectedRows) {
        TripleStoreModel tsm = owlModel.getTripleStoreModel();
        boolean deleteEnabled = false;
        if (selectedRows.length > 0) {
            deleteEnabled = true;
            for (int i = 0; i < selectedRows.length; i++) {
                int index = selectedRows[i];
                Object object = getObject(index);
                if (!tsm.isEditableTriple(subject, predicate, object)) {
                    deleteEnabled = false;
                }
            }
        }
        return deleteEnabled;
    }


    public boolean isStringProperty() {
        if (stringProperty == null) {
            stringProperty = new Boolean(isStringPropertyHelper());
        }
        return stringProperty.booleanValue();
    }


    private boolean isStringPropertyHelper() {
        final RDFSDatatype datatype = owlModel.getXSDstring();
        if (subject != null) {
            Iterator types = subject.listRDFTypes();
            while (types.hasNext()) {
                RDFSClass type = (RDFSClass) types.next();
                if (type instanceof OWLNamedClass) {
                    OWLNamedClass cls = (OWLNamedClass) type;
                    if (datatype.equals(cls.getAllValuesFrom(predicate))) {
                        return true;
                    }
                }
            }
        }
        return datatype.equals(predicate.getRange());
    }


    public void setSubject(RDFResource subject) {
        this.subject = subject;
        stringProperty = null;
        updateValues();
    }


    private void setTypeAt(Object aValue, int rowIndex) {
        RDFSDatatype datatype = (RDFSDatatype) aValue;
        if (!getRDFSLiteral(rowIndex).getDatatype().equals(datatype)) {
            String lexicalValue = ""; // oldLiteral.toString();
            RDFSLiteral newValue = owlModel.createRDFSLiteral(lexicalValue, datatype);
            PropertyValueEditor editor = getEditor(newValue);
            if (editor != null) {
                Object newLiteral = editor.createDefaultValue(subject, predicate);
                if (newLiteral instanceof RDFSLiteral) {
                    newValue = (RDFSLiteral) newLiteral;
                }
            }
            values.set(rowIndex, newValue);
            subject.setPropertyValues(predicate, getNewValues(rowIndex));  // Will fire back events etc
        }
    }


    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        RDFSLiteral oldLiteral = getRDFSLiteral(rowIndex);
        if (columnIndex == COL_VALUE) {
            //Do not allow empty value to be saved.
            //Side effect: by adding this, users will not be able to put language tag first before the actual value.
            /*if(aValue == null || aValue.toString().trim().isEmpty()){
                subject.removePropertyValue(predicate, aValue);
                return;
            }*/

            if (aValue instanceof RDFSLiteral) {
                values.set(rowIndex, aValue.toString().trim());
            }
            else {
                final String lexicalValue = ((String) aValue).trim();
                RDFSLiteral newValue;
                RDFSDatatype datatype = oldLiteral.getDatatype();

                if (datatype.equals(owlModel.getXSDstring())) {
                	newValue = DefaultRDFSLiteral.create(owlModel, lexicalValue, oldLiteral.getLanguage());
                } else {
                	newValue = owlModel.createRDFSLiteral(lexicalValue, datatype);
                }
                values.set(rowIndex, newValue);
            }
            subject.setPropertyValues(predicate, getNewValues(rowIndex));  // Will fire back events etc
        }
        else {
            if (isStringProperty()) {
                setLangAt((String) aValue, rowIndex);
            }
            else {
                setTypeAt(aValue, rowIndex);
            }
        }
    }

     //Original code
    /*
     private void setLangAt(String value, int rowIndex) {
        final String lexicalValue = (String) getValueAt(rowIndex, COL_VALUE);
        value = value.trim();
        Object newValue;
        if (value == null || value.length() == 0) {
            newValue = lexicalValue;
        }
        else {
            newValue = owlModel.createRDFSLiteral(lexicalValue, value);
        }
        values.set(rowIndex, newValue);
        subject.setPropertyValues(predicate, getNewValues(rowIndex));  // Will fire back events etc
    }
    */


    /*
     * Modified by Hemed Al Ruwehy
     * 2017-09-13
     */
    private void setLangAt(String value, int rowIndex) {
        final String lexicalValue = (String)getValueAt(rowIndex, COL_VALUE);
        Object newValue;
        if(isValidXSDLanguage(value)) {//Validate xsd:language
            newValue =  owlModel.createRDFSLiteral(lexicalValue, value.trim());
        }
        else {
            newValue = lexicalValue;
        }
        values.set(rowIndex, newValue);
        subject.setPropertyValues(predicate, getNewValues(rowIndex));  //Will fire back events etc
    }


    public void setValues(Collection newValues) {
        values = new ArrayList();
        for (Iterator it = newValues.iterator(); it.hasNext();) {
            Object value = it.next();
            if (!(value instanceof RDFResource) &&
                    !(value instanceof RDFSLiteral)) {
                value = owlModel.createRDFSLiteral(value);
            }
            values.add(value);
        }
        fireTableDataChanged();
    }


    public void updateValues() {
        if (predicate != null && subject != null) {
            List newValues = new ArrayList(subject.getPropertyValues(predicate, true));
            Collection hasValues = subject.getHasValuesOnTypes(predicate);
            for (Iterator it = hasValues.iterator(); it.hasNext();) {
                Object hasValue = it.next();
                if (!newValues.contains(hasValue)) {
                    newValues.add(hasValue);
                }
            }
            setValues(newValues);
        }
    }
}
