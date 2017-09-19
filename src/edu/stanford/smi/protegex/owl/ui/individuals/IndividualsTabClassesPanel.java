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

package edu.stanford.smi.protegex.owl.ui.individuals;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.resource.Icons;
import edu.stanford.smi.protege.resource.LocalizedText;
import edu.stanford.smi.protege.resource.ResourceKey;
import edu.stanford.smi.protege.ui.FrameRenderer;
import edu.stanford.smi.protege.ui.HeaderComponent;
import edu.stanford.smi.protege.ui.ParentChildRoot;
import edu.stanford.smi.protege.util.*;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.model.RDFSClass;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;
import edu.stanford.smi.protegex.owl.ui.actions.ResourceActionManager;
import edu.stanford.smi.protegex.owl.ui.cls.ClassTree;
import edu.stanford.smi.protegex.owl.ui.cls.ClassTreePanel;
import edu.stanford.smi.protegex.owl.ui.widget.OWLUI;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;

/**
 * The class tree display on the individuals tab. This contains header label (top),
 * scrollable class hierarchy (center) and finder component in the footer (now disabled).
 *
 * @author Holger Knublauch  <holger@knublauch.com>
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class IndividualsTabClassesPanel extends SelectableContainer implements ClassTreePanel {
    private static final Color classBrowserColor = new Color(36, 182, 245);

    private ClassTree classTree;

    private OWLModel owlModel;


    public IndividualsTabClassesPanel(OWLModel owlModel) {
        this.owlModel = owlModel;
        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createClsesPanel(), BorderLayout.CENTER);
        setSelectable(classTree);
    }


    private JComponent createHeaderPanel() {
        JLabel label = ComponentFactory.createLabel(owlModel.getProject().getName());
        label.setIcon(Icons.getProjectIcon());
        String classBrowserLabel = LocalizedText.getText(ResourceKey.CLASS_BROWSER_TITLE);
        String forProjectLabel = LocalizedText.getText(ResourceKey.CLASS_BROWSER_FOR_PROJECT_LABEL);
        HeaderComponent header = new HeaderComponent(classBrowserLabel, forProjectLabel, label);
        //header.setColor(Colors.getClsColor());
        header.setColor(classBrowserColor); //use new color that matches with new icons
        return header;
    }


    private JComponent createClsesPanel() {
        RDFSNamedClass root = owlModel.getOWLThingClass();
        classTree = new ClassTree(null, new ParentChildRoot(root));
        classTree.setLargeModel(true);       
        classTree.setSelectionRow(0);       
        
        classTree.setCellRenderer(new InferredInstancesCountRenderer());
        classTree.addMouseListener(new PopupMenuMouseListener(classTree) {
            protected JPopupMenu getPopupMenu() {
                Collection sel = classTree.getSelection();
                if (sel.size() == 1) {
                    JPopupMenu menu = new JPopupMenu();
                    Cls cls = (Cls) sel.iterator().next();
                    if (cls instanceof RDFResource) {
                        ResourceActionManager.addResourceActions(menu, IndividualsTabClassesPanel.this, (RDFResource) cls);
                        if (menu.getComponentCount() > 0) {
                            return menu;
                        }
                    }
                }
                return null;
            }


            protected void setSelection(JComponent c, int x, int y) {
                int row = classTree.getRowForLocation(x, y);
                if (row >= 0) {
                    classTree.setSelectionRow(row);
                }
            }
        });
        
        String classHiearchyLabel = LocalizedText.getText(ResourceKey.CLASS_BROWSER_HIERARCHY_LABEL);
        LabeledComponent c = new LabeledComponent(classHiearchyLabel, ComponentFactory.createScrollPane(classTree));
        c.setBorder(ComponentUtilities.getAlignBorder());
        c.addHeaderButton(getViewClsAction());

         /*
          *  Remove Finder Component from the class tree component (CLASS BROWSER) following the request from UiB Library.
          *  In server-client architecture, finding a resource deep into the hierarchy is resource-intensive and
          *  causes Protege to freeze. See issue #1 at https://prosjekt.uib.no/issues/7757.
          *
          * - Hemed Al Ruwehy, 27-07-2017
         **/

        /**
        FindAction fAction = new FindInDialogAction(new DefaultClassFind(owlModel, Find.CONTAINS),
                                       Icons.getFindClsIcon(), classTree, true);
        ResourceFinder finder = new ResourceFinder(fAction);
        FindAction findIndAction = new FindInDialogAction(new DefaultIndividualFind(owlModel, Find.CONTAINS),
                                       Icons.getFindIcon(), null, true);
        findIndAction.setTextBox(finder.getTextComponent());
        finder.addButton(findIndAction);
        c.setFooterComponent(finder);
        **/

        return c;
    }


    public JTree getDropComponent() {
        return classTree;
    }


    private Action getViewClsAction() {
        return new ViewAction(ResourceKey.CLASS_VIEW, this) {
            public void onView(Object o) {
                Cls cls = (Cls) o;
                owlModel.getProject().show(cls);
            }
        };
    }


    /**
     * @deprecated
     */
    public void setSelectedCls(Cls cls) {
        if (cls instanceof RDFSNamedClass) {
            setSelectedClass((RDFSNamedClass) cls);
        }
    }


    public void setSelectedClass(RDFSNamedClass cls) {
    	OWLUI.setSelectedNodeInTree(classTree, cls);
    }


	public void setSelectedClass(RDFSClass cls) {
		OWLUI.setSelectedNodeInTree(classTree, cls);		
	}


	public JTree getTree() {		
		return classTree;
	}
	
    private class InferredInstancesCountRenderer extends FrameRenderer {

        InferredInstancesCountRenderer() {
            setDisplayDirectInstanceCount(true);
        }
        
        @Override
        public void setMainText(String text) {        
        	super.setMainText(StringUtilities.unquote(text));
        }


        protected String getInstanceCountString(Cls cls) {
            if (cls instanceof RDFSNamedClass) {
                RDFSNamedClass c = (RDFSNamedClass) cls;
                int inferredInstanceCount = c.getInferredInstanceCount();
                if (inferredInstanceCount > 0) {
                    return "  (" + cls.getDirectInstanceCount() + " / " + inferredInstanceCount + ")";
                }
            }
            return super.getInstanceCountString(cls);
        }
    }
}
