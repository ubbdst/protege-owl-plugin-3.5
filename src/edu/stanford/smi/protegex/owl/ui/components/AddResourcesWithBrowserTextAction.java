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

import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.ui.dialogs.DefaultSelectionDialogFactory;

import java.util.Collection;

public class AddResourcesWithBrowserTextAction extends AddResourceAction {

	private static final long serialVersionUID = -3841713833632888447L;
	
	public AddResourcesWithBrowserTextAction(
			AddablePropertyValuesComponent component, boolean symmetric) {
		super(component, symmetric);		
	}

	@Override
	protected Collection selectResourcesByType(OWLModel owlModel,
			Collection clses) {
		return new DefaultSelectionDialogFactory().
			selectResourcesWithBrowserTextByType(component, owlModel, clses, "Select Resources..");
	
	}
}
