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

package edu.stanford.smi.protegex.owl.ui.widget;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Facet;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.util.ComponentUtilities;
import edu.stanford.smi.protege.util.LabeledComponent;
import edu.stanford.smi.protege.widget.AbstractSlotWidget;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.ui.components.annotations.AnnotationsComponent;
import edu.stanford.smi.protegex.owl.ui.components.triples.AbstractTriplesComponent;
import edu.stanford.smi.protegex.owl.ui.components.triples.TriplesComponent;
import edu.stanford.smi.protegex.owl.ui.icons.OWLIcons;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Collection;

/**
 * The SlotWidget showing up as default on the top of all forms.
 * This contains two "tabs", for annotation property values and some other triples.
 *
 * @author Holger Knublauch  <holger@knublauch.com>
 */
public class HeaderWidget extends AbstractSlotWidget {

    private AbstractTriplesComponent component;


    public void activate(AbstractTriplesComponent newComponent) {

        if (component != null) {
            removeAll();
            ComponentUtilities.dispose(component);
        }

        component = newComponent;
        component.setSubject((RDFResource) getInstance());
        component.setEnabled(isEnabled());
        add(BorderLayout.CENTER, component);
        revalidate();
    }


    public void activateAnnotationsComponent() {
        RDFProperty property = (RDFProperty) getSlot();
        activate(new AnnotationsComponent(property, isReadOnlyConfiguredWidget()) {
            @Override
			protected void addButtons(LabeledComponent lc) {
                super.addButtons(lc);
                lc.addHeaderSeparator();
                lc.addHeaderSeparator();
                lc.addHeaderButton(new AbstractAction("Switch to Triples", OWLIcons.getImageIcon(OWLIcons.TRIPLES)) {
                    public void actionPerformed(ActionEvent e) {
                        activateTriplesComponent();
                    }
                });
            }
        });
    }


    public void activateTriplesComponent() {
        RDFProperty property = (RDFProperty) getSlot();
        activate(new TriplesComponent(property, isReadOnlyConfiguredWidget()) {
            @Override
			protected void addButtons(LabeledComponent lc) {
                super.addButtons(lc);
                lc.addHeaderSeparator();
                lc.addHeaderSeparator();
                lc.addHeaderButton(new AbstractAction("Switch to Annotations", OWLIcons.getImageIcon(OWLIcons.ANNOTATIONS_TABLE)) {
                    public void actionPerformed(ActionEvent e) {
                        activateAnnotationsComponent();
                    }
                });
            }
        });
    }


    public void initialize() {
        setLayout(new BorderLayout());
        //activateAnnotationsComponent();
        activateTriplesComponent();
    }


    public static boolean isSuitable(Cls cls, Slot slot, Facet facet) {
        return OWLWidgetMapper.isSuitable(HeaderWidget.class, cls, slot);
    }


    @Override
	public void setInstance(Instance newInstance) {
        super.setInstance(newInstance);
        if (newInstance instanceof RDFResource) {
            RDFResource resource = (RDFResource) newInstance;
            component.setSubject(resource);
        }
    }


    @Override
    protected String getInvalidValueText(Collection values) {
    	return "";
    }

	@Override
	public void setEnabled(boolean enabled) {
		component.setEnabled(enabled);
		super.setEnabled(enabled);
	}

	@Override
	public void dispose() {
		component.dispose();
	}

}
