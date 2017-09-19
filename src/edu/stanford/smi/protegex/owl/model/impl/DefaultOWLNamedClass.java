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

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protegex.owl.model.*;
import edu.stanford.smi.protegex.owl.model.visitor.OWLModelVisitor;
import edu.stanford.smi.protegex.owl.ui.icons.OWLIcons;

import javax.swing.*;
import java.util.*;

/**
 * The default implementation of the OWLNamedClass interface.
 *
 * @author Holger Knublauch  <holger@knublauch.com>
 */
public class DefaultOWLNamedClass extends DefaultRDFSNamedClass implements OWLNamedClass {


    public DefaultOWLNamedClass(KnowledgeBase kb, FrameID id) {
        super(kb, id);
    }


    public DefaultOWLNamedClass() {
    }


    public void addDisjointClass(RDFSClass aClass) {
        Slot disjointClassesSlot = getOWLModel().getOWLDisjointWithProperty();
        addOwnSlotValue(disjointClassesSlot, aClass);
    }


    public void addEquivalentClass(final RDFSClass aClass) {
        new Transaction(getOWLModel(), "Add Equivalent Class" + Transaction.APPLY_TO_TRAILER_STRING + this.getName()) {
            public boolean doOperations() {
                if (!hasDirectSuperclass(aClass)) {
                    addDirectSuperclass(aClass);
                }
                if (!aClass.isSubclassOf(DefaultOWLNamedClass.this)) {
                    aClass.addSuperclass(DefaultOWLNamedClass.this);
                }
                return true;
            };
        }.execute();
    }


    public void addInferredSuperclass(RDFSClass superclass) {
        Slot superclassesSlot = getAbstractOWLModel().getProtegeInferredSuperclassesProperty();
        addOwnSlotValue(superclassesSlot, superclass);
        if (superclass instanceof OWLNamedClass) {
            Slot subclassesSlot = getAbstractOWLModel().getProtegeInferredSubclassesProperty();
            ((Cls) superclass).addOwnSlotValue(subclassesSlot, this);
        }
    }


    public OWLIndividual createOWLIndividual(String name) {
        String fullName = OWLUtil.getInternalFullName(getOWLModel(), name);
        return (OWLIndividual) createInstance(fullName);
    }


    public boolean equalsStructurally(RDFSClass cls) {
        return getName().equals(cls.getName());
    }


    public RDFResource getAllValuesFrom(RDFProperty property) {
        Collection restrictions = getRestrictions(property, true);
        for (Iterator it = restrictions.iterator(); it.hasNext();) {
            OWLRestriction restriction = (OWLRestriction) it.next();
            if (restriction instanceof OWLAllValuesFrom) {
                return ((OWLAllValuesFrom) restriction).getAllValuesFrom();
            }
        }
        return property.getRange();
    }


    @Override
    public Set getAssociatedProperties() {
        Set set = super.getAssociatedProperties();
        Set maxZeroProperties = new HashSet();
        Iterator restrictions = getRestrictions(true).iterator();
        while (restrictions.hasNext()) {
            OWLRestriction restriction = (OWLRestriction) restrictions.next();
            RDFProperty property = restriction.getOnProperty();
            if ((restriction instanceof OWLCardinality || restriction instanceof OWLMaxCardinality) &&
                    ((OWLCardinalityBase) restriction).getCardinality() == 0) {
                maxZeroProperties.add(property);
                maxZeroProperties.add(property.getSubproperties(true));
            }
            else {
                set.add(property);
                set.addAll(property.getSubproperties(true));
            }
        }
        set.removeAll(maxZeroProperties);
        return set;
    }


    public int getClassificationStatus() {
        final Slot slot = getAbstractOWLModel().getProtegeClassificationStatusProperty();
        Collection values = getDirectOwnSlotValues(slot);
        if (values.size() == 0) {
            return OWLNames.CLASSIFICATION_STATUS_UNDEFINED;
        }
        else {
            return ((Integer) values.iterator().next()).intValue();
        }
    }


    public Collection getDirectRestrictions() {
        Collection result = new ArrayList();
        getDirectRestrictions(result, getDirectSuperclasses().iterator());
        return result;
    }

    
    public Collection getHasValues(RDFProperty property) {
    	Set hasValues = new HashSet();
    	
        for (Iterator it = getRestrictions(property, true).iterator(); it.hasNext();) {
            OWLRestriction restriction = (OWLRestriction) it.next();
            if (restriction instanceof OWLHasValue) {
                OWLHasValue hasValue = (OWLHasValue) restriction;
                hasValues.add(hasValue.getHasValue());
            }
        }
        return hasValues;    	
    }

