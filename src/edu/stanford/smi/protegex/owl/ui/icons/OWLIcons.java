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

package edu.stanford.smi.protegex.owl.ui.icons;

import edu.stanford.smi.protege.util.ApplicationProperties;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.owl.model.impl.DefaultOWLAllValuesFrom;

import javax.swing.*;
import java.awt.*;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * A singleton that provides access to the OWL specific icons.
 *
 * @author Holger Knublauch  <holger@knublauch.com>
 */
public class OWLIcons {
    private static final Logger log = Log.getLogger(OWLIcons.class);
    

    public final static String STYLE_DEFAULT = "Default";

    public final static String STYLE_MULTICOLORED = "Multicolored";

    public static final String STYLE_VARIABLE = OWLIcons.class.getName() + ".style";

    public static String style;

    private static int filterAdd = 80;

    private static double filterFactor = 1.3;

    private static Map inheritedMap = new HashMap();

    static final String OWL = "OWL";

    private static Map readOnlyMap = new HashMap();

    static Map theOWLIcons = new HashMap();


    static {
        setStyle(ApplicationProperties.getString(STYLE_VARIABLE, STYLE_DEFAULT));
    }


    public static final String ACCEPT = "Accept";

    public static final String ADD_OVERLAY = "AddOverlay";

    public static final String ADD_PROPERTY_TO_CLASS_FORM = "AddPropertyToClassForm";

    public static final String ANNOTATION_PROPERTY = "OWLAnnotationProperty";

    public static final String ANNOTATION = "Annotation";

    public static final String ANNOTATIONS = "Annotations";

    public static final String ANNOTATIONS_TABLE = "AnnotationsTable";

    public static final String ANONYMOUS_OWL_CLASS = "OWLAnonymousClass";

    public static final String ASSERT_FALSE = "AssertFalse";

    public static final String ASSERT_TRUE = "AssertTrue";

    public static final String CHECK_CONSISTENCY = "CheckConsistency";

    public static final String CLASS_FRAME = "ClassFrame";

    public static final String CLASS_MATRIX = "ClassMatrix";

    public static final String CLASSIFY = "Classify";

    public static final String CLASSIFY_INDIVIDUAL = "ClassifyIndividual";

    public static final String CLOSURE_CLOSED = "ClosureClosed";

    public static final String CLOSURE_OPEN = "ClosureOpen";

    public static final String CREATE_OVERLAY = "CreateOverlay";

    public static final String CREATE_IMPORTED_TRIPLESTORE = "CreateImportedTripleStore";

    public static final String DATATYPE_TRIPLE = "DatatypeTriple";

    public static final String DEFINED_OWL_CLASS = "DefinedOWLClass";

    public static final String DELETE = "Delete";

    public static final String TRASH_CLASS_ICON = "OWLTrashClass";
    public static final String TRASH_INDIVIDUAL_ICON = "OWLTrashIndividual";
    public static final String MERGE_INDIVIDUAL_ICON = "OWLMergeIndividual";

    public static final String DELETE_OVERLAY = "DeleteOverlay";

    public static final String ERROR = "Error";

    public static final String FILE = "File";

    public static final String FIND_ASSERTS = "FindAsserts";

    public static final String GET_INFERRED_SUBCLASSES = "GetInferredSubclasses";

    public static final String GET_INFERRED_SUPERCLASSES = "GetInferredSuperclasses";

    public static final String HELP_LOGO = "OWLHelp.png";

    public static final String IMPORT = "Import";

    public static final String LAYOUT_HORIZONTALLY = "LayoutHorizontally";

    public static final String LAYOUT_VERTICALLY = "LayoutVertically";

    public static final String OWL_ALL_VALUES_FROM = "OWLAllValuesFrom";

    public static final String OWL_ANNOTATION_PROPERTY_FRAME = "OWLAnnotationPropertyFrame";

    public static final String OWL_CARDINALITY = "OWLCardinality";

