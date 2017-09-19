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
import edu.stanford.smi.protege.model.framestore.MergingNarrowFrameStore;
import edu.stanford.smi.protege.model.framestore.NarrowFrameStore;
import edu.stanford.smi.protege.ui.FrameComparator;
import edu.stanford.smi.protege.ui.ProjectManager;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.URIUtilities;
import edu.stanford.smi.protegex.owl.jena.JenaKnowledgeBaseFactory;
import edu.stanford.smi.protegex.owl.model.*;
import edu.stanford.smi.protegex.owl.model.classparser.OWLClassParser;
import edu.stanford.smi.protegex.owl.model.event.PropertyValueAdapter;
import edu.stanford.smi.protegex.owl.model.event.PropertyValueListener;
import edu.stanford.smi.protegex.owl.model.event.ResourceAdapter;
import edu.stanford.smi.protegex.owl.model.event.ResourceListener;
import edu.stanford.smi.protegex.owl.model.triplestore.TripleStore;
import edu.stanford.smi.protegex.owl.model.triplestore.TripleStoreModel;
import edu.stanford.smi.protegex.owl.model.triplestore.TripleStoreUtil;
import edu.stanford.smi.protegex.owl.ui.ProtegeUI;
import edu.stanford.smi.protegex.owl.ui.profiles.OWLProfiles;
import edu.stanford.smi.protegex.owl.ui.profiles.ProfilesManager;
import edu.stanford.smi.protegex.owl.util.OWLFrameStoreUtils;

import javax.swing.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A collection of static utility methods for OWL classes. Many of them are used
 * to "simulate" the multiple inheritance between the various implementations of
 * RDFResource.
 *
 * @author Holger Knublauch <holger@knublauch.com>
 * @author Hemed Al Ruwehy
 */
public class OWLUtil {
    public static final Logger LOGGER = Log.getLogger(OWLUtil.class);

    public static void addIsDefinedBy(Instance source, Instance instance) {
        OWLModel owlModel = (OWLModel) source.getKnowledgeBase();
        source.addOwnSlotValue(owlModel.getRDFSIsDefinedByProperty(), instance);
    }

    public static void addLabel(RDFResource source, String label, String language) {
        Object value = null;
        if (language != null && language.trim().length() > 0) {
            value = source.getOWLModel().createRDFSLiteralOrString(label, language.trim());
        } else {
            value = label;
        }
        RDFProperty property = source.getOWLModel().getRDFSLabelProperty();
        if (!source.getPropertyValues(property).contains(value)) {
            source.addPropertyValue(property, value);
        }
    }

    public static void addSameAs(Instance source, Instance instance) {
        OWLModel owlModel = (OWLModel) source.getKnowledgeBase();
        source.addOwnSlotValue(owlModel.getOWLSameAsProperty(), instance);
    }

    public static void addVersionInfo(Instance source, String versionInfo) {
        OWLModel owlModel = (OWLModel) source.getKnowledgeBase();
        source.addOwnSlotValue(owlModel.getOWLVersionInfoProperty(), versionInfo);
    }

    public static RDFUntypedResource assignUniqueURI(RDFUntypedResource eri) {
        OWLModel owlModel = eri.getOWLModel();
        String prefix = "http://protege.stanford.edu/";
        String suffix = "";
        int index = 1;
        while (owlModel.getRDFUntypedResource(prefix + index + suffix, false) != null) {
            index++;
        }
        return (RDFUntypedResource) eri.rename(prefix + index + suffix);
    }

    /**
     * @deprecated use the version with project parameter instead
     */
    @Deprecated
    public static boolean confirmSaveAndReload() {
        return confirmSaveAndReload(null);
    }

    public static boolean confirmSaveAndReload(Project project) {
        return ProtegeUI.getModalDialogFactory().showConfirmDialog(
                (OWLModel) project.getKnowledgeBase(),
                "This change will not take effect until you save and\n"
                        + "reload your project.  Do you want to do this now?", "Confirm Reload");
    }

    public static void copyAnnotations(Instance source, Instance target) {
        for (Iterator it = source.getKnowledgeBase().getSlots().iterator(); it.hasNext();) {
            Slot slot = (Slot) it.next();
            if (slot instanceof OWLProperty && ((OWLProperty) slot).isAnnotationProperty()) {
                Collection values = source.getDirectOwnSlotValues(slot);
                if (values.size() > 0) {
                    target.setOwnSlotValues(slot, values);
                }
            }
        }
    }

    public static void convertEquivalentClsIntoSuperClses(RDFSClass ownerClass, RDFSClass equivalentClass) {
        if (equivalentClass instanceof OWLIntersectionClass) {
            OWLIntersectionClass intersectionCls = (OWLIntersectionClass) equivalentClass;
            Collection neo = new ArrayList();
            for (Object element : intersectionCls.getOperands()) {
                RDFSClass operand = (RDFSClass) element;
                RDFSClass copy = operand.createClone();
                neo.add(copy);
            }
            intersectionCls.delete();
            for (Iterator it = neo.iterator(); it.hasNext();) {
                RDFSClass aClass = (RDFSClass) it.next();
                ownerClass.addSuperclass(aClass);
            }
        } else {
            equivalentClass.removeSuperclass(ownerClass);
        }
        /*
        Cls rootCls = ownerClassssss.getKnowledgeBase().getRootCls();
        if(ownerClassssss.hasDirectSuperclass(rootCls)) {
            ownerClassssss.removeDirectSuperclass(rootCls);
        } */
    }

