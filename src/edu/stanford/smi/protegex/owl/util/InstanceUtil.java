package edu.stanford.smi.protegex.owl.util;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.Reference;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.owl.model.*;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import static edu.stanford.smi.protegex.owl.model.impl.DefaultRDFSLiteral.DATATYPE_PREFIX;
import static edu.stanford.smi.protegex.owl.model.impl.DefaultRDFSLiteral.LANGUAGE_PREFIX;
import static edu.stanford.smi.protegex.owl.model.impl.DefaultRDFSLiteral.SEPARATOR;

/**
 * A static utility class for some convenience  methods
 *
 * @author Hemed Al Ruwehy
 *         University of Bergen Library
 *         <p>
 *         28-04-2017
 */
public class InstanceUtil {

    private static transient Logger log = Log.getLogger(InstanceUtil.class);
    public static final String PATH_SEPARATOR = "/";

    private InstanceUtil() { }


    /**
     * Remove a given property value from an instance
     *
     * @param instance an instance to modify
     * @param property RDF property
     */
    public static void removePropertyValue(Instance instance, RDFProperty property) {
        if (instance.hasOwnSlot(property) && instance.getDirectOwnSlotValue(property) != null) {
            instance.setDirectOwnSlotValue(property, null);
        }
    }


    /**
     * Gets all references of type OWLIndividual of the given resource
     *
     * @param resource a resource in which its references need to be fetched
     * @return a map which contains resource and property to which this resource is referred to
     */
    @SuppressWarnings("unchecked")
    public static Map<RDFResource, RDFProperty> getInstanceReferences(RDFResource resource){
        Map<RDFResource, RDFProperty> instancesAndProperties = new HashMap();
        Collection<Reference> references = resource.getReferences();
        for(Reference reference : references) {
            if(reference.getFrame() instanceof OWLIndividual){
                RDFResource subject = (RDFResource) reference.getFrame();
                RDFProperty predicate = (RDFProperty) reference.getSlot();
                if(subject != null && predicate != null) {
                    instancesAndProperties.put(subject, predicate);
                }
            }
        }
        return instancesAndProperties;
    }

    /**
     * Check if a given property has inverse relation
     */
    public static boolean isInverseProperty(RDFProperty property) {
        return property.getInverseProperty() != null;
    }


    /**
     * Update a property value from an instance
     *
     * @param instance instance to modify
     * @param property RDF property
     * @param value    new value for the given property
     */
    public static void updatePropertyValue(Instance instance, RDFProperty property, Object value) {
        if (instance.hasOwnSlot(property) && instance.getDirectOwnSlotValue(property) != null) {
            instance.setDirectOwnSlotValue(property, value);
        }
    }

    /**
     * Modify instance properties
     *
     * @param instance      an instance to be modified
     * @param propertiesMap properties map which holds keys as RDF properties and values as RDF property values
     * @since 1.8
     */
    /*public static void modifyProperties(Instance instance, Map<RDFProperty, Optional<?>> propertiesMap) {
        for (Map.Entry<RDFProperty, Optional<?>> entry : propertiesMap.entrySet()) {
            if (entry.getValue().isPresent()) {
                updatePropertyValue(instance, entry.getKey(), entry.getValue().get());
            } else {
                removePropertyValue(instance, entry.getKey());
            }
        }
    }*/

    public static void modifyProperties(Instance instance, Map<RDFProperty, Object> propertiesMap) {
        for (Map.Entry<RDFProperty, Object> entry : propertiesMap.entrySet()) {
            if (entry.getValue() != null) {
                updatePropertyValue(instance, entry.getKey(), entry.getValue());
            } else {
                removePropertyValue(instance, entry.getKey());
            }
        }
    }


    /**
     * Return a default class URI prefix
     */
    private static String getDefaultClassURIPrefix(OWLModel model, String className) {
        String regex = ".+[/#]([^/#]+)$";
        if (!className.matches(regex)) {
            throw new IllegalArgumentException("RDFType does not have anything after a \"#\" or \"/\""
                    + "You might want to change property name [" + className + "] in the ontology");
        }
        String typeName = className.replaceAll(regex, "$1");
        //Lowercase the class label
        if(typeName != null && !typeName.isEmpty()){
            typeName = typeName.toLowerCase();
        }
        return getNamespace(model) + "instance" + PATH_SEPARATOR + typeName + PATH_SEPARATOR;
    }


