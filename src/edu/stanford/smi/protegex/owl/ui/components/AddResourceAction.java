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

package edu.stanford.smi.protegex.owl.ui.components;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.model.ValueType;
import edu.stanford.smi.protegex.owl.model.*;
import edu.stanford.smi.protegex.owl.model.impl.AbstractOWLModel;
import edu.stanford.smi.protegex.owl.model.impl.OWLUtil;
import edu.stanford.smi.protegex.owl.ui.ProtegeUI;
import edu.stanford.smi.protegex.owl.ui.icons.OWLIcons;
import edu.stanford.smi.protegex.owl.ui.resourceselection.ResourceSelectionAction;
import edu.stanford.smi.protegex.owl.ui.widget.OWLUI;

import java.util.*;

/**
 * @author Holger Knublauch  <holger@knublauch.com>
 */
public class AddResourceAction extends ResourceSelectionAction {

    protected AddablePropertyValuesComponent component;
    protected boolean symmetric;


    public AddResourceAction(AddablePropertyValuesComponent component, boolean symmetric) {
        super("Add...", OWLIcons.getAddIcon(OWLIcons.RDF_INDIVIDUAL), true);
        this.component = component;
        this.symmetric = symmetric;
    }


    @Override
	public Collection getSelectableResources() {
        RDFResource subject = component.getSubject();
        RDFProperty predicate = component.getPredicate();
        OWLModel owlModel = subject.getOWLModel();
        Collection values;
        if (isClassProperty()) {
            values = owlModel.getUserDefinedRDFSNamedClasses();
        }
        else {
            RDFSNamedClass type = (RDFSNamedClass) subject.getProtegeType();
            Collection clses = type.getUnionRangeClasses(predicate);
            if (clses.size() > 0 && !OWLUtil.containsAnonymousClass(clses)) {
                values = new HashSet();
                for (Iterator it = clses.iterator(); it.hasNext();) {
                    Cls cls = (Cls) it.next();
                    if (cls.isVisible()) {
                        values.addAll(cls.getInstances());
                    }
                }
            }
            else {
                if (predicate instanceof OWLProperty) {
                    values = new ArrayList(owlModel.getOWLIndividuals(true));
                }
                else {
                    values = new ArrayList(owlModel.getRDFIndividuals(true));
                }
            }
        }
        if (OWLUI.isExternalResourcesSupported(owlModel)) {
            values.addAll(owlModel.getRDFUntypedResourcesClass().getInstances(false));
        }
        values.remove(subject);
        values.removeAll(subject.getPropertyValues(predicate));
        return AbstractOWLModel.getRDFResources(owlModel, values);
    }


    private boolean isClassProperty() {
        RDFResource subject = component.getSubject();
        RDFProperty predicate = component.getPredicate();
        RDFSNamedClass type = (RDFSNamedClass) subject.getProtegeType();
        Collection clses = type.getUnionRangeClasses(predicate);
        RDFSNamedClass metaclass = predicate.getOWLModel().getRDFSNamedClassClass();
        if (clses.size() == 1) {
            RDFSClass range = (RDFSClass) clses.iterator().next();
            return range.equals(metaclass) || range.getSuperclasses(true).contains(metaclass);
        }
        return false;
    }


    private boolean isPropertyProperty() {
        RDFResource subject = component.getSubject();
        RDFProperty predicate = component.getPredicate();
        if (((Slot) predicate).getValueType() == ValueType.INSTANCE) {
            RDFSNamedClass type = (RDFSNamedClass) subject.getProtegeType();
            Collection clses = type.getUnionRangeClasses(predicate);
            RDFSNamedClass metaclass = predicate.getOWLModel().getRDFPropertyClass();
            return clses.size() == 1 && ((RDFSClass) clses.iterator().next()).getSuperclasses(true).contains(metaclass);
        }
        else {
            return false;
        }
    }


    @Override
	public Collection pickResources() {
        RDFResource subject = component.getSubject();
        RDFProperty predicate = component.getPredicate();
        OWLModel owlModel = predicate.getOWLModel();
        if (OWLUI.isExternalResourcesSupported(owlModel)) {
            owlModel.getRDFUntypedResourcesClass().setVisible(true);
        }
        Collection result;
        if (isClassProperty()) {
            String title = "Select classes to add";
            result = ProtegeUI.getSelectionDialogFactory().selectClasses(component, owlModel, title);
        }
        else if (isPropertyProperty()) {
            String title = "Select properties to add";
            Collection properties = owlModel.getVisibleUserDefinedRDFProperties();
            result = ProtegeUI.getSelectionDialogFactory().selectResourcesFromCollection(component, owlModel, properties, title);
        }
        else {
            RDFSNamedClass type = (RDFSNamedClass) subject.getRDFType();
            Collection clses = type.getUnionRangeClasses(predicate);
            if (OWLUtil.containsAnonymousClass(clses) || clses.isEmpty()) {
                clses = Collections.singleton(owlModel.getOWLThingClass());
            }
            result = selectResourcesByType(owlModel, clses);            	
        }
        owlModel.getRDFUntypedResourcesClass().setVisible(false);
        return result;
    }


    protected Collection selectResourcesByType(OWLModel owlModel, Collection clses) {
    	return ProtegeUI.getSelectionDialogFactory().selectResourcesByType(component, owlModel, clses);
	}


	public void resourceSelected(RDFResource resource) {
        RDFResource subject = component.getSubject();
        RDFProperty predicate = component.getPredicate();
        if (subject.getPropertyValues(predicate).contains(resource)) {
            ProtegeUI.getModalDialogFactory().showErrorMessageDialog(subject.getOWLModel(), "The object " + resource.getBrowserText() + " is already in the list.");
        }
        else {
            component.addObject(resource, symmetric);
        }
    }

}
