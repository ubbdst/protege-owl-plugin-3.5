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

package edu.stanford.smi.protegex.owl.model.impl;

import edu.stanford.smi.protege.model.DefaultSimpleInstance;
import edu.stanford.smi.protege.model.FrameID;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protegex.owl.model.*;
import edu.stanford.smi.protegex.owl.model.event.PropertyValueListener;
import edu.stanford.smi.protegex.owl.model.event.ResourceListener;
import edu.stanford.smi.protegex.owl.model.visitor.OWLModelVisitor;
import edu.stanford.smi.protegex.owl.ui.icons.OWLIcons;

import javax.swing.*;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import static edu.stanford.smi.protegex.owl.util.InstanceUtil.getTrashClass;
import static edu.stanford.smi.protegex.owl.util.InstanceUtil.stripDatatype;
import static edu.stanford.smi.protegex.owl.ui.widget.UUIDWidget.getUUIDFromInstanceURI;
import static edu.stanford.smi.protegex.owl.ui.widget.UUIDWidget.isValidUUID;

public class DefaultRDFIndividual extends DefaultSimpleInstance implements RDFIndividual {

    public DefaultRDFIndividual(KnowledgeBase kb, FrameID id) {
        super(kb, id);
    }

    public DefaultRDFIndividual() { }

    /**
     * Returns a local name of this instance
     *
     * Note that, this is an experimental feature, it may be removed in the future.
     */
    /*private String getHumanReadableName(OWLModel model) {
        if(humanReadableText == null) {
            String classURI = getProtegeType().getName();
            //Generate new browser text
            String newBrowserText = model.getUniqueFrameName(NamespaceUtil.getLocalName(classURI));
            //Set this new browser text
            setHumanReadableText(newBrowserText);
            return newBrowserText;
        }
        return humanReadableText;
    }*/

    /**
     * Returns a human understandable name of this instance
     *
     * This is an experimental feature, it may be removed in the future.
     */
    private String getHumanReadableName() {
        String uuid = getUUIDFromInstanceURI(this);
        if(!uuid.isEmpty()) {
            if (isInTrash()) {
                Object classHierarchyURI = getPropertyValue(getOWLModel()
                        .getRDFProperty(UBBOntologyNamespaces.CLASS_HIERARCHY_URI));
                if (classHierarchyURI != null) {
                  return stripDatatype(classHierarchyURI.toString());
                }
            }
            return getProtegeType().getBrowserText() + "_" + uuid;
        }
      return "";
    }


    public void accept(OWLModelVisitor visitor) {
        visitor.visitRDFIndividual(this);
    }

    /**
     * Check if this instance belongs to Trash
     */
    private boolean isInTrash() {
        return getProtegeTypes().contains(getTrashClass(getOWLModel()));
    }

    @Override
    public String getBrowserText() {
        String browserText = super.getBrowserText();
        //Append class name for easy readability
        if(browserText.startsWith("http") || isValidUUID(browserText)){
            if(!getHumanReadableName().isEmpty()) {
                return getHumanReadableName();
            }
        }
        return browserText;
    }

    public Icon getIcon() {
    	String iconName;
    	if(isAnonymous()) {
           iconName = OWLIcons.RDF_ANON_INDIVIDUAL;
        }
        else if(isInTrash()) {
            iconName = OWLIcons.TRASH_INDIVIDUAL_ICON;
        }
        else {
    	    iconName = OWLIcons.RDF_INDIVIDUAL;
        }
    	//iconName = (isInTrash() || isAnonymous() ? OWLIcons.RDF_ANON_INDIVIDUAL : OWLIcons.RDF_INDIVIDUAL);
        return isEditable() ?
                OWLIcons.getImageIcon(iconName) :
                OWLIcons.getReadOnlyIndividualIcon(OWLIcons.getImageIcon(iconName));
    }


    public String getIconName() {
        return (isAnonymous() ? OWLIcons.RDF_ANON_INDIVIDUAL : OWLIcons.RDF_INDIVIDUAL);
    }
    // RDFResource implementation methods --------------------------------------------------------


    public void addComment(String comment) {
        OWLUtil.addComment(this, comment);
    }


    public void addDifferentFrom(RDFResource resource) {
        OWLUtil.addDifferentFrom(this, resource);
    }


    public void addIsDefinedBy(RDFResource instance) {
        OWLUtil.addIsDefinedBy(this, instance);
    }


    public void addLabel(String label, String language) {
        OWLUtil.addLabel(this, label, language);
    }


    public void addPropertyValue(RDFProperty property, Object value) {
        OWLUtil.addPropertyValue(this, property, value);
    }


    public void addPropertyValueListener(PropertyValueListener listener) {
        OWLUtil.addPropertyValueListener(this, listener);
    }