    public static final String OWL_COMPLEMENT_CLASS = "OWLComplementClass";

    public static final String OWL_DATATYPE_ANNOTATION_PROPERTY = "AnnotationOWLDatatypeProperty";

    public static final String OWL_DATATYPE_PROPERTY = "OWLDatatypeProperty";

    public static final String OWL_DATATYPE_PROPERTY_INHERITED = OWL_DATATYPE_PROPERTY + "Inherited";

    public static final String OWL_DISJOINT_CLASSES = "OWLDisjointClasses";

    public static final String OWL_ENUMERATED_CLASS = "OWLEnumeratedClass";

    public static final String OWL_EQUIVALENT_CLASS = "OWLEquivalentClass";

    public static final String OWL_HAS_VALUE = "OWLHasValue";

    public static final String OWL_INTERSECTION_CLASS = "OWLIntersectionClass";

    public static final String OWL_MAX_CARDINALITY = "OWLMaxCardinality";

    public static final String OWL_MIN_CARDINALITY = "OWLMinCardinality";

    public static final String OWL_OBJECT_ANNOTATION_PROPERTY = "AnnotationOWLObjectProperty";

    public static final String OWL_OBJECT_PROPERTY = "OWLObjectProperty";

    public static final String OWL_OBJECT_PROPERTY_INHERITED = OWL_OBJECT_PROPERTY + "Inherited";

    public static final String OWL_RESTRICTION = "OWLRestriction";

    public static final String OWL_SOME_VALUES_FROM = "OWLSomeValuesFrom";

    public static final String OWL_UNION_CLASS = "OWLUnionClass";

    public static final String PREFIX = "Prefix";

    public static final String PRIMITIVE_OWL_CLASS = "PrimitiveClass";

    public static final String PROPERTY_FRAME = "PropertyFrame";

    public static final String PROPERTY_MATRIX = "PropertyMatrix";

    public static final String RDF_DATATYPE = "RDFSDatatype";

    public static final String RDF_INDIVIDUAL = "RDFIndividual";
    
    public static final String RDF_ANON_INDIVIDUAL = "RDFAnonIndividual";    

    public static final String RDF_INDIVIDUAL_FRAME = "RDFIndividualFrame";

    public static final String RDF_INDIVIDUALS = "RDFIndividuals";

    public static final String RDF_PROPERTY = "RDFProperty";

    public static final String RDF_PROPERTY_INHERITED = RDF_PROPERTY + "Inherited";

    public static final String RDFS_METACLASS = "RDFSMetaclass";

    public static final String RDFS_NAMED_CLASS = "RDFSNamedClass";

    public static final String RDFS_RANGE = "RDFSRange";

    public static final String RDFS_SUBCLASS_OF = "RDFSSubclassOf";

    public static final String REFRESH = "Refresh";

    public static final String REMOVE_OVERLAY = "RemoveOverlay";

    public static final String SAVE_INFERRED = "SaveInferred";

    public static final String SELECT_ACTIVE_TRIPLESTORE = "SelectActiveTripleStore";

    public static final String SELECT_FILE = "SelectFile";

    public static final String SHOW_EXPLORER = "ShowExplorer";

    public static final String SIBLING_CLASS = "SiblingClass";

    public static final String SOURCE_CODE = "SourceCode";

    public static final String SPARQL = "SPARQL";

    public static final String SPARQL_RESULTS_PANEL = "SPARQLResultsPanel";

    public static final String SUB_CLASS = "SubClass";

    public static final String SUPERCLASS_EXPLORER = "SuperclassExplorer";

    public static final String TEST = "Test";

    public static final String TEST_SETTINGS = "TestSettings";

    public static final String TODO = "Todo";

    public static final String TOP = "Top";

    public static final String TRIPLE = "Triple";

    public static final String TRIPLES = "Triples";

    public static final String VIEW = "View";

    public static final String VIEW_OVERLAY = "ViewOverlay";

    public static final String XSD_DATATYPE = "XSDDatatype";