    public static void convertSuperClsIntoEquivalentCls(OWLNamedClass ownerCls, RDFSClass superClass) {
        RDFSClass definition = ownerCls.getDefinition();
        convertSuperClsIntoEquivalentCls(ownerCls, superClass, definition);
    }

    public static void convertSuperClsIntoEquivalentCls(OWLNamedClass ownerCls, RDFSClass superClass,
            RDFSClass definition) {
        if (definition == null) {
            superClass.addSuperclass(ownerCls);
        } else {
            RDFSClass copy = superClass.createClone();
            Collection operands = new ArrayList();
            OWLModel owlModel = ownerCls.getOWLModel();
            if (definition instanceof OWLIntersectionClass) {
                OWLIntersectionClass intersection = (OWLIntersectionClass) definition;
                for (Object element : intersection.getOperands()) {
                    RDFSClass oldOperand = (RDFSClass) element;
                    RDFSClass newOperand = oldOperand.createClone();
                    operands.add(newOperand);
                }
            } else {
                RDFSClass equiCopy = definition.createClone();
                operands.add(equiCopy);
            }
            operands.add(copy);
            OWLIntersectionClass intersection = owlModel.createOWLIntersectionClass(operands);
            ownerCls.addEquivalentClass(intersection);
            ownerCls.removeSuperclass(definition);
            ownerCls.removeSuperclass(superClass);
        }
    }

    /**
     * Creates a copy of a given class and also copies the annotation property
     * values.
     *
     * @param cls
     *            the class to clone
     * @return a clone of cls or cls itself if it is a named class
     * @deprecated use CloneFactory and ResourceCopier instead
     */
    @Deprecated
    public static edu.stanford.smi.protege.model.Cls createClone(edu.stanford.smi.protege.model.Cls cls) {
        String expression = cls.getBrowserText();
        OWLModel owlModel = (OWLModel) cls.getKnowledgeBase();
        edu.stanford.smi.protege.model.Cls clone = createClone(owlModel, expression);
        copyAnnotations(cls, clone);
        return clone;
    }

    /**
     * @param owlModel
     * @param expression
     * @deprecated use CloneFactory and ResourceCopier instead
     */
    @Deprecated
    public static RDFSClass createClone(OWLModel owlModel, String expression) {
        OWLClassParser parser = owlModel.getOWLClassParser();
        try {
            return parser.parseClass(owlModel, expression);
        } catch (Exception ex) {
            System.err.println("Error in OWLUtil.createClone (" + expression + ")");
            ex.printStackTrace();
            return null;
        }
    }

    public static void ensureSubclassesDisjoint(OWLNamedClass cls) {

        List subclasses = getPotentiallyDisjointSubclasses(cls);

        for (Iterator outer = subclasses.iterator(); outer.hasNext();) {
            OWLNamedClass outerCls = (OWLNamedClass) outer.next();
            if (!outerCls.isSystem()) {
                Collection disjoints = outerCls.getDisjointClasses();
                for (Iterator inner = subclasses.iterator(); inner.hasNext();) {
                    RDFSNamedClass innerClass = (RDFSNamedClass) inner.next();
                    if (!outerCls.equals(innerClass) && !disjoints.contains(innerClass)) {
                        outerCls.addDisjointClass(innerClass);
                    }
                }
            }
        }
    }