    public void addProtegeType(RDFSClass type) {
        OWLUtil.addProtegeType(this, type);
    }


    public void addRDFType(RDFSClass type) {
        OWLUtil.addRDFType(this, type);
    }


    public void addResourceListener(ResourceListener listener) {
        OWLUtil.addResourceListener(this, listener);
    }


    public void addSameAs(RDFResource resource) {
        OWLUtil.addSameAs(this, resource);
    }


    public void addVersionInfo(String versionInfo) {
        OWLUtil.addVersionInfo(this, versionInfo);
    }


    public RDFResource as(Class javaInterface) {
        return OWLUtil.as(this, javaInterface);
    }


    public boolean canAs(Class javaInterface) {
        return OWLUtil.canAs(this, javaInterface);
    }


    public boolean equalsStructurally(RDFObject object) {
        if (object instanceof RDFIndividual) {
            return getName().equals(((RDFIndividual) object).getName());
        }
        return false;
    }


    public RDFResource getAllValuesFromOnTypes(RDFProperty property) {
        return OWLUtil.getAllValuesFromOnTypes(this, property);
    }


    public Collection getComments() {
        return OWLUtil.getComments(this);
    }


    public Collection getDifferentFrom() {
        return OWLUtil.getDifferentFrom(this);
    }


    public Collection getHasValuesOnTypes(RDFProperty property) {
        return OWLUtil.getHasValuesOnTypes(this, property);
    }


    public Class getIconLocation() {
        return OWLIcons.class;
    }


    public RDFSClass getProtegeType() {
        return OWLUtil.getDirectRDFType(this);
    }


    public Collection getProtegeTypes() {
        return OWLUtil.getDirectRDFTypes(this);
    }


    public Collection getDocumentation() {
        return OWLUtil.getComments(this);
    }


    public Collection getInferredTypes() {
        return OWLUtil.getInferredDirectTypes(this);
    }


    public Collection getIsDefinedBy() {
        return OWLUtil.getIsDefinedBy(this);
    }


    public Collection getLabels() {
        return OWLUtil.getLabels(this);
    }


    public String getLocalName() {
        return NamespaceUtil.getLocalName(getName());
    }
    
    public String getPrefixedName() {
    	return NamespaceUtil.getPrefixedName(getOWLModel(), getName());
    }


    public String getNamespace() {
        return NamespaceUtil.getNameSpace(getName());
    }


    public String getNamespacePrefix() {
        return NamespaceUtil.getPrefixForResourceName(getOWLModel(), getName());
    }


    public OWLModel getOWLModel() {
        return (OWLModel) getKnowledgeBase();
    }


    public Collection getPossibleRDFProperties() {
        return OWLUtil.getPossibleRDFProperties(this);
    }


    public Object getPropertyValue(RDFProperty property) {
        return OWLUtil.getPropertyValue(this, property, false);
    }


    public RDFResource getPropertyValueAs(RDFProperty property, Class javaInterface) {
        return OWLUtil.getPropertyValueAs(this, property, javaInterface);
    }


    public Object getPropertyValue(RDFProperty property, boolean includingSubproperties) {
        return OWLUtil.getPropertyValue(this, property, includingSubproperties);
    }


    public RDFSLiteral getPropertyValueLiteral(RDFProperty property) {
        return OWLUtil.getPropertyValueLiteral(this, property);
    }


    public int getPropertyValueCount(RDFProperty property) {
        return OWLUtil.getPropertyValueCount(this, property);
    }


    public Collection getPropertyValueLiterals(RDFProperty property) {
        return OWLUtil.getPropertyValueLiterals(this, property);
    }


    public Collection getPropertyValues(RDFProperty property) {
        return OWLUtil.getPropertyValues(this, property, false);
    }


    public Collection getPropertyValuesAs(RDFProperty property, Class javaInterface) {
        return OWLUtil.getPropertyValuesAs(this, property, javaInterface);
    }


    public Collection getPropertyValues(RDFProperty property, boolean includingSubproperties) {
        return OWLUtil.getPropertyValues(this, property, includingSubproperties);
    }


    public Collection getRDFProperties() {
        return OWLUtil.getRDFProperties(this);
    }


    public RDFSClass getRDFType() {
        return OWLUtil.getRDFType(this);
    }


    public Collection getRDFTypes() {
        return OWLUtil.getRDFTypes(this);
    }


    public Set getReferringAnonymousClasses() {
        return OWLUtil.getReferringAnonymousClses(this);
    }


    public Collection getSameAs() {
        return OWLUtil.getSameAs(this);
    }


    public String getURI() {
        //return getOWLModel().getURIForResourceName(getName());
    	return getName();
    }