    public static final String XSD_MAX_EXCLUSIVE = "XSDMaxExclusive";

    public static final String XSD_MAX_INCLUSIVE = "XSDMaxInclusive";

    public static final String XSD_MIN_EXCLUSIVE = "XSDMinExclusive";

    public static final String XSD_MIN_INCLUSIVE = "XSDMinInclusive";


    public static Icon getAddIcon() {
        return getImageIcon("Add");
    }


    public static Icon getAddIcon(String baseIconName) {
        return getAddIcon(baseIconName, OWLIcons.class);
    }


    public static Icon getAddIcon(String baseIconName, Class baseClass) { // x was: 2
        return new OverlayIcon(baseIconName, 5, 4, ADD_OVERLAY, 12, 12, baseClass);
    }


    public static Icon getAllRestrictionIcon() {
        return getImageIcon(DefaultOWLAllValuesFrom.ICON_NAME);
    }


    public static ImageIcon getArchiveProjectIcon() {
        return getImageIcon("ArchiveProject");
    }


    public static Icon getAssertChangeIcon() {
        return getImageIcon("AssertChange");
    }


    public static Icon getBackspaceIcon() {
        return getImageIcon("Backspace");
    }


    public static Icon getCheckConsistencyIcon() {
        return getImageIcon(CHECK_CONSISTENCY);
    }


    public static Icon getClassifyIcon() {
        return getImageIcon(CLASSIFY);
    }


    public static Icon getClassifyIndividualIcon() {
        return getImageIcon(CLASSIFY_INDIVIDUAL);
    }


    public static Icon getCloseIcon() {
        return getImageIcon("Close");
    }


    public static Icon getClassesIcon() {
        return getImageIcon("Classes");
    }


    public static Icon getComputeSingleInferredTypesIcon() {
        return getImageIcon("ComputeSingleInferredTypes");
    }


    public static Icon getComputeIndividualsBelongingToClassIcon() {
        return getImageIcon("ComputeIndividualsBelongingToClass");
    }


    public static Icon getCopyIcon() {
        return getImageIcon("Copy");
    }


    public static Icon getCreateIcon(String baseIconName) {
        return getCreateIcon(baseIconName, OWLIcons.class);
    }


    public static Icon getCreateIcon(String baseIconName, int topY) {
        return getCreateIcon(baseIconName, topY, OWLIcons.class);
    }


    public static Icon getCreateIcon(String baseIconName, Class clazz) {
        return new OverlayIcon(baseIconName, 5, 4, CREATE_OVERLAY, 13, 1, clazz);
    }


    public static Icon getCreateIcon(String baseIconName, int topY, Class clazz) {
        return new OverlayIcon(baseIconName, 5, 4, CREATE_OVERLAY, 13, topY, clazz);
    }


    public static Icon getCreateIndividualIcon(String baseIconName) {
        if (isDefaultStyle()) {
            return new OverlayIcon(baseIconName, 5, 4, CREATE_OVERLAY, 11, 3, OWLIcons.class);
        }
        else {
            return new OverlayIcon(baseIconName, 5, 4, CREATE_OVERLAY, 12, 2, OWLIcons.class);
        }
    }


    public static Icon getCreatePropertyIcon(String baseIconName) {
        return getCreateIcon(baseIconName, isDefaultStyle() ? 2 : 1);
    }


    public static Icon getCutIcon() {
        return getImageIcon("Cut");
    }


    /**
     * @deprecated please access constant directly
     */
    public static Icon getDefinedClsIcon() {
        return getImageIcon(DEFINED_OWL_CLASS);
    }


    public static Icon getDeleteIcon() {
        return getImageIcon(DELETE);
    }

    public static Icon getTrashClassIcon() {
        return getImageIcon(TRASH_CLASS_ICON);
    }

    public static Icon getTrashIndividualIcon() {
        return getImageIcon(TRASH_INDIVIDUAL_ICON);
    }

