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

package edu.stanford.smi.protegex.owl.ui.components.singleresource;

import edu.stanford.smi.protegex.owl.model.*;
import edu.stanford.smi.protegex.owl.ui.ProtegeUI;
import edu.stanford.smi.protegex.owl.ui.components.PropertyValuesComponent;
import edu.stanford.smi.protegex.owl.ui.dialogs.DefaultSelectionDialogFactory;
import edu.stanford.smi.protegex.owl.ui.icons.OWLIcons;
import edu.stanford.smi.protegex.owl.ui.resourceselection.ResourceSelectionAction;
import edu.stanford.smi.protegex.owl.ui.widget.OWLUI;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * @author Holger Knublauch  <holger@knublauch.com>
 */
public class SetResourceAction extends ResourceSelectionAction {

    private PropertyValuesComponent component;


    public SetResourceAction(PropertyValuesComponent component) {
        this("Select existing resource...", OWLIcons.getAddIcon(OWLIcons.RDF_INDIVIDUAL), component);
    }

    public SetResourceAction(String value, Icon icon, PropertyValuesComponent component) {
        super(value, icon);
        this.component = component;
    }


    public Collection getEnumeratedValues(Collection unionRangeClasses) {
        Collection results = new HashSet();
        for (Iterator it = unionRangeClasses.iterator(); it.hasNext();) {
            RDFSClass rangeClass = (RDFSClass) it.next();
            if (rangeClass instanceof OWLEnumeratedClass) {
                results.addAll(((OWLEnumeratedClass) rangeClass).getOneOf());
            }
        }
        return results;
    }


    @Override
	public Collection getSelectableResources() {
        RDFResource subject = component.getSubject();
        RDFProperty predicate = component.getPredicate();
        Set clses = getUnionRangeClasses(subject, predicate, true);
        Collection enums = getEnumeratedValues(clses);
        if (enums.size() > 0) {
            return enums;
        }
        else if (clses.contains(subject.getOWLModel().getOWLThingClass())) {
            return subject.getOWLModel().getUserDefinedRDFIndividuals(true);
        }
        else {
            Set instances = new HashSet();
            for (Iterator it = clses.iterator(); it.hasNext();) {
                RDFSClass cls = (RDFSClass) it.next();
                instances.addAll(cls.getInstances(true));
            }
            return instances;
        }
    }


    private Set getUnionRangeClasses(RDFResource subject, RDFProperty predicate, boolean includingEnumeratedClasses) {
        OWLModel owlModel = subject.getOWLModel();
        Set clses = new HashSet();
        for (Iterator it = subject.getRDFTypes().iterator(); it.hasNext();) {
            RDFSClass type = (RDFSClass) it.next();
            if (type instanceof RDFSNamedClass) {
                RDFSNamedClass namedType = (RDFSNamedClass) type;
                Collection urcs = namedType.getUnionRangeClasses(predicate);
                for (Iterator us = urcs.iterator(); us.hasNext();) {
                    RDFSClass rangeClass = (RDFSClass) us.next();
                    if (rangeClass instanceof RDFSNamedClass ||
                            (includingEnumeratedClasses && rangeClass instanceof OWLEnumeratedClass)) {
                        clses.add(rangeClass);
                    }
                }
            }
        }
        if (clses.isEmpty()) {
            clses.add(owlModel.getOWLThingClass());
        }
        return clses;
    }


    @Override
	public RDFResource pickResource() {
        RDFResource subject = component.getSubject();
        RDFProperty predicate = component.getPredicate();
        OWLModel owlModel = predicate.getOWLModel();
        Collection allClasses = getUnionRangeClasses(subject, predicate, true);
        Collection enums = getEnumeratedValues(allClasses);
        if (enums.isEmpty()) {
            Collection clses = getUnionRangeClasses(subject, predicate, false);
            if (OWLUI.isExternalResourcesSupported(owlModel)) {
                owlModel.getRDFUntypedResourcesClass().setVisible(true);
                clses.add(owlModel.getRDFUntypedResourcesClass());
            }
            //RDFResource resource = ProtegeUI.getSelectionDialogFactory().selectResourceByType((Component) component, owlModel, clses);
            RDFResource resource = new DefaultSelectionDialogFactory().selectResourceWithBrowserTextByType((Component) component, owlModel, clses, "Select Resource");
            owlModel.getRDFUntypedResourcesClass().setVisible(false);
            return resource;
        }
        else {
            return ProtegeUI.getSelectionDialogFactory().selectResourceFromCollection((Component) component, owlModel, enums, (String) getValue(Action.NAME));
        }
    }


    public void resourceSelected(RDFResource resource) {
        component.getSubject().setPropertyValue(component.getPredicate(), resource);
    }
}
