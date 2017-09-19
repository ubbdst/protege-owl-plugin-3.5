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

package edu.stanford.smi.protegex.owlx.examples;

import edu.stanford.smi.protege.exception.OntologyLoadException;
import edu.stanford.smi.protegex.owl.ProtegeOWL;
import edu.stanford.smi.protegex.owl.model.OWLIndividual;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.model.RDFSClass;
import junit.framework.Assert;

import java.util.Collection;
import java.util.Iterator;

/**
 * @author Holger Knublauch  <holger@knublauch.com>
 */
public class CreateNamedClasses {

    public static void main(String[] args) throws OntologyLoadException {

        OWLModel owlModel = ProtegeOWL.createJenaOWLModel();

        OWLNamedClass personClass = owlModel.createOWLNamedClass("Person");

        // Create subclass (complicating version)
        OWLNamedClass brotherClass = owlModel.createOWLNamedClass("Brother");
        brotherClass.addSuperclass(personClass);
        brotherClass.removeSuperclass(owlModel.getOWLThingClass());

        // Create subclass (direct version)
        OWLNamedClass sisterClass = owlModel.createOWLNamedSubclass("Sister", personClass);

        printClassTree(personClass, "");

        OWLIndividual hans = brotherClass.createOWLIndividual("Hans");
        Collection brothers = brotherClass.getInstances(false);
        Assert.assertTrue(brothers.contains(hans));
        Assert.assertTrue(brothers.size() == 1);

        Assert.assertEquals(personClass.getInstanceCount(false), 0);
        Assert.assertEquals(personClass.getInstanceCount(true), 0);
        Assert.assertTrue(personClass.getInstances(true).contains(hans));

        Assert.assertTrue(hans.getProtegeType().equals(brotherClass));
        Assert.assertTrue(hans.hasProtegeType(brotherClass));
        Assert.assertFalse(hans.hasProtegeType(personClass, false));
        Assert.assertTrue(hans.hasProtegeType(personClass, true));
    }


    private static void printClassTree(RDFSClass cls, String indentation) {
        System.out.println(indentation + cls.getName());
        for (Iterator it = cls.getSubclasses(false).iterator(); it.hasNext(); ) {
            RDFSClass subclass = (RDFSClass) it.next();
            printClassTree(subclass, indentation + "    ");
        }
    }
}