    public static Icon getMergeIndividualIcon() {
        return getImageIcon(MERGE_INDIVIDUAL_ICON);
    }


    public static Icon getDeleteClsIcon() {
        return getDeleteIcon(PRIMITIVE_OWL_CLASS);
    }


    public static Icon getDeleteIcon(String iconName) {
        return getDeleteIcon(iconName, OWLIcons.class);
    }


    public static Icon getDeleteIcon(String iconName, Class clazz) {
        return new OverlayIcon(iconName, 5, 5, DELETE_OVERLAY, 4, 4, clazz);
    }


    public static Icon getDeprecatedIcon() {
        return getImageIcon("Deprecated");
    }


    public static Icon getDisplayChangedClassesIcon() {
        return getImageIcon("DisplayChangedClasses");
    }


    public static ImageIcon getDownIcon() {
        return getImageIcon("Down");
    }


    public static Icon getEditHTMLIcon() {
        return getImageIcon("EditHTML");
    }


    public static Icon getEquivalentClassIcon() {
        return getImageIcon("OWLEquivalentClass");
    }


    public static ImageIcon getExternalResourceIcon() {
        return getImageIcon("ExternalResource");
    }


    private static Icon getFilteredIcon(ImageFilter filter, ImageIcon icon, String frameIconName) {
        Image image = icon.getImage();
        ImageProducer prod = new FilteredImageSource(image.getSource(), filter);
        Image readOnlyImage = Toolkit.getDefaultToolkit().createImage(prod);
        final Image frameImage = getImageIcon(frameIconName).getImage();
        return new OverlayIcon(readOnlyImage, 0, 0, frameImage, 0, 0,
                icon.getIconWidth(), icon.getIconHeight());
        //frameImage.getWidth(null), frameImage.getHeight(null));
    }


    public static Icon getFindUsageIcon() {
        return getImageIcon("FindUsage");
    }


    public static ImageIcon getImageIcon(String name) {
        return getImageIcon(name, OWLIcons.class);
    }


    public static ImageIcon getImageIcon(String name, Class clazz) {
        ImageIcon icon = (ImageIcon) theOWLIcons.get(name);
        if (icon == null || icon.getIconWidth() == -1) {
            String partialName = name;
            if (name.lastIndexOf('.') < 0) {
                partialName += ".gif";
            }
            if (icon == null) {
                icon = loadImageIcon(clazz, partialName);
            }
            if (icon == null && !name.equals("Ugly")) {
                icon = getImageIcon("Ugly");
            }
            theOWLIcons.put(name, icon);
        }
        return icon;
    }


    public static Icon getInheritedClsIcon(ImageIcon icon) {
        return getInheritedIcon(icon, CLASS_FRAME);
    }


    public static Icon getInheritedIcon(ImageIcon icon, String frameIconName) {
        Icon i = (Icon) inheritedMap.get(icon);
        if (i == null) {
            ImageFilter filter = isMulticoloredStyle() ?
                    (ImageFilter) new InheritedColorfulFilter() :
                    (ImageFilter) new DefaultReadOnlyFilter(1.0, 26, 46, 94);
            i = getFilteredIcon(filter, icon, frameIconName);
            inheritedMap.put(icon, i);
        }
        return i;
    }


    public static Icon getInheritedPropertyIcon(ImageIcon icon) {
        return getInheritedIcon(icon, PROPERTY_FRAME);
    }


    public static URL getImageURL(Class clazz, String baseIconName) {
        if (baseIconName.lastIndexOf(".") < 0) {
            baseIconName += ".gif";
        }
        String[] tries = {
                style + "/" + baseIconName,
                STYLE_MULTICOLORED + "/" + baseIconName,
                baseIconName
        };
        for (int i = 0; i < tries.length; i++) {
            String name = tries[i];
            URL url = clazz.getResource(name);
            if (url != null) {
                return url;
            }
        }
        log.warning("[OWLIcons] Error: Could not find icon " + STYLE_MULTICOLORED + baseIconName);
        return null;
    }


