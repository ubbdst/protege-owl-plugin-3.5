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

package edu.stanford.smi.protegex.owl.ui.components.multiresource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import javax.swing.AbstractListModel;

import edu.stanford.smi.protege.event.FrameAdapter;
import edu.stanford.smi.protege.event.FrameEvent;
import edu.stanford.smi.protege.event.FrameListener;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.server.framestore.RemoteClientFrameStore;
import edu.stanford.smi.protege.util.ApplicationProperties;
import edu.stanford.smi.protege.util.Disposable;
import edu.stanford.smi.protege.util.FrameWithBrowserText;
import edu.stanford.smi.protege.util.FrameWithBrowserTextComparator;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.model.triplestore.TripleStoreModel;
import edu.stanford.smi.protegex.owl.ui.individuals.OWLGetOwnSlotValuesBrowserTextJob;
import edu.stanford.smi.protegex.owl.ui.widget.OWLUI;

/**
 * A ListModel representing values of a subject-predicate pair.
 *
 * @author Holger Knublauch  <holger@knublauch.com>
 */
public class MultiResourceListModel extends AbstractListModel implements Disposable {

	private static final long serialVersionUID = -7197293909519481988L;

	private RDFProperty predicate;
    private RDFResource subject;
    private List<FrameWithBrowserText> values = new ArrayList<FrameWithBrowserText>();
    
    private FrameListener frameListener;

    public MultiResourceListModel(RDFProperty predicate) {
        this.predicate = predicate;
        this.frameListener = getFrameListener();
        addListener();
    }

    @SuppressWarnings("deprecation")
	private void addListener() {
		if (predicate != null) {
			predicate.getKnowledgeBase().addFrameListener(getFrameListener());
		}		
	}
    
    @SuppressWarnings("deprecation")
	private void removeListener() {
		if (predicate != null) {
			predicate.getKnowledgeBase().removeFrameListener(getFrameListener());
		}		
	}


    private FrameListener getFrameListener() {
    	if (frameListener == null) {
    		frameListener = new FrameAdapter() {
    			@Override
    			public void browserTextChanged(FrameEvent event) {
    				Frame frame = event.getFrame();
    				for (FrameWithBrowserText fbt : values) {
						Frame f = fbt.getFrame();
						if (f != null && f.equals(frame)) {
							updateValues();
						}
					}
    			}
    		};
    	}
    	return frameListener;
    }

	public Object getElementAt(int index) {
    	return  values.get(index);
    }

    public RDFProperty getPredicate() {
        return predicate;
    }

    public RDFResource getResourceAt(int row) {
    	FrameWithBrowserText fbt = (FrameWithBrowserText) getElementAt(row);
        return (RDFResource) fbt.getFrame();
    }

    public int getRowOf(Object value) {
    	if (value instanceof RDFResource) {
    		return values.indexOf(new FrameWithBrowserText((RDFResource)value));
    	} else if (value instanceof FrameWithBrowserText) {
    		return values.indexOf(value);
    	}
        return -1;
    }

    public int getSize() {
        return values.size();
    }

    public RDFResource getSubject() {
        return subject;
    }


    public boolean isEditable(int row) {
        TripleStoreModel tsm = subject.getOWLModel().getTripleStoreModel();
        Object object = getResourceAt(row);
        return tsm.isEditableTriple(subject, predicate, object);
    }

    public boolean isRDFResource(int row) {
        return ((FrameWithBrowserText) getElementAt(row)).getFrame() instanceof RDFResource;
    }

    public void setSubject(RDFResource subject) {
        this.subject = subject;
        updateValues();
    }


    public void updateValues() {
        fireIntervalRemoved(this, 0, values.size());
        values = getValues();
        fireIntervalAdded(this, 0, values.size());
    }

    private List<FrameWithBrowserText> getValues() {
    	if (subject != null && useCacheHeuristics() &&
    			subject.getProject().isMultiUserClient() &&
    			isCached()) {
    		return getValuesFromCache();
    	} else {
        	OWLGetOwnSlotValuesBrowserTextJob job = new OWLGetOwnSlotValuesBrowserTextJob(subject.getOWLModel(), subject, predicate, false);
        	Collection<FrameWithBrowserText> vals = job.execute();
        	return new ArrayList<FrameWithBrowserText>(vals);    		
    	}
    }
    
    /**
     * This is a heuristic if the values of the (subj, pred) are cached..
     * Even if the (sub,pred) is cached, the frames inside may not be - 
     * they are needed for the browser text.. It's not clear that this
     * is going to work..
     */
    private boolean isCached() {    	
    	if (!RemoteClientFrameStore.isCacheComplete(subject) || 
    			!RemoteClientFrameStore.isCached(subject, predicate, null, false)) {
    		return false;
    	}
    	//cahce for subj and pred should be complete, so next call should not go to the server
    	Collection vals = subject.getPropertyValues(predicate);
    	if (vals == null || vals.isEmpty()) { return true; }
    	
    	for (Iterator iterator = vals.iterator(); iterator.hasNext();) {
			Object object = iterator.next();
			if (object instanceof Frame) {
				boolean c = RemoteClientFrameStore.isCacheComplete((Frame)object);
				if (!c) { return false;}
			}
		}    	
    	return true;
    }
    
    //TODO: refactor out
    private List<FrameWithBrowserText> getValuesFromCache() {
    	List<FrameWithBrowserText> framesWithBrowserText = new ArrayList<FrameWithBrowserText>();		
		Collection values = getLocalValues();
		for (Iterator iterator = values.iterator(); iterator.hasNext();) {
			Object value = iterator.next();
			if (value instanceof Frame) {
				Frame valueFrame = (Frame) value;
				framesWithBrowserText.add(new FrameWithBrowserText(valueFrame,
						valueFrame.getBrowserText(), ((Instance)valueFrame).getDirectTypes()));				
			} else {
				framesWithBrowserText.add(new FrameWithBrowserText(null, value.toString(), null));
			}
		}
		Collections.sort(framesWithBrowserText, new FrameWithBrowserTextComparator());
		return framesWithBrowserText;
    }
    
    //TODO: refactor out
    private Collection getLocalValues() {
    	Collection values = new ArrayList(subject.getPropertyValues(predicate, true));
    	values.addAll(subject.getHasValuesOnTypes(predicate));
    	return values;
    }
    
    
    private boolean useCacheHeuristics() {
    	return ApplicationProperties.getBooleanProperty(OWLUI.USE_CACHE_HEURISTICS_PROP, true);
    }
    

    public void dispose() {
    	try {
			removeListener();
		} catch (Exception e) {
			Log.getLogger().log(Level.WARNING, "Could not remove KB listener from multi resource widget for: " + predicate, e);
		}
    }
    
}