    public Object getHasValue(RDFProperty property) {
        for (Iterator it = getRestrictions(property, true).iterator(); it.hasNext();) {
            OWLRestriction restriction = (OWLRestriction) it.next();
            if (restriction instanceof OWLHasValue) {
                OWLHasValue hasValue = (OWLHasValue) restriction;
                return hasValue.getHasValue();
            }
        }
        return null;
    }


    public Collection getInferredEquivalentClasses() {
        Collection result = new HashSet();
        for (Iterator it = getInferredSuperclasses().iterator(); it.hasNext();) {
            Cls superCls = (Cls) it.next();
            if (getInferredSubclasses().contains(superCls)) {
                result.add(superCls);
            }
        }
        return result;
    }


    private void getDirectRestrictions(Collection results, Iterator clses) {
        while (clses.hasNext()) {
            Cls cls = (Cls) clses.next();
            if (cls instanceof OWLRestriction) {
                results.add(cls);
            }
            else if (cls instanceof OWLIntersectionClass) {
                OWLIntersectionClass logicalCls = (OWLIntersectionClass) cls;
                getDirectRestrictions(results, logicalCls.getOperands().iterator());
            }
        }
    }


    public Collection getInferredSubclasses() {
        Slot slot = getAbstractOWLModel().getProtegeInferredSubclassesProperty();
        return getDirectOwnSlotValues(slot);
    }


    public Collection getInferredSuperclasses() {
        Slot slot = getAbstractOWLModel().getProtegeInferredSuperclassesProperty();
        return getDirectOwnSlotValues(slot);
    }


    public int getMaxCardinality(RDFProperty property) {
        if (property.isFunctional()) {
            return 1;
        }
        Collection restrictions = getRestrictions(property, true);
        for (Iterator it = restrictions.iterator(); it.hasNext();) {
            OWLRestriction restriction = (OWLRestriction) it.next();
            if (restriction instanceof OWLCardinalityBase) {
                OWLCardinalityBase base = (OWLCardinalityBase) restriction;
                if (!base.isQualified()) {
                    if (restriction instanceof OWLMaxCardinality) {
                        return ((OWLMaxCardinality) restriction).getCardinality();
                    }
                    else if (restriction instanceof OWLCardinality) {
                        return ((OWLCardinality) restriction).getCardinality();
                    }
                }
            }
        }
        return -1;
    }


    public int getMinCardinality(RDFProperty property) {
        Collection restrictions = getRestrictions(property, true);
        for (Iterator it = restrictions.iterator(); it.hasNext();) {
            OWLRestriction restriction = (OWLRestriction) it.next();
            if (restriction instanceof OWLMinCardinality) {
                return ((OWLMinCardinality) restriction).getCardinality();
            }
            else if (restriction instanceof OWLCardinality) {
                return ((OWLCardinality) restriction).getCardinality();
            }
        }
        return 0;
    }


    public Collection getRestrictions() {
        return getRestrictions(false);
    }


    @Override
    public Icon getIcon() {
        if (getLocalName().equals(UBBOntologyNamespaces.TRASH_CLASS_NAME)) {
            return isEditable() ?
                    OWLIcons.getImageIcon(OWLIcons.TRASH_CLASS_ICON) :
                    OWLIcons.getReadOnlyClsIcon(OWLIcons.getImageIcon(OWLIcons.TRASH_CLASS_ICON));
        }
        else if (!getOWLModel().getProject().isMultiUserClient() && isMetaCls()) {
            return super.getIcon();
        }
        else {
            ImageIcon ii = getImageIconForNonMetaclass();
            if (isEditable()) {
                return ii;
            }
            else {
                return OWLIcons.getReadOnlyClsIcon(ii);
            }
        }
    }


    @Override
    public String getIconName() {
        if(isMetaclass()) {
            return super.getIconName();
        }
        else {
            return getImageIconNameForNonMetaclass();
        }
    }


    @Override
    public ImageIcon getImageIcon() {
        if (isMetaCls()) {
            return super.getImageIcon();
        }
        else {
            return getImageIconForNonMetaclass();
        }
    }


    private ImageIcon getImageIconForNonMetaclass() {
        String str = getImageIconNameForNonMetaclass();
        return OWLIcons.getImageIcon(str);
    }