    public static ImageIcon getMetaclassIcon() {
        return getImageIcon("RDFSMetaclass");
    }


    public static Icon getNavigateBackIcon() {
        return getImageIcon("NavigateBack");
    }


    public static Icon getNavigateForwardIcon() {
        return getImageIcon("NavigateForward");
    }


    public static Icon getNerdErrorIcon() {
        return getImageIcon(ERROR); //"NerdError");
    }


    public static Icon getNerdSmilingIcon() {
        return getImageIcon(ACCEPT); //"NerdSmiling");
    }


    public static Icon getOpenProjectIcon() {
        return getImageIcon("OpenProject");
    }


    public static Icon getOWLIcon() {
        return getImageIcon(OWL);
    }


    public static Icon getOWLTestErrorIcon() {
        return getImageIcon("TestError");
    }


    public static Icon getOWLFullIcon() {
        return getImageIcon("OWLFull");
    }


    public static Icon getOWLTestWarningIcon() {
        return getImageIcon("TestWarning");
    }


    public static Icon getPasteIcon() {
        return getImageIcon("Paste");
    }


    public static Icon getPreferencesIcon() {
        return getImageIcon("Preferences");
    }


    public static Icon getPrimitiveClsIcon() {
        return getImageIcon(PRIMITIVE_OWL_CLASS);
    }


    public static Icon getPropertiesIcon() {
        return getImageIcon("Properties");
    }


    public static Icon getReadOnlyAnnotationPropertyIcon(ImageIcon icon) {
        return getReadOnlyIcon(icon, OWL_ANNOTATION_PROPERTY_FRAME);
    }


    public static Icon getReadOnlyIcon(ImageIcon icon, String frameIconName) {
        Icon i = (Icon) readOnlyMap.get(icon);
        if (i == null) {
            ImageFilter filter = new MulticoloredReadOnlyFilter();
            i = getFilteredIcon(filter, icon, frameIconName);
            readOnlyMap.put(icon, i);
        }
        return i;
    }


    public static Icon getDefaultReadOnlyIcon(ImageIcon icon, String frameIconName, int ar, int ag, int ab) {
        Icon i = (Icon) readOnlyMap.get(icon);
        if (i == null) {
            ImageFilter filter = new DefaultReadOnlyFilter(1.0, 24, 42, 90);
            i = getFilteredIcon(filter, icon, frameIconName);
            readOnlyMap.put(icon, i);
        }
        return i;
    }

    
    public static Icon getReadOnlyClsIcon(String iconName) {
    	return getReadOnlyIcon(getImageIcon(iconName), iconName);
    }

    public static Icon getReadOnlyClsIcon(ImageIcon icon) {
        return isDefaultStyle() ? getDefaultReadOnlyIcon(icon, CLASS_FRAME, 30, 30, 80) :
                getReadOnlyIcon(icon, CLASS_FRAME);
    }


    public static Icon getReadOnlyIndividualIcon(ImageIcon icon) {
        return getReadOnlyIcon(icon, RDF_INDIVIDUAL_FRAME);
    }


    public static Icon getReadOnlyPropertyIcon(ImageIcon icon) {
        return getReadOnlyIcon(icon, "PropertyFrame");
    }


    public static Icon getRedoIcon() {
        return getImageIcon("Redo");
    }


    public static Icon getRemoveIcon() {
        return getImageIcon("Remove");
    }


    public static Icon getRemoveIcon(String baseIconName) {
        return new OverlayIcon(baseIconName, 5, 4, REMOVE_OVERLAY, 14, 13);
    }


    public static ImageIcon getRevertProjectIcon() {
        return getImageIcon("RevertProject");
    }


    public static Icon getSaveInferredIcon() {
        return getImageIcon("SaveInferred");
    }


    public static Icon getSaveProjectIcon() {
        return getImageIcon("SaveProject");
    }


    public static Icon getTestIcon() {
        return getImageIcon(TEST);
    }