    /**
     * Return corresponding class URI for a given RDF class type.
     */
    @SuppressWarnings("unchecked")
    public static String getClassURIPrefix(Instance instance) {
        OWLModel owlModel = (OWLModel)instance.getKnowledgeBase();
        Collection<Cls> rdfTypes = instance.getDirectTypes();
        int iterations = 0;
         /*
         if (rdfTypes.size() >= 2)
            log.info("Found classes: " + rdfTypes.toString() + " for instance " +  instance.getName());
         */
        for (Cls clazz : rdfTypes) {
            iterations++;
            String className = clazz.getName();
            if (className.equals(RDFClassType.CONCEPT.getName())) {
                return  getNamespace(owlModel) + "topic" + PATH_SEPARATOR;
            } else if (className.equals(RDFClassType.CONCEPT_SCHEME.getName())) {
                return getNamespace(owlModel) + "conceptscheme" + PATH_SEPARATOR;
            } else if (className.equals(RDFClassType.PROXY_COLLECTION.getName())) {
                return getNamespace(owlModel) + "instance/collection" + PATH_SEPARATOR;
            } else if (className.equals(RDFClassType.EXHIBITION.getName())) {
                return getNamespace(owlModel) + "exhibition" + PATH_SEPARATOR;
            } else {
                if (iterations == rdfTypes.size()) {
                    //If we reach the end of the list, and none of the above class names are present,
                    //then fallback to a default class URI prefix
                    return getDefaultClassURIPrefix(owlModel, className);
                }
            }
        }
        return getNamespace(owlModel);
    }

    /**
     * Get active namespace for the active project
     */
    private static String getNamespace(OWLModel model) {
        return new UUIDInstanceURI(model).getNamespaceForActiveProject();
    }


    /**
     * Encode a given URL string
     *
     * @param url URL to be encoded.
     */
    public static String encodeUrl(String url) {
        String encodedUrl = url.toLowerCase();
        try {
            encodedUrl = URLEncoder.encode(url, "UTF-8");
            return encodedUrl;
        } catch (UnsupportedEncodingException e) {
            log.warning("(UnsupportedEncodingException: " + e.getLocalizedMessage());
        }
        return encodedUrl;
    }


    /**
     * Get Trash class or create new one if it does not exist
     */
    public static OWLNamedClass getTrashClass(OWLModel model) {
        OWLNamedClass trashClass = model.getOWLNamedClass(UBBOntologyNamespaces.TRASH_CLASS_NAME);
        //If it does not exists, create it.
        if (trashClass == null) {
            trashClass = model.createOWLNamedClass(UBBOntologyNamespaces.TRASH_CLASS_NAME);
        }
        return trashClass;
    }

    /**
     * Validates language tag based on xsd:language specification.
     * See http://www.datypic.com/sc/xsd/t-xsd_language.html.
     *
     * @param language a language tag to validate
     *
     * @return true if language tag is valid, otherwise false.
     */
    public static boolean isValidXSDLanguage(String language) {
        String xsdLangRegex = "^[a-zA-Z]{1,8}(-[a-zA-Z0-9]{1,8})*$";
        if (language == null || language.trim().length() == 0) {
            return false;
        }
        if(language.matches(xsdLangRegex)) {
            return true;
        }
        return false;
    }

    /**
     * We know that value of this widget is of data type xsd:anyURI and there is no need for users to see
     * the full URI including datatype in the UI. Therefore, we are stripping out datatype part for easy readability.

     * @param rawValue a string with data type
     *                 example: ~@http://www.w3.org/2001/XMLSchema#anyURI http://data.ub.uib.no/instance/document/ubb-ms-02
     *
     * @return a string where datatype is stripped.
     *                 example: http://data.ub.uib.no/instance/document/ubb-ms-02
     */
    public static String stripDatatype(String rawValue) {
        if(rawValue == null) {
            return "";
        }
        if (rawValue.startsWith(LANGUAGE_PREFIX) || rawValue.startsWith(DATATYPE_PREFIX)) {
            return rawValue.substring(rawValue.indexOf(SEPARATOR) + 1).trim();
        }
        return rawValue;
    }
}



