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

package edu.stanford.smi.protegex.owl.ui.editors;

import edu.stanford.smi.protegex.owl.model.*;
import edu.stanford.smi.protegex.owl.ui.ProtegeUI;
import edu.stanford.smi.protegex.owl.ui.widget.HTMLEditorPanel;
import edu.stanford.smi.protegex.owl.ui.widget.OWLWidgetUtil;

import java.awt.*;
import java.util.Iterator;

/**
 * @author Holger Knublauch  <holger@knublauch.com>
 */
public class StringValueEditor implements PropertyValueEditor {

    public boolean canEdit(RDFResource instance, RDFProperty property, Object value) {
        final OWLModel owlModel = instance.getOWLModel();
        if (value != null) {
            if (value instanceof RDFSLiteral) {
                RDFSLiteral literal = (RDFSLiteral) value;
                return instance.getOWLModel().getXSDstring().equals(literal.getDatatype());
            }
            else {
                return value instanceof String;
            }
        }
        else {
            for (Iterator it = instance.getProtegeTypes().iterator(); it.hasNext();) {
                RDFSNamedClass type = (RDFSNamedClass) it.next();
                if (OWLWidgetUtil.isDatatypeProperty(owlModel.getXSDstring(), type, property)) {
                    return true;
                }
            }
        }
        return false;
    }


    public Object createDefaultValue(RDFResource instance, RDFProperty property) {
        final OWLModel owlModel = instance.getOWLModel();
        String lang = owlModel.getDefaultLanguage();
        if (lang != null) {
            return owlModel.createRDFSLiteral("", lang);
        }
        else {
            return "";
        }
    }


    public Object editValue(Component parent, RDFResource instance, RDFProperty property, Object value) {
        OWLModel owlModel = instance.getOWLModel();
        RDFSLiteral oldLiteral;
        if (value instanceof String) {
            oldLiteral = owlModel.createRDFSLiteral((String) value, owlModel.getXSDstring());
        }
        else if (value instanceof RDFSLiteral) {
            oldLiteral = (RDFSLiteral) value;
        }
        else {
            oldLiteral = owlModel.createRDFSLiteral("", owlModel.getXSDstring());
        }
        if (parent == null) {
            parent = ProtegeUI.getTopLevelContainer(property.getProject());
        }
        RDFSLiteral newLiteral = HTMLEditorPanel.show(parent, oldLiteral,
                "Editing " + property.getBrowserText() + " at " + instance.getBrowserText(), instance.getOWLModel());

        if (newLiteral != null && !oldLiteral.equals(newLiteral)) {
            if(newLiteral.getLanguage() == null || newLiteral.getLanguage().length() == 0) {
                return newLiteral.getString();
            }
            else {
                return newLiteral;
            }
        }
        else {
            return null;
        }
    }


    public boolean mustEdit(RDFResource subject, RDFProperty predicate, Object value) {
        return false;
    }
}