    private String getImageIconNameForNonMetaclass() {
        String str = null;
        if (getPropertyValueCount(getOWLModel().getOWLEquivalentClassProperty()) == 0) {
            str = OWLIcons.PRIMITIVE_OWL_CLASS;
        }
        else {
            str = OWLIcons.DEFINED_OWL_CLASS;
        }
        if (!getOWLModel().getProject().isMultiUserClient()) {
            if (isConsistent() == false) {
                str += "Inconsistent";
            }
            if (getSubclassesDisjoint()) {
                str += "SD";
            }
        }
        return str;
    }


    public Collection getRestrictions(boolean includingSuperclassRestrictions) {
        if (includingSuperclassRestrictions) {
            Set reached = new HashSet();
            Collection restrictions = new ArrayList();
            getRestrictions(this, reached, restrictions);
            return restrictions;
        }
        else {
            return getDirectRestrictions();
        }
    }


    public Collection getRestrictions(RDFProperty property, boolean includingSuperclassRestrictions) {
        Collection result = new ArrayList();
        Collection rs = getRestrictions(includingSuperclassRestrictions);
        for (Iterator it = rs.iterator(); it.hasNext();) {
            OWLRestriction restriction = (OWLRestriction) it.next();
            if (property.equals(restriction.getOnProperty())) {
                result.add(restriction);
            }
        }
        return result;
    }


    private static void getRestrictions(OWLNamedClass cls, Set reached, Collection results) {
        reached.add(cls);
        Collection restrictions = cls.getRestrictions(false);
        for (Iterator rit = restrictions.iterator(); rit.hasNext();) {
            OWLRestriction restriction = (OWLRestriction) rit.next();
            if (restriction instanceof OWLAllValuesFrom) {
                if (!isOverloadedAllValuesFrom((OWLAllValuesFrom) restriction, results)) {
                    results.add(restriction);
                }
            }
            else if (restriction instanceof OWLMinCardinality) {
                if (!isOverloadedMinCardinality((OWLMinCardinality) restriction, results)) {
                    results.add(restriction);
                }
            }
            else if (restriction instanceof OWLMaxCardinality) {
                if (!isOverloadedMaxCardinality((OWLMaxCardinality) restriction, results)) {
                    results.add(restriction);
                }
            }
            else if (restriction instanceof OWLCardinality) {
                if (!isOverloadedCardinality((OWLCardinality) restriction, results)) {
                    results.add(restriction);
                }
            }
            else {  // owl:hasValue or owl:someValuesFrom cannot be overloaded
                results.add(restriction);
            }
        }

        for (Iterator it = cls.getNamedSuperclasses().iterator(); it.hasNext();) {
            RDFSNamedClass superclass = (RDFSNamedClass) it.next();
            if (superclass instanceof OWLNamedClass && !reached.contains(superclass)) {
                getRestrictions((OWLNamedClass) superclass, reached, results);
            }
        }
    }


    public RDFResource getSomeValuesFrom(RDFProperty property) {
        Collection restrictions = getRestrictions(property, true);
        for (Iterator it = restrictions.iterator(); it.hasNext();) {
            OWLRestriction restriction = (OWLRestriction) it.next();
            if (restriction instanceof OWLSomeValuesFrom) {
                return ((OWLSomeValuesFrom) restriction).getSomeValuesFrom();
            }
        }
        return null;
    }


    public boolean isConsistent() {
        return getInferredSuperclasses().contains(getOWLModel().getOWLNothing()) == false;
    }


    private static boolean isOverloadedAllValuesFrom(OWLAllValuesFrom restriction, Collection restrictions) {
        RDFProperty property = restriction.getOnProperty();
        for (Iterator it = restrictions.iterator(); it.hasNext();) {
            OWLRestriction owlRestriction = (OWLRestriction) it.next();
            if (owlRestriction instanceof OWLAllValuesFrom && owlRestriction.getOnProperty().equals(property)) {
                return true;
            }
        }
        return false;
    }


    private static boolean isOverloadedCardinality(OWLCardinality restriction, Collection restrictions) {
        RDFProperty property = restriction.getOnProperty();
        for (Iterator it = restrictions.iterator(); it.hasNext();) {
            OWLRestriction owlRestriction = (OWLRestriction) it.next();
            if (property.equals(owlRestriction.getOnProperty())) {
                if (owlRestriction instanceof OWLCardinality) {
                    return true;
                }
            }
        }
        return false;
    }


    private static boolean isOverloadedMaxCardinality(OWLMaxCardinality restriction, Collection restrictions) {
        RDFProperty property = restriction.getOnProperty();
        for (Iterator it = restrictions.iterator(); it.hasNext();) {
            OWLRestriction owlRestriction = (OWLRestriction) it.next();
            if (property.equals(owlRestriction.getOnProperty())) {
                if (owlRestriction instanceof OWLCardinality || owlRestriction instanceof OWLMaxCardinality) {
                    return true;
                }
            }
        }
        return false;
    }