    public Collection getVersionInfo() {
        return OWLUtil.getVersionInfo(this);
    }


    public boolean hasPropertyValue(RDFProperty property) {
        return OWLUtil.hasPropertyValue(this, property);
    }


    public boolean hasPropertyValue(RDFProperty property, boolean includingSubproperties) {
        return OWLUtil.hasPropertyValue(this, property, includingSubproperties);
    }


    public boolean hasPropertyValue(RDFProperty property, Object value) {
        return hasPropertyValue(property, value, false);
    }


    public boolean hasPropertyValue(RDFProperty property, Object value, boolean includingSuperproperties) {
        return OWLUtil.hasPropertyValue(this, property, value, includingSuperproperties);
    }


    public boolean hasProtegeType(RDFSClass type) {
        return OWLUtil.hasProtegeType(this, type);
    }


    public boolean hasProtegeType(RDFSClass type, boolean includingSuperclasses) {
        return OWLUtil.hasProtegeType(this, type, includingSuperclasses);
    }


    public boolean hasRDFType(RDFSClass type) {
        return OWLUtil.hasRDFType(this, type);
    }


    public boolean hasRDFType(RDFSClass type, boolean includingSuperclasses) {
        return OWLUtil.hasRDFType(this, type, includingSuperclasses);
    }


    public boolean isAnonymous() {
        return getOWLModel().isAnonymousResourceName(getName());
    }


    public boolean isValidPropertyValue(RDFProperty property, Object object) {
        return OWLUtil.isValidPropertyValue(this, property, object);
    }


    public Iterator listPropertyValues(RDFProperty property) {
        return OWLUtil.listPropertyValues(this, property, false);
    }


    public Iterator listPropertyValuesAs(RDFProperty property, Class javaInterface) {
        return OWLUtil.listPropertyValuesAs(this, property, javaInterface);
    }


    public Iterator listPropertyValues(RDFProperty property, boolean includingSubproperties) {
        return OWLUtil.listPropertyValues(this, property, includingSubproperties);
    }


    public Iterator listRDFTypes() {
        return getRDFTypes().iterator();
    }


    public void removeComment(String value) {
        OWLUtil.removeComment(this, value);
    }


    public void removeDifferentFrom(RDFResource resource) {
        OWLUtil.removeDifferentFrom(this, resource);
    }


    public void removeIsDefinedBy(RDFResource resource) {
        OWLUtil.removeIsDefinedBy(this, resource);
    }


    public void removeLabel(String label, String language) {
        OWLUtil.removeLabel(this, label, language);
    }


    public void removePropertyValue(RDFProperty property, Object value) {
        OWLUtil.removePropertyValue(this, property, value);
    }


    public void removePropertyValueListener(PropertyValueListener listener) {
        OWLUtil.removePropertyValueListener(this, listener);
    }


    public void removeProtegeType(RDFSClass type) {
        OWLUtil.removeProtegeType(this, type);
    }


    public void removeRDFType(RDFSClass type) {
        OWLUtil.removeRDFType(this, type);
    }


    public void removeResourceListener(ResourceListener listener) {
        OWLUtil.removeResourceListener(this, listener);
    }


    public void removeSameAs(RDFResource resource) {
        OWLUtil.removeSameAs(this, resource);
    }


    public void removeVersionInfo(String versionInfo) {
        OWLUtil.removeVersionInfo(this, versionInfo);
    }


    public void setComment(String comment) {
        OWLUtil.setComment(this, comment);
    }


    public void setComments(Collection comments) {
        OWLUtil.setComments(this, comments);
    }


    public void setDocumentation(String value) {
        OWLUtil.setComment(this, value);
    }


    public void setInferredTypes(Collection types) {
        OWLUtil.setInferredTypes(this, types);
    }


    public void setPropertyValue(RDFProperty property, Object value) {
        OWLUtil.setPropertyValue(this, property, value);
    }

    public void setPropertyValues(RDFProperty property, Collection values) {
        OWLUtil.setPropertyValues(this, property, values);
    }

    public void setProtegeType(RDFSClass type) {
        OWLUtil.setProtegeType(this, type);
    }


    public void setProtegeTypes(Collection types) {
        OWLUtil.setProtegeTypes(this, types);
    }


    public void setRDFType(RDFSClass type) {
        OWLUtil.setRDFType(this, type);
    }


    public void setRDFTypes(Collection types) {
        OWLUtil.setRDFTypes(this, types);
    }
    
    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(this.getClass().getSimpleName());
        buffer.append("(");
        buffer.append(getName());
        buffer.append(" of ");
        buffer.append(getDirectTypes());
        buffer.append(")");
        return buffer.toString();
    }
}