    public static boolean equalsStructurally(Collection collection1, Collection collection2) {
        if (collection1.size() == collection2.size()) {
            for (Iterator it1 = collection1.iterator(); it1.hasNext();) {
                RDFObject object1 = (RDFObject) it1.next();
                boolean found = false;
                for (Iterator it2 = collection2.iterator(); it2.hasNext();) {
                    RDFObject object2 = (RDFObject) it2.next();
                    if (object1.equalsStructurally(object2)) {
                        found = true;
                        break;
                    }
                }
                if (found == false) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    public static Collection getDocumentation(Instance source) {
        OWLModel owlModel = (OWLModel) source.getKnowledgeBase();
        return source.getOwnSlotValues(owlModel.getRDFSCommentProperty());
    }

    public static Collection getInferredDirectTypes(RDFResource resource) {
        RDFProperty inferredTypesProperty = resource.getOWLModel().getRDFProperty(ProtegeNames.Slot.INFERRED_TYPE);
        return resource.getPropertyValues(inferredTypesProperty);
    }

    public static Collection getIsDefinedBy(Instance source) {
        OWLModel owlModel = (OWLModel) source.getKnowledgeBase();
        return source.getOwnSlotValues(owlModel.getRDFSIsDefinedByProperty());
    }

    public static Collection getLabels(RDFResource source) {
        return source.getPropertyValues(source.getOWLModel().getRDFSLabelProperty());
    }

    /**
     * Gets all paths from a given class to the root. This is needed if the user
     * requests to see all occurances of a class in a tree.
     *
     * @param cls
     *            the Cls to find all paths to
     * @return a Collection of List objects, with each List starting at cls
     */
    public static Collection<List<Cls>> getPathsToRoot(Cls cls) {
        Collection<List<Cls>> results = new ArrayList<List<Cls>>();
        return getPathToRoot(cls, new LinkedList<Cls>(), results);
    }

    private static Collection<List<Cls>> getPathToRoot(Cls cls, LinkedList<Cls> list, Collection<List<Cls>> lists) {
        list.add(0, cls);
        Iterator<Cls> i = cls.getDirectSuperclasses().iterator();
        Cls rootCls = cls.getKnowledgeBase().getRootCls();
        while (i.hasNext()) {
            Cls superclass = i.next();
            if (cls.isVisible()) {
                List<Cls> copy = new ArrayList<Cls>(list);
                getPathToRoot(superclass, list, lists);
                if (list.getFirst().equals(rootCls)) {
                    lists.add(list);
                } else {
                    // Backtracking
                    list.clear();
                    list.addAll(copy);
                }
            }
        }
        return lists;
    }

    public static List getPotentiallyDisjointSubclasses(OWLNamedClass cls) {
        List subclasses = new ArrayList();
        Iterator it = cls.getNamedSubclasses().iterator();
        while (it.hasNext()) {
            RDFSNamedClass subClass = (RDFSNamedClass) it.next();
            if (subClass instanceof OWLNamedClass && ((OWLNamedClass) subClass).getDefinition() == null
                    && subClass.isVisible()) {
                subclasses.add(subClass);
            }
        }
        return subclasses;
    }

    public static Set getReferringAnonymousClses(RDFResource instance) {
        final Slot directSubclassesSlot = ((Instance) instance).getKnowledgeBase()
                .getSlot(Model.Slot.DIRECT_SUBCLASSES);
        final RDFProperty rdfFirstProperty = instance.getOWLModel().getRDFFirstProperty();
        Set result = new HashSet();
        for (Iterator it = instance.getReferences().iterator(); it.hasNext();) {
            Reference reference = (Reference) it.next();
            if (reference.getFrame() instanceof OWLAnonymousClass) {
                if (!directSubclassesSlot.equals(reference.getSlot())) {
                    result.add(reference.getFrame());
                }
            } else if (reference.getFrame() instanceof RDFList && rdfFirstProperty.equals(reference.getSlot())) {
                RDFList list = (RDFList) reference.getFrame();
                RDFList start = list.getStart();
                getReferringLogicalClasses(start, result);
            }
        }
        return result;
    }

    public static void getReferringLogicalClasses(RDFList list, Set set) {
        KnowledgeBase kb = list.getOWLModel();
        Collection refs = kb.getReferences(list, 1000);
        for (Iterator it = refs.iterator(); it.hasNext();) {
            Reference ref = (Reference) it.next();
            if (ref.getFrame() instanceof OWLNAryLogicalClass || ref.getFrame() instanceof OWLEnumeratedClass) {
                set.add(ref.getFrame());
            }
        }
    }

    public static Collection getSameAs(Instance source) {
        OWLModel owlModel = (OWLModel) source.getKnowledgeBase();
        return source.getOwnSlotValues(owlModel.getOWLSameAsProperty());
    }

    public static Collection getSelectableNamedClses(OWLModel owlModel) {
        Collection clses = owlModel.getUserDefinedRDFSNamedClasses();
        clses.add(owlModel.getOWLThingClass());
        if (owlModel.getOWLNamedClassClass().isVisible()) {
            clses.add(owlModel.getOWLNamedClassClass());
        }
        if (owlModel.getRDFPropertyClass().isVisible()) {
            clses.add(owlModel.getOWLDatatypePropertyClass());
            clses.add(owlModel.getOWLObjectPropertyClass());
        }
        return clses;
    }

    public static Collection getVersionInfo(Instance source) {
        OWLModel owlModel = (OWLModel) source.getKnowledgeBase();
        return source.getOwnSlotValues(owlModel.getOWLVersionInfoProperty());
    }

    public static boolean hasDirectRestriction(edu.stanford.smi.protege.model.Cls cls, Slot slot,
            edu.stanford.smi.protege.model.Cls metaCls) {
        for (Object element : cls.getDirectSuperclasses()) {
            edu.stanford.smi.protege.model.Cls superCls = (edu.stanford.smi.protege.model.Cls) element;
            if (superCls.getDirectType().equals(metaCls)) {
                OWLRestriction restriction = (OWLRestriction) superCls;
                if (restriction.getOnProperty().equals(slot)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean hasOWLDLProfile(OWLModel owlModel) {
        return ProfilesManager.isFeatureSupported(owlModel, OWLProfiles.OWL_DL) && !hasOWLFullProfile(owlModel);
    }

    public static boolean hasOWLFullProfile(OWLModel owlModel) {
        return ProfilesManager.isFeatureSupported(owlModel, OWLProfiles.OWL_Full);
    }

    public static boolean hasRDFProfile(OWLModel owlModel) {
        return ProfilesManager.isFeatureSupported(owlModel, OWLProfiles.RDF_but_not_OWL);
    }

    public static boolean isInconsistent(edu.stanford.smi.protege.model.Cls cls) {
        final Slot slot = ((AbstractOWLModel) cls.getKnowledgeBase()).getSlot(ProtegeNames.Slot.INFERRED_SUPERCLASSES);
        Collection values = cls.getDirectOwnSlotValues(slot);
        if (values.contains(((AbstractOWLModel) cls.getKnowledgeBase()).getOWLNothing())) {
            return true;
        } else {
            return false;
        }
    }

    public static void printStackTrace() {
        try {
            throw new RuntimeException("Debugging purposes only");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void removeIsDefinedBy(Instance source, Instance instance) {
        OWLModel owlModel = (OWLModel) source.getKnowledgeBase();
        source.removeOwnSlotValue(owlModel.getRDFSIsDefinedByProperty(), instance);
    }

    public static void removeLabel(RDFResource source, String label, String language) {
        Object value = null;
        if (language != null && language.trim().length() > 0) {
            value = source.getOWLModel().createRDFSLiteralOrString(label, language.trim());
        } else {
            value = label;
        }
        source.removePropertyValue(source.getOWLModel().getRDFSLabelProperty(), value);
    }

    public static void removeSameAs(Instance source, Instance instance) {
        OWLModel owlModel = (OWLModel) source.getKnowledgeBase();
        source.removeOwnSlotValue(owlModel.getOWLSameAsProperty(), instance);
    }

    public static void removeSubclassesDisjoint(OWLNamedClass cls) {

        List subclasses = new ArrayList();
        Iterator it = cls.getNamedSubclasses().iterator();
        while (it.hasNext()) {
            RDFSNamedClass subClass = (RDFSNamedClass) it.next();
            if (subClass instanceof OWLNamedClass && ((OWLNamedClass) subClass).getDefinition() == null
                    && subClass.isVisible()) {
                subclasses.add(subClass);
            }
        }

        TripleStoreModel tsm = cls.getOWLModel().getTripleStoreModel();
        RDFProperty owlDisjointWithProperty = cls.getOWLModel().getOWLDisjointWithProperty();
        for (Iterator outer = subclasses.iterator(); outer.hasNext();) {
            OWLNamedClass outerCls = (OWLNamedClass) outer.next();
            if (outerCls.isEditable()) {
                Collection disjoints = outerCls.getDisjointClasses();
                for (Iterator inner = subclasses.iterator(); inner.hasNext();) {
                    OWLNamedClass innerCls = (OWLNamedClass) inner.next();
                    if (!outerCls.equals(innerCls) && disjoints.contains(innerCls)) {
                        if (tsm.isEditableTriple(outerCls, owlDisjointWithProperty, innerCls)) {
                            outerCls.removeDisjointClass(innerCls);
                        }
                    }
                }
            }
        }
    }

    public static void removeVersionInfo(Instance source, String versionInfo) {
        OWLModel owlModel = (OWLModel) source.getKnowledgeBase();
        source.removeOwnSlotValue(owlModel.getOWLVersionInfoProperty(), versionInfo);
    }

    public static void resetComputedSuperclasses(OWLModel owlModel) {
        resetComputedSuperclasses(owlModel, owlModel.getUserDefinedOWLNamedClasses());
    }

    public static void resetComputedSuperclasses(OWLModel owlModel, Collection clses) {
        AbstractOWLModel abstractOWLModel = (AbstractOWLModel) owlModel;
        Slot computedSubClassesSlot = abstractOWLModel.getProtegeInferredSubclassesProperty();
        Slot computedSuperClassesSlot = abstractOWLModel.getProtegeInferredSuperclassesProperty();
        for (Iterator it = clses.iterator(); it.hasNext();) {
            OWLNamedClass owlCls = (OWLNamedClass) it.next();
            ((Cls) owlCls).setOwnSlotValues(computedSubClassesSlot, Collections.EMPTY_LIST);
            ((Cls) owlCls).setOwnSlotValues(computedSuperClassesSlot, Collections.EMPTY_LIST);
        }
        ((Cls) owlModel.getOWLThingClass()).setOwnSlotValues(computedSubClassesSlot, Collections.EMPTY_LIST);
        ((Cls) owlModel.getOWLNothing()).setOwnSlotValues(computedSubClassesSlot, Collections.EMPTY_LIST);
    }

    // TODO: May need to deprecate this later for multi-file systems like Eclipse
    public static boolean saveAndReloadProject() {
        final ProjectManager projectManager = ProjectManager.getProjectManager();
        if (projectManager.saveProjectRequest()) {
            URI uri = projectManager.getCurrentProject().getProjectURI();
            if (projectManager.closeProjectRequest()) {
                projectManager.loadProject(uri);
                return true;
            }
        }
        return false;
    }

    public static void setDocumentation(Instance source, Collection values) {
        OWLModel owlModel = (OWLModel) source.getKnowledgeBase();
        source.setOwnSlotValues(owlModel.getRDFSCommentProperty(), values);
    }

    public static void setConsistentClassificationStatus(OWLNamedClass namedCls) {
        if (namedCls.isMetaclass()) {
            namedCls.setClassificationStatus(OWLNames.CLASSIFICATION_STATUS_CONSISTENT_AND_UNCHANGED);
        } else {
            Collection asserted = namedCls.getNamedSuperclasses();
            Collection inferred = namedCls.getInferredSuperclasses();
            Collection diff = new ArrayList(inferred);
            diff.removeAll(asserted);
            int status = diff.size() == 0 ? OWLNames.CLASSIFICATION_STATUS_CONSISTENT_AND_UNCHANGED
                    : OWLNames.CLASSIFICATION_STATUS_CONSISTENT_AND_CHANGED;
            namedCls.setClassificationStatus(status);
        }
    }

    public static void sortSubclasses(Cls superCls) {
        if (superCls.getDirectSubclassCount() > 1) {
            List oldSuperclasses = new ArrayList(superCls.getDirectSubclasses());
            List clses = new ArrayList(oldSuperclasses);
            Collections.sort(clses, new FrameComparator() {
                @Override
                public int compare(Frame o1, Frame o2) {
                    if (o1 instanceof RDFSNamedClass && o2 instanceof RDFSNamedClass) {
                        return ((RDFSNamedClass) o1).getBrowserText().compareTo(((RDFSNamedClass) o2).getBrowserText());
                    } else if (o1 instanceof RDFSNamedClass) {
                        return 1;
                    } else if (o2 instanceof RDFSNamedClass) {
                        return -1;
                    } else {
                        return 0;
                    }
                }
            });
            for (int i = 0; i < clses.size() - 1; i++) {
                Cls a = (Cls) clses.get(i);
                Cls b = (Cls) clses.get(i + 1);
                superCls.moveDirectSubclass(b, a);
            }
        }
    }

    public static void addProtegeType(RDFResource resource, RDFSClass type) {
        ((KnowledgeBase) resource.getOWLModel()).addDirectType(resource, type);
    }

    public static RDFSClass getDirectRDFType(RDFResource resource) {
        return (RDFSClass) ((KnowledgeBase) resource.getOWLModel()).getDirectType(resource);
    }

    public static Collection getDirectRDFTypes(RDFResource resource) {
        return ((KnowledgeBase) resource.getOWLModel()).getDirectTypes(resource);
    }

    public static boolean hasProtegeType(RDFResource resource, RDFSClass type) {
        return ((KnowledgeBase) resource.getOWLModel()).hasDirectType(resource, type);
    }

    public static boolean hasProtegeType(RDFResource resource, RDFSClass type, boolean includingSuperclasses) {
        if (includingSuperclasses) {
            return ((KnowledgeBase) resource.getOWLModel()).hasType(resource, type);
        } else {
            return ((KnowledgeBase) resource.getOWLModel()).hasDirectType(resource, type);
        }
    }

    public static void removeProtegeType(RDFResource resource, RDFSClass type) {
        ((KnowledgeBase) resource.getOWLModel()).removeDirectType(resource, type);
    }

    public static void setProtegeType(RDFResource resource, RDFSClass type) {
        ((KnowledgeBase) resource.getOWLModel()).setDirectType(resource, type);
    }

    public static void setProtegeTypes(RDFResource resource, Collection types) {
        ((KnowledgeBase) resource.getOWLModel()).setDirectTypes(resource, types);
    }

    public static void addDifferentFrom(RDFResource resource, RDFResource differentFrom) {
        OWLModel owlModel = resource.getOWLModel();
        ((KnowledgeBase) owlModel).addOwnSlotValue(resource, owlModel.getOWLDifferentFromProperty(), differentFrom);
    }

    public static void removeDifferentFrom(RDFResource resource, RDFResource differentFrom) {
        OWLModel owlModel = resource.getOWLModel();
        ((KnowledgeBase) owlModel).removeOwnSlotValue(resource, owlModel.getOWLDifferentFromProperty(), differentFrom);
    }

    public static void addComment(RDFResource resource, String comment) {
        OWLModel owlModel = resource.getOWLModel();
        ((KnowledgeBase) owlModel).addOwnSlotValue(resource, owlModel.getRDFSCommentProperty(), comment);
    }

    public static void removeComment(RDFResource resource, String comment) {
        OWLModel owlModel = resource.getOWLModel();
        ((KnowledgeBase) owlModel).removeOwnSlotValue(resource, owlModel.getRDFSCommentProperty(), comment);
    }

    public static void addPropertyValue(RDFResource resource, RDFProperty property, Object value) {
        KnowledgeBase kb = resource.getOWLModel();
        kb.addOwnSlotValue(resource, property, value);
    }

    public static Collection getComments(RDFResource resource) {
        OWLModel owlModel = resource.getOWLModel();
        return resource.getPropertyValues(owlModel.getRDFSCommentProperty());
    }

    public static void setComment(RDFResource resource, String comment) {
        OWLModel owlModel = resource.getOWLModel();
        resource.setPropertyValues(owlModel.getRDFSCommentProperty(),
                comment == null ? (Collection) Collections.EMPTY_LIST : (Collection) Collections.singleton(comment));
    }

    public static void setComments(RDFResource resource, Collection comments) {
        OWLModel owlModel = resource.getOWLModel();
        resource.setPropertyValues(owlModel.getRDFSCommentProperty(), comments);
    }

    public static void setInferredTypes(RDFResource resource, Collection types) {
        RDFProperty inferredTypesProperty = resource.getOWLModel().getRDFProperty(ProtegeNames.Slot.INFERRED_TYPE);
        resource.setPropertyValues(inferredTypesProperty, types);
    }

    public static Collection getDifferentFrom(RDFResource resource) {
        OWLModel owlModel = resource.getOWLModel();
        return resource.getPropertyValues(owlModel.getRDFProperty(OWLNames.Slot.DIFFERENT_FROM));
    }

    public static Object getPropertyValue(RDFResource resource, RDFProperty property, boolean includingSubproperties) {
        Collection values = getPropertyValues(resource, property, includingSubproperties);
        return values.isEmpty() ? null : values.iterator().next();
    }

    public static int getPropertyValueCount(RDFResource resource, RDFProperty property) {
        return ((KnowledgeBase) resource.getOWLModel()).getDirectOwnSlotValues(resource, property).size();
    }

    @SuppressWarnings("deprecation")
    public static Collection getPropertyValues(RDFResource resource, RDFProperty property,
            boolean includingSubproperties) {
        OWLModel owlModel = resource.getOWLModel();
        Collection values;
        if (includingSubproperties) {
            values = owlModel.getOwnSlotValues(resource, property);
        } else {
            values = owlModel.getDirectOwnSlotValues(resource, property);
        }
        return OWLFrameStoreUtils.convertValueListToRDFLiterals(owlModel, values);
    }

    public static Iterator listPropertyValues(RDFResource resource, RDFProperty property, boolean includingSubproperties) {
        return OWLUtil.getPropertyValues(resource, property, includingSubproperties).iterator();
    }

    public static void removePropertyValue(RDFResource resource, RDFProperty property, Object value) {
        KnowledgeBase kb = resource.getOWLModel();
        if (value instanceof RDFSLiteral) {
            value = ((DefaultRDFSLiteral) value).getRawValue();
        }
        kb.removeOwnSlotValue(resource, property, value);
    }

    public static void setPropertyValue(RDFResource resource, RDFProperty property, Object value) {
        KnowledgeBase owlModel = resource.getOWLModel();
        owlModel.setOwnSlotValues(resource, property, value == null ? (Collection) Collections.EMPTY_LIST : Collections
                .singleton(value));
    }

    public static void setPropertyValues(RDFResource resource, RDFProperty property, Collection values) {
        KnowledgeBase kb = resource.getOWLModel();
        kb.setOwnSlotValues(resource, property, values);
    }

    public static boolean isSystem(Frame resource) {
        MergingNarrowFrameStore mnfs = MergingNarrowFrameStore.get(resource.getKnowledgeBase());
        NarrowFrameStore systemFrameStore = mnfs.getSystemFrameStore();
        Slot directTypeSlot = resource.getKnowledgeBase().getSlot(Model.Slot.DIRECT_TYPES);
        return systemFrameStore.getValuesCount(resource, directTypeSlot, null, false) > 0;
    }

    public static Collection getPropertyValueLiterals(RDFResource resource, RDFProperty property) {
        return ((AbstractOWLModel) resource.getOWLModel()).getPropertyValueLiterals(resource, property);
    }

    public static RDFSLiteral getPropertyValueLiteral(RDFResource resource, RDFProperty property) {
        Object value = getPropertyValue(resource, property, false);
        if (value != null) {
            if (value instanceof RDFSLiteral) {
                return (RDFSLiteral) value;
            } else {
                return resource.getOWLModel().createRDFSLiteral(value);
            }
        }
        return null;
    }

    public static Collection getRDFProperties(RDFResource resource) {
        Collection<RDFProperty> result = new ArrayList<RDFProperty>();
        final OWLModel owlModel = resource.getOWLModel();
        final Collection properties = new ArrayList(owlModel.getRDFProperties());
        if (!(resource instanceof RDFProperty)) {
            properties.remove(owlModel.getRDFSSubPropertyOfProperty());
        }
        for (Iterator it = properties.iterator(); it.hasNext();) {
            Object o = it.next();
            if (!(o instanceof RDFProperty)) {
                LOGGER.warning("Looking at properties \"of\" " + resource + " and found non-property " + o);
            }
            else {
                RDFProperty property = (RDFProperty) o;
                if (resource.getPropertyValueCount(property) > 0) {
                    result.add(property);
                }
            }
        }
        return result;
    }

    public static void addResourceListener(RDFResource resource, ResourceListener listener) {
        if (!(listener instanceof ResourceAdapter)) {
            throw new IllegalArgumentException("Listener must be a ResourceAdapter");
        }
        ((Instance) resource).addFrameListener(listener);
    }

    public static void removeResourceListener(RDFResource resource, ResourceListener listener) {
        if (!(listener instanceof ResourceAdapter)) {
            throw new IllegalArgumentException("Listener must be a ResourceAdapter");
        }
        ((Instance) resource).removeFrameListener(listener);
    }

    public static boolean hasPropertyValue(RDFResource resource, RDFProperty property, Object value,
            boolean includingSubproperties) {
        return resource.getPropertyValues(property, includingSubproperties).contains(value);
    }

    public static void addPropertyValueListener(RDFResource resource, PropertyValueListener listener) {
        if (!(listener instanceof PropertyValueAdapter)) {
            throw new IllegalArgumentException("Listener must be a PropertyValueAdapter");
        }
        ((Instance) resource).addFrameListener(listener);
    }

    public static void removePropertyValueListener(RDFResource resource, PropertyValueListener listener) {
        ((Instance) resource).removeFrameListener(listener);
    }

    public static Collection getPossibleRDFProperties(RDFResource resource) {
        Set result = new HashSet();
        for (Iterator it = resource.getProtegeTypes().iterator(); it.hasNext();) {
            RDFSClass type = (RDFSClass) it.next();
            Collection ps = type.getUnionDomainProperties(true);
            result.addAll(ps);
            for (Iterator pit = ps.iterator(); pit.hasNext();) {
                RDFProperty property = (RDFProperty) pit.next();
                for (Iterator sit = property.getSubproperties(true).iterator(); sit.hasNext();) {
                    RDFProperty subproperty = (RDFProperty) sit.next();
                    if (subproperty.getDomain(false) == null) {
                        result.add(subproperty);
                    }
                }
            }
        }
        return result;
    }

    public static RDFResource as(RDFResource resource, Class javaInterface) {
        return resource.getOWLModel().getOWLJavaFactory().as(resource, javaInterface); // TODO
    }

    public static boolean canAs(RDFResource resource, Class javaInterface) {
        return resource.getOWLModel().getOWLJavaFactory().canAs(resource, javaInterface); // TODO
    }

    public static Collection getPropertyValuesAs(RDFResource resource, RDFProperty property, Class javaInterface) {
        Collection results = new ArrayList();
        for (Iterator it = resource.getPropertyValues(property).iterator(); it.hasNext();) {
            Object o = it.next();
            if (o instanceof RDFResource) {
                results.add(((RDFResource) o).as(javaInterface));
            } else {
                results.add(o);
            }
        }
        return results;
    }

    public static RDFResource getPropertyValueAs(RDFResource resource, RDFProperty property, Class javaInterface) {
        RDFResource r = (RDFResource) resource.getPropertyValue(property);
        if (r != null) {
            return r.as(javaInterface);
        } else {
            return null;
        }
    }

    public static Iterator listPropertyValuesAs(RDFResource resource, RDFProperty property, Class javaInterface) {
        return getPropertyValuesAs(resource, property, javaInterface).iterator();
    }

    public static boolean hasPropertyValue(RDFResource resource, RDFProperty property) {
        return resource.getPropertyValueCount(property) > 0;
    }

    public static boolean hasPropertyValue(RDFResource resource, RDFProperty property, boolean includingSubproperties) {
        if (includingSubproperties) {
            return resource.getPropertyValues(property, includingSubproperties).size() > 0;
        } else {
            return hasPropertyValue(resource, property);
        }
    }

    public static RDFSClass getRDFType(RDFResource resource) {
        return (RDFSClass) resource.getPropertyValue(resource.getOWLModel().getRDFTypeProperty());
    }

    public static Collection getRDFTypes(RDFResource resource) {
        return resource.getPropertyValues(resource.getOWLModel().getRDFTypeProperty());
    }

    public static void addRDFType(RDFResource resource, RDFSClass type) {
        addProtegeType(resource, type);
    }

    public static void removeRDFType(RDFResource resource, RDFSClass type) {
        removeProtegeType(resource, type);
    }

    public static boolean hasRDFType(RDFResource resource, RDFSClass type) {
        return resource.getRDFTypes().contains(type);
    }

    public static boolean hasRDFType(RDFResource resource, RDFSClass type, boolean includingSuperclasses) {
        if (includingSuperclasses) {
            // This is theoretically wrong: It should test the rdf:type values
            return hasProtegeType(resource, type, includingSuperclasses);
        } else {
            return hasRDFType(resource, type);
        }
    }

    public static void sortSubclasses(OWLModel owlModel) {
        for (Iterator it = owlModel.getUserDefinedOWLNamedClasses().iterator(); it.hasNext();) {
            OWLNamedClass namedCls = (OWLNamedClass) it.next();
            OWLUtil.sortSubclasses(namedCls);
        }
        OWLUtil.sortSubclasses(owlModel.getOWLThingClass());
    }

    public static void setRDFType(RDFResource resource, RDFSClass type) {
        setProtegeType(resource, type);
    }

    public static void setRDFTypes(RDFResource resource, Collection types) {
        setProtegeTypes(resource, types);
    }

    public static Collection getHasValuesOnTypes(RDFResource resource, RDFProperty property) {
        Collection results = new HashSet();
        for (Iterator it = resource.getRDFTypes().iterator(); it.hasNext();) {
            RDFSClass type = (RDFSClass) it.next();
            if (type instanceof OWLNamedClass) {
                Collection hasValues = ((OWLNamedClass) type).getHasValues(property);
                results.addAll(hasValues);
            }
        }
        return results;
    }

    public static boolean containsAnonymousClass(Collection clses) {
        for (Iterator it = clses.iterator(); it.hasNext();) {
            Cls cls = (Cls) it.next();
            if (cls instanceof OWLAnonymousClass) {
                return true;
            }
        }
        return false;
    }

    public static List removeInvisibleResources(Iterator it) {
        List results = new ArrayList();
        while (it.hasNext()) {
            RDFResource resource = (RDFResource) it.next();
            if (resource.isVisible()) {
                results.add(resource);
            }
        }
        return results;
    }

    public static boolean isValidPropertyValue(RDFResource resource, RDFProperty property, Object object) {
        return resource.getOWLModel().isValidPropertyValue(resource, property, object);
    }

    public static RDFResource getAllValuesFromOnTypes(RDFResource resource, RDFProperty property) {
        Iterator types = resource.listRDFTypes();
        while (types.hasNext()) {
            RDFSClass type = (RDFSClass) types.next();
            if (type instanceof OWLNamedClass) {
                RDFResource all = ((OWLNamedClass) type).getAllValuesFrom(property);
                if (all != null) {
                    return all;
                }
            }
        }
        return property.getRange();
    }

    public static OWLOntology getActiveOntology(OWLModel owlModel) {
        //TODO: please check implementation
        RDFResource owlOntology = null;
        TripleStoreModel tsm = owlModel.getTripleStoreModel();
        TripleStore activeTripleStore = tsm.getActiveTripleStore();

        return activeTripleStore.getOWLOntology();
    }

    public static void setActiveOntology(OWLModel owlModel, OWLOntology ont) {
        if (ont != null && !ont.equals(OWLUtil.getActiveOntology(owlModel))) {
            TripleStoreModel tsm = owlModel.getTripleStoreModel();
            TripleStore tripleStore = tsm.getHomeTripleStore(ont);
            TripleStoreUtil.switchTripleStore(owlModel, tripleStore);
        }
    }

    public static URI getOWLFileURI(OWLModel owlModel) {
        try {
            Project project = owlModel.getProject();
            String owlURI = JenaKnowledgeBaseFactory.getOWLFilePath(project.getSources());
            if (owlURI.startsWith("http://")) {
                return new URI(owlURI);
            } else {
                URI projectURI = project.getProjectURI();
                if (projectURI == null) {
                    //return new File(owlURI).toURI();
                    return new URI(owlURI);
                } else {
                    URI projectDirURI = project.getProjectDirectoryURI();
                    URI rel = URIUtilities.relativize(projectDirURI, new URI(owlURI));
                    URI result = projectURI.resolve(rel);
                    return result;
                }
            }
        } catch (Exception ex) {
            Log.getLogger().log(Level.SEVERE, "Exception caught", ex);
        }
        return null;
    }

    public static boolean runsWithGUI(OWLModel owlModel) {
        Project project = owlModel.getProject();

        if (project == null) {
            return false;
        }

        JComponent mainPanel = ProjectManager.getProjectManager().getMainPanel();

        //TT: This is a rather week test
        return mainPanel != null;

    }

    /**
     * Renames an ontology. Returns a new ontology object with the new name.
     *
     * @param owlModel
     *            - the OWL model
     * @param oldOntology
     *            - the old OWL Ontology
     * @param newName
     *            - the new name of the ontology
     * @return - a new OWL ontology with the name <code>newName</code>
     */
    public static OWLOntology renameOntology(OWLModel owlModel, OWLOntology oldOntology, String newName) {
        String oldOntoloyName = oldOntology.getName();
        OWLOntology newOntology = (OWLOntology) owlModel.rename(oldOntology, newName);

        synchronizeTripleStoreAfterOntologyRename(owlModel, oldOntoloyName, newOntology);

        return newOntology;
    }

    /**
     * Ensures that the triple store and the renamed ontology have the same
     * name.
     *
     * @param owlModel
     *            - the OWL model
     * @param oldName
     *            - old name of the ontology
     * @param newOntology
     *            - new (renamed) OWL ontology
     */
    public static void synchronizeTripleStoreAfterOntologyRename(OWLModel owlModel, String oldName,
            OWLOntology newOntology) {
        TripleStoreModel tsm = owlModel.getTripleStoreModel();

        //set triple store name to be the same as the new ontology name
        TripleStore ts = tsm.getTripleStore(oldName);
        if (ts == null) { //what to do in this case?
            Log.getLogger().severe("Error at ontology rename. Could not find triplestore " + oldName);
            return;
        }

        String newOntologyName = newOntology.getName();
        ts.setName(newOntologyName);
        ts.setOriginalXMLBase(newOntologyName);

        if (ts.equals(tsm.getTopTripleStore())) {
            //reset the model ontology cache - this will ensure that ontology and triplestore have the same name
            owlModel.resetOntologyCache();
        }
    }

    /*
     * Get the full name of a resource.  We have to handle the short name instead of full URIs
     * used in the create & getter methods. This is a backwards compatibility extension.
     * All the create & get methods will check the expandShortNameInMethods flag, which is default false.
     */
    public static String getInternalFullName(OWLModel owlModel, String name) {
        return getInternalFullName(owlModel, name, owlModel.isExpandShortNameInMethods());
    }

    public static String getInternalFullName(OWLModel owlModel, String name, boolean expandShortNames) {
        if (name == null) {
            return null;
        }
        if (owlModel.isAnonymousResourceName(name)) {
            return name;
        }
        if (expandShortNames && !isAbsoluteURI(owlModel, name)) {
            name = NamespaceUtil.getFullName(owlModel, name);
        }

        return name;
    }

    protected static boolean isAbsoluteURI(OWLModel owlModel, String uriString) {
        if (!uriString.contains(":")) {
            return false;
        }

        URI uri = null;

        try {
            uri = new URI(uriString);
        } catch (URISyntaxException e) {
            //do nothing - uri will be null. Hopefully this won't be the case
        }
        if (uri == null || !uri.isAbsolute()) {
            return false;
        }
        if (uri.isOpaque()) {
            String scheme = uri.getScheme();
            return owlModel.getNamespaceManager().getNamespaceForPrefix(scheme) == null ? true : false;
        }
        return true;
    }
}