    public static Icon getTestSettingsIcon() {
        return getImageIcon(TEST_SETTINGS);
    }


    public static Icon getTODOIcon() {
        return getImageIcon(TODO);
    }


    public static ImageIcon getUpIcon() {
        return getImageIcon("Up");
    }


    public static Icon getUndoIcon() {
        return getImageIcon("Undo");
    }


    public static Icon getViewIcon() {
        return OWLIcons.getImageIcon(VIEW);
    }


    public static Icon getViewIcon(String iconName) {
        return getViewIcon(iconName, OWLIcons.class);
    }


    public static Icon getViewIcon(String iconName, Class clazz) {
        return new OverlayIcon(iconName, 4, 4, VIEW_OVERLAY, 11, 11, clazz);
    }


    public static Icon getSuperclassesIcon() {
        return getImageIcon(RDFS_SUBCLASS_OF);
    }


    public static boolean isMulticoloredStyle() {
        return STYLE_MULTICOLORED.equals(style);
    }


    public static boolean isDefaultStyle() {
        return STYLE_DEFAULT.equals(style);
    }


    static ImageIcon loadImageIcon(Class cls, String name) {
        URL url = getImageURL(cls, name);
        if (url != null) {
            ImageIcon icon = new ImageIcon(url);
            if (icon.getIconWidth() == -1) {
                Log.getLogger().severe("[OWLIcons] Failed to load " + name);
            }
            return icon;
        }
        return null;
    }


    public static void setStyle(String newStyle) {
        style = newStyle;
        theOWLIcons.clear();
        inheritedMap.clear();
        readOnlyMap.clear();
        filterFactor = isDefaultStyle() ? 1.0 : 1.3;
        filterAdd = isDefaultStyle() ? 50 : 80;
    }


    /**
     * Removes all pixels unless they are black.
     */
    public static class InheritedColorfulFilter extends RGBImageFilter {

        public int filterRGB(int x, int y, int rgb) {
            if ((rgb & 0xffffff) == 0) {
                return rgb;
            }
            else {
                return (rgb & 0xff000000) | 0xffffff;
            }
        }
    }


    public static class MulticoloredReadOnlyFilter extends RGBImageFilter {

        public int filterRGB(int x, int y, int rgb) {

            int r = (rgb & 0xff0000) >> 16;
            int g = (rgb & 0xff00) >> 8;
            int b = (rgb & 0xff);

            r = (int) (r * filterFactor);
            if (r > 0xff) r = 0xff;
            g = (int) (g * filterFactor);
            if (g > 0xff) g = 0xff;
            b = (int) (b * filterFactor);
            if (b > 0xff) b = 0xff;

            final int a = filterAdd;
            r = Math.min(0xff, r + a);
            g = Math.min(0xff, g + a);
            b = Math.min(0xff, b + a);

            return (rgb & 0xff000000) | (r << 16) | (g << 8) | b;
        }
    }


    public static class DefaultReadOnlyFilter extends RGBImageFilter {

        private int ar;

        private int ag;

        private int ab;

        private double factor;


        public DefaultReadOnlyFilter(double factor, int ar, int ag, int ab) {
            this.factor = factor;
            this.ar = ar;
            this.ag = ag;
            this.ab = ab;
        }


        public int filterRGB(int x, int y, int rgb) {

            int r = (rgb & 0xff0000) >> 16;
            int g = (rgb & 0xff00) >> 8;
            int b = (rgb & 0xff);

            r = (int) (r * factor);
            if (r > 0xff) r = 0xff;
            g = (int) (g * factor);
            if (g > 0xff) g = 0xff;
            b = (int) (b * factor);
            if (b > 0xff) b = 0xff;

            r = Math.min(0xff, r + ar);
            g = Math.min(0xff, g + ag);
            b = Math.min(0xff, b + ab);

            return (rgb & 0xff000000) | (r << 16) | (g << 8) | b;
        }
    }
}