    private static boolean isOverloadedMinCardinality(OWLMinCardinality restriction, Collection restrictions) {
        RDFProperty property = restriction.getOnProperty();
        for (Iterator it = restrictions.iterator(); it.hasNext();) {
            OWLRestriction owlRestriction = (OWLRestriction) it.next();
            if (property.equals(owlRestriction.getOnProperty())) {
                if (owlRestriction instanceof OWLCardinality || owlRestriction instanceof OWLMinCardinality) {
                    return true;
                }
            }
        }
        return false;
    }


    public boolean getSubclassesDisjoint() {
        final Slot slot = getOWLModel().getProtegeSubclassesDisjointProperty();
        if (slot != null) {
            final Object value = getDirectOwnSlotValue(slot);
            if (value instanceof Boolean) {
                return ((Boolean) value).booleanValue();
            }
        }
        return false;
    }


    public boolean hasNamedSuperclass() {
        return getNamedSuperclasses().size() > 0;
    }


    public boolean isDefinedClass() {
        return getDefinition() != null;
    }


    @Override
    public boolean isFunctionalProperty(RDFProperty property) {
        int max = getMaxCardinality(property);
        return max == 0 || max == 1;
    }


    public boolean hasNamedSuperClass() {
        return hasNamedSuperclass();
    }


    public boolean isProbeClass() {
        Slot slot = getKnowledgeBase().getSlot(ProtegeNames.getProbeClassSlotName());
        if (slot instanceof OWLDatatypeProperty) {
            return Boolean.TRUE.equals(getDirectOwnSlotValue(slot));
        }
        return false;
    }


    public void removeDisjointClass(RDFSClass aClass) {
        Slot disjointClassesSlot = getOWLModel().getOWLDisjointWithProperty();
        removeOwnSlotValue(disjointClassesSlot, aClass);
        if (aClass.isAnonymous()) {
            getKnowledgeBase().deleteCls(aClass);
        }
    }


    public void removeEquivalentClass(final RDFSClass equivalentClass) {
        new Transaction(getOWLModel(), "Remove Equivalent Class" + Transaction.APPLY_TO_TRAILER_STRING + this.getName()) {
            public boolean doOperations() {
		        if (equivalentClass instanceof OWLAnonymousClass) {
		            removeDirectSuperclass(equivalentClass);
		        }
		        else {
		            removeDirectSuperclass(equivalentClass);
		            equivalentClass.removeSuperclass(DefaultOWLNamedClass.this);
		        }
		        return true;
            };
        }.execute();
    }


    public void removeInferredSuperclass(RDFSClass superclass) {
        Slot superclassesSlot = getAbstractOWLModel().getProtegeInferredSuperclassesProperty();
        Slot subclassesSlot = getAbstractOWLModel().getProtegeInferredSubclassesProperty();
        removeOwnSlotValue(superclassesSlot, superclass);
        ((Cls) superclass).removeOwnSlotValue(subclassesSlot, this);
    }


    public void setClassificationStatus(int value) {
        if (value != getClassificationStatus()) {
            final Slot slot = getAbstractOWLModel().getProtegeClassificationStatusProperty();
            setOwnSlotValue(slot, new Integer(value));
        }
    }


    public void setDefinition(RDFSClass definingClass) {
        for (Iterator it = getEquivalentClasses().iterator(); it.hasNext();) {
            RDFSClass cls = (RDFSClass) it.next();
            removeEquivalentClass(cls);
        }
        if (definingClass != null) {
            addEquivalentClass(definingClass);
        }
        if (!hasNamedSuperClass()) {
            addDirectSuperclass(getKnowledgeBase().getRootCls());
        }
    }


    public void setSubclassesDisjoint(boolean value) {
        final Slot slot = getOWLModel().getProtegeSubclassesDisjointProperty();
        if (slot == null) {
            throw new RuntimeException("Could not find slot " + ProtegeNames.getSubclassesDisjointSlotName());
        }
        if (value) {
            setDirectOwnSlotValue(slot, Boolean.TRUE);
            OWLUtil.ensureSubclassesDisjoint(this);
        }
        else {
            setDirectOwnSlotValues(slot, Collections.EMPTY_LIST);
            OWLUtil.removeSubclassesDisjoint(this);
        }
    }


    @Override
    public void accept(OWLModelVisitor visitor) {
        visitor.visitOWLNamedClass(this);
    }
}
