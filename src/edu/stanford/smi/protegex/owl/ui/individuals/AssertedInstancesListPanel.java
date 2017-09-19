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

import edu.stanford.smi.protege.action.MakeCopiesAction;
import edu.stanford.smi.protege.action.ReferencersAction;
import edu.stanford.smi.protege.event.*;
import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.resource.Colors;
import edu.stanford.smi.protege.resource.LocalizedText;
import edu.stanford.smi.protege.resource.ResourceKey;
import edu.stanford.smi.protege.server.framestore.RemoteClientFrameStore;
import edu.stanford.smi.protege.ui.BrowserTextListFinder;
import edu.stanford.smi.protege.ui.ConfigureAction;
import edu.stanford.smi.protege.ui.FrameRenderer;
import edu.stanford.smi.protege.ui.HeaderComponent;
import edu.stanford.smi.protege.util.*;
import edu.stanford.smi.protegex.owl.model.*;
import edu.stanford.smi.protegex.owl.ui.OWLLabeledComponent;
import edu.stanford.smi.protegex.owl.ui.ProtegeUI;
import edu.stanford.smi.protegex.owl.ui.actions.DeleteInstanceOrMoveToTrashAction;
import edu.stanford.smi.protegex.owl.ui.icons.OWLIcons;
import edu.stanford.smi.protegex.owl.ui.widget.OWLUI;
import edu.stanford.smi.protegex.owl.ui.widget.UUIDWidget;
import edu.stanford.smi.protegex.owl.util.InstanceUtil;
import edu.stanford.smi.protegex.owl.util.UUIDInstanceURI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;
import java.util.logging.Logger;

/**
 * The panel that holds the list of direct instances of one or more classes. If
 * only one class is chosen then you can also create new instances of this
 * class.
 *
 * @author Holger Knublauch  <holger@knublauch.com>
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class AssertedInstancesListPanel extends SelectableContainer implements Disposable {

    private static final int SORT_LIMIT;
    private static transient Logger log = Log.getLogger(AssertedInstancesListPanel.class);

    static {
        //Not applicable anymore - instances are always sorted when displayed.
        //If in client-server mode, the instances are sorted on the server
        //and they are retrieved by a client using a Protege Job
        SORT_LIMIT = ApplicationProperties.getIntegerProperty("ui.DirectInstancesList.sort_limit", 1000);
    }

    private OWLModel owlModel;
    private Collection<Cls> classes = Collections.emptyList();
    private AllowableAction createAction;
    private AllowableAction createAnonymousAction;
    private AllowableAction copyAction;
    private AllowableAction deleteAction;
    private HeaderComponent header;
    private OWLLabeledComponent lc;
    private InstancesList list;
    private Collection<Instance> listenedToInstances = new ArrayList<Instance>();
    private boolean showSubclassInstances;
    private FrameListener _clsFrameListener = new FrameAdapter() {
        @Override
        public void ownSlotValueChanged(FrameEvent event) {
            super.ownSlotValueChanged(event);
            updateButtons();
        }
    };
    private FrameListener _instanceFrameListener = new FrameAdapter() {
        @Override
        public void browserTextChanged(FrameEvent event) {
            Frame frame = event.getFrame();
            ComponentUtilities.replaceListValue(list,
                    new FrameWithBrowserText(frame),
                    new FrameWithBrowserText(frame, frame.getBrowserText(), ((Instance) frame).getDirectTypes()));
            repaint();
        }

        @Override
        public void frameReplaced(FrameEvent event) {
            Instance oldInst = (Instance) event.getFrame();
            Instance newInst = (Instance) event.getNewFrame();

            ComponentUtilities.replaceListValue(list,
                    new FrameWithBrowserText(oldInst, oldInst.getBrowserText(), oldInst.getDirectTypes()),
                    new FrameWithBrowserText(newInst, newInst.getBrowserText(), newInst.getDirectTypes()));
        }
    };
    private ClsListener _clsListener = new ClsAdapter() {
        @Override
        public void directInstanceAdded(ClsEvent event) {
            if (event.isReplacementEvent()) {
                return;
            }
            Instance instance = event.getInstance();
            FrameWithBrowserText fbt = new FrameWithBrowserText(instance, instance.getBrowserText(), instance.getDirectTypes());
            if (!getModel().contains(fbt)) {
                ComponentUtilities.addListValue(list, fbt);
                instance.addFrameListener(_instanceFrameListener);
            }
        }

        @Override
        public void directInstanceRemoved(ClsEvent event) {
            if (event.isReplacementEvent()) {
                return;
            }
            removeInstanceListener(event.getInstance());
            ComponentUtilities.removeListValue(list, new FrameWithBrowserText(event.getInstance()));
        }


    };


    public AssertedInstancesListPanel(OWLModel owlModel) {
        this.owlModel = owlModel;

        Action viewAction = createViewAction();
        list = new InstancesList(viewAction);
        createListRenderer();
        lc = new OWLLabeledComponent(null, ComponentFactory.createScrollPane(list));
        addButtons(viewAction, lc);
        lc.setFooterComponent(new BrowserTextListFinder(list, ResourceKey.INSTANCE_SEARCH_FOR));

        lc.setBorder(ComponentUtilities.getAlignBorder());
        add(lc, BorderLayout.CENTER);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(createHeader(), BorderLayout.NORTH);
        add(panel, BorderLayout.NORTH);

        setSelectable(list);
        // initializeShowSubclassInstances();
        lc.setHeaderLabel("Asserted Instances");
    }

    private static Collection removeHiddenInstances(Collection instances) {
        Collection visibleInstances = new ArrayList(instances);
        Iterator i = visibleInstances.iterator();
        while (i.hasNext()) {
            Instance instance = (Instance) i.next();
            if (!instance.isVisible()) {
                i.remove();
            }
        }
        return visibleInstances;
    }

    private void updateLabel() {
        String text;
        Cls cls = getSoleAllowedCls();
        BrowserSlotPattern pattern = cls == null ? null : cls.getBrowserSlotPattern();
        if (pattern == null) {
            text = null;
        } else {
            // text = "Instances by ";
            if (pattern.isSimple()) {
                text = pattern.getFirstSlot().getBrowserText();
                if (Model.Slot.NAME.equals(text)) {
                    text = "Asserted Instances";
                }
            } else {
                text = "multiple properties";
            }
        }
        lc.setHeaderLabel(text);
    }

    private HeaderComponent createHeader() {
        JLabel label = ComponentFactory.createLabel();
        String instanceBrowserLabel = LocalizedText.getText(ResourceKey.INSTANCE_BROWSER_TITLE);
        String forClassLabel = LocalizedText.getText(ResourceKey.CLASS_EDITOR_FOR_CLASS_LABEL);
        header = new HeaderComponent(instanceBrowserLabel, forClassLabel, label);
        header.setColor(Colors.getInstanceColor());
        return header;
    }

    //TODO: fix me
    private void fixRenderer() {
        FrameRenderer frameRenderer = (FrameRenderer) list.getCellRenderer();
        frameRenderer.setDisplayType(showSubclassInstances);
    }

    protected void addButtons(Action viewAction, LabeledComponent c) {
        // c.addHeaderButton(createReferencersAction());
        c.addHeaderButton(createConfigureAction());
        c.addHeaderButton(createCreateAction());
        c.addHeaderButton(createCopyAction());
        c.addHeaderButton(createDeleteAction());
        c.addHeaderButton(createCreateAnonymousAction());
    }

    private void addClsListeners() {
        Iterator i = classes.iterator();
        while (i.hasNext()) {
            Cls cls = (Cls) i.next();
            cls.addClsListener(_clsListener);
            cls.addFrameListener(_clsFrameListener);
        }
    }

    private void addInstanceListeners() {
        ListModel model = getModel();
        int start = list.getFirstVisibleIndex();
        int stop = list.getLastVisibleIndex();
        if (start < 0) {
            return;
        }
        for (int i = start; i < stop + 1; i++) {
            FrameWithBrowserText fbt = (FrameWithBrowserText) model.getElementAt(i);
            Instance instance = (Instance) fbt.getFrame();
            addInstanceListener(instance);
        }
    }

    private void removeInstanceListeners() {
        Iterator i = listenedToInstances.iterator();
        while (i.hasNext()) {
            Instance instance = (Instance) i.next();
            instance.removeFrameListener(_instanceFrameListener);
        }
        listenedToInstances.clear();
    }

    private void addInstanceListener(Instance instance) {
        instance.addFrameListener(_instanceFrameListener);
        listenedToInstances.add(instance);
    }

    protected Action createCreateAction() {
        createAction = new CreateAction("Create new instance", OWLIcons.getCreateIndividualIcon(OWLIcons.RDF_INDIVIDUAL)) {
            @Override
            public void onCreate() {
                if (!classes.isEmpty()) {
                    RDFSClass firstType = (RDFSClass) CollectionUtilities.getFirstItem(classes);
                    //final String name = owlModel.createNewResourceName(NamespaceUtil.getLocalName(firstType.getName()));
                    final String name = new UUIDInstanceURI(owlModel).generateUniqueName();
                    //log.info("Creating new instance with name: " + name);
                    Transaction<Instance> t = new Transaction<Instance>(owlModel, "Create Individual: " +
                            NamespaceUtil.getLocalName(name) + " of " + CollectionUtilities.toString(classes), name) {
                        private Instance instance;

                        @Override
                        public boolean doOperations() {
                            instance = owlModel.createInstance(name, classes);
                            if (instance instanceof Cls) {
                                Cls newCls = (Cls) instance;
                                if (newCls.getDirectSuperclassCount() == 0) {
                                    newCls.addDirectSuperclass(owlModel.getOWLThingClass());
                                }
                            }
                            return true;
                        }

                        @Override
                        public Instance getResult() {
                            return instance;
                        }
                    };
                    t.execute();
                    list.setSelectedValue(new FrameWithBrowserText(t.getResult()), true);
                }
            }
        };
        return createAction;
    }

    //TODO
    protected Action createCreateAnonymousAction() {
        createAnonymousAction = new CreateAction("Create anonymous instance", OWLIcons.getCreateIndividualIcon(OWLIcons.RDF_ANON_INDIVIDUAL)) {
            @Override
            public void onCreate() {
                if (!classes.isEmpty()) {
                    String name = owlModel.getNextAnonymousResourceName();
                    Instance instance = owlModel.createInstance(new FrameID(name), classes, false);
                    if (instance instanceof Cls) {
                        Cls newCls = (Cls) instance;
                        if (newCls.getDirectSuperclassCount() == 0) {
                            newCls.addDirectSuperclass(owlModel.getOWLThingClass());
                        }
                    }
                    list.setSelectedValue(new FrameWithBrowserText(instance), true);
                }
            }
        };
        return createAnonymousAction;
    }

    protected Action createConfigureAction() {
        return new ConfigureAction() {
            @Override
            public void loadPopupMenu(JPopupMenu menu) {
                menu.add(createSetDisplaySlotMenuItem());
                menu.add(createSetDisplaySlotsMenuItem());
                menu.add(createShowAllInstancesAction());
            }
        };
    }

    protected JMenuItem createShowAllInstancesAction() {
        Action action = new AbstractAction("Show Subclass Instances") {
            public void actionPerformed(ActionEvent event) {
                setShowAllInstances(!showSubclassInstances);
            }
        };
        JMenuItem item = new JCheckBoxMenuItem(action);
        item.setSelected(showSubclassInstances);
        return item;
    }

    private void setShowAllInstances(boolean b) {
        showSubclassInstances = b;
        // ApplicationProperties.setBoolean(SHOW_SUBCLASS_INSTANCES, b);
        reload();
        fixRenderer();
    }

    protected Cls getSoleAllowedCls() {
        Cls cls;
        if (classes.size() == 1) {
            cls = CollectionUtilities.getFirstItem(classes);
        } else {
            cls = null;
        }
        return cls;
    }

    protected JMenuItem createSetDisplaySlotMenuItem() {
        JMenuItem menu = new JMenuItem("Set Display Property");
        menu.addActionListener(new ActionListener() {
            @SuppressWarnings("unchecked")
            public void actionPerformed(ActionEvent e) {
                Cls cls = getSoleAllowedCls();
                ArrayList<Slot> slots = new ArrayList<Slot>(cls.getVisibleTemplateSlots());
                slots.add(cls.getKnowledgeBase().getNameSlot());
                Collections.sort(slots);
                Slot slot = ProtegeUI.getSelectionDialogFactory().selectProperty(AssertedInstancesListPanel.this, owlModel, slots);
                if (slot != null) {
                    cls.setDirectBrowserSlot(slot);
                }
            }
        });
        return menu;
    }

    /*
     * A significant change  happened at svn revision 18150.
     * The functionality at that time was more friendly than this version but didn't work
     * well when there were multiple properties
     */
    protected JMenuItem createSetDisplaySlotsMenuItem() {
        JMenuItem menu = new JMenuItem(createSetDisplaySlotMultipleAction());
        menu.setEnabled(true);
        return menu;
    }

    protected Action createSetDisplaySlotMultipleAction() {
        return new AbstractAction("Set Display Properties") {
            private static final long serialVersionUID = -5884471155567559812L;

            public void actionPerformed(ActionEvent event) {
                Cls cls = getSoleAllowedCls();
                BrowserSlotPattern currentPattern = getSoleAllowedCls().getBrowserSlotPattern();
                MultiSlotPanel panel = new MultiSlotPanel(currentPattern, cls);
                int rval = ModalDialog.showDialog(AssertedInstancesListPanel.this, panel, "Multislot Display Pattern",
                        ModalDialog.MODE_OK_CANCEL);
                if (rval == ModalDialog.OPTION_OK) {
                    BrowserSlotPattern pattern = panel.getBrowserTextPattern();
                    if (pattern != null) {
                        cls.setDirectBrowserSlotPattern(pattern);
                    }
                }
                updateLabel();
                repaint();
            }
        };
    }

    protected Action createDeleteAction() {
        //deleteAction = new DeleteInstancesAction(this);
        deleteAction = new DeleteInstanceOrMoveToTrashAction(ResourceKey.INSTANCE_DELETE, this);
        return deleteAction;
    }


    /**
     * Act upon copying the instance. When instance is copied, delete it's identifier property,
     * and replace classHierarchyURI with UUID
     *
     * Modified by Hemed, 2017-05-02
     *
     */
    protected Action createCopyAction() {
        copyAction = new MakeCopiesAction(ResourceKey.INSTANCE_COPY, this) {
            @Override
            protected Instance copy(Instance instance, boolean isDeep) {
                final String newName = new UUIDInstanceURI(owlModel).generateUniqueName();
                //log.info("Copying instance: " + instance.getName() + " to " + newName);
                Instance copy = (Instance) super.copy(instance, isDeep).rename(newName);
                log.info("Copied instance: " + instance.getName() + " to " + copy.getName());

                //Get UUID from instance URI
                String uuid = UUIDWidget.getUUIDFromInstanceURI(copy);

                //Get corresponding class URI in xsd:anyURI datatype
                Object classHierarchyURI = owlModel.createRDFSLiteral(
                        InstanceUtil.getClassURIPrefix(copy).toLowerCase() + uuid,
                        owlModel.getXSDanyURI()
                );

                //Build properties that need to be modified before instance is displaced
                Map<RDFProperty, Object> properties = new HashMap<RDFProperty, Object>();
                properties.put(owlModel.getRDFProperty(UBBOntologyNamespaces.IDENTIFIER), null);
                properties.put(owlModel.getRDFProperty(UBBOntologyNamespaces.UUID), uuid);
                properties.put(owlModel.getRDFProperty(UBBOntologyNamespaces.CLASS_HIERARCHY_URI), classHierarchyURI);

                //After copying, modify particular properties for a copied instance
                InstanceUtil.modifyProperties(copy, properties);
                setSelectedInstance(copy);
                return copy;
            }
        };
        return copyAction;
    }



    protected Action createReferencersAction() {
        return new ReferencersAction(ResourceKey.INSTANCE_VIEW_REFERENCES, this);
    }

    protected Action createViewAction() {
        return new ViewAction(ResourceKey.INSTANCE_VIEW, this) {
            @Override
            public void onView(Object o) {
                owlModel.getProject().show((Instance) o);
            }
        };
    }

    @Override
    public void dispose() {
        removeClsListeners();
        removeInstanceListeners();
    }

    public JComponent getDragComponent() {
        return list;
    }

    private SimpleListModel getModel() {
        return (SimpleListModel) list.getModel();
    }

    private boolean isSelectionEditable() {
        boolean isEditable = true;
        Iterator i = getSelection().iterator();
        while (i.hasNext()) {
            Instance instance = (Instance) i.next();
            if (!instance.isEditable()) {
                isEditable = false;
                break;
            }
        }
        return isEditable;
    }

    @Override
    public void onSelectionChange() {
        // Log.enter(this, "onSelectionChange");
        boolean editable = isSelectionEditable();
        ComponentUtilities.setDragAndDropEnabled(list, editable);
        updateButtons();
    }

    private void removeInstanceListener(Instance instance) {
        instance.removeFrameListener(_instanceFrameListener);
        listenedToInstances.remove(instance);
    }

    private void removeClsListeners() {
        Iterator i = classes.iterator();
        while (i.hasNext()) {
            Cls cls = (Cls) i.next();
            cls.removeClsListener(_clsListener);
            cls.removeFrameListener(_clsFrameListener);
        }
    }

    public void setClses(Collection newClses) {
        removeClsListeners();
        classes = new ArrayList(newClses);
        list.setClasses(newClses);
        reload();
        updateButtons();
        addClsListeners();
    }

    public void reload() {
        removeInstanceListeners();
        getModel().setValues(getFramesWithBrowserText(classes));
        addInstanceListeners();
        reloadHeader(classes);
        updateLabel();
    }

    private void reloadHeader(Collection clses) {
        StringBuffer text = new StringBuffer();
        Icon icon = null;
        Iterator i = clses.iterator();
        while (i.hasNext()) {
            Cls cls = (Cls) i.next();
            if (icon == null) {
                icon = cls.getIcon();
            }
            if (text.length() != 0) {
                text.append(", ");
            }
            text.append(cls.getBrowserText());
        }
        JLabel label = (JLabel) header.getComponent();
        label.setText(text.toString());
        label.setIcon(icon);
    }

    public void setSelectedInstance(Instance instance) {
        list.setSelectedValue(new FrameWithBrowserText(instance), true);
        updateButtons();
    }


    private void updateButtons() {
        Cls cls = CollectionUtilities.getFirstItem(classes);
        createAction.setEnabled(cls == null ? false : cls.isConcrete());
        createAnonymousAction.setEnabled(cls == null ? false : cls.isConcrete());

        Instance instance = (Instance) getSoleSelection();
        boolean allowed = instance != null && instance instanceof SimpleInstance;
        copyAction.setAllowed(allowed);
    }


    private void createListRenderer() {
        FrameRenderer listRenderer = new FrameRenderer() {
            @Override
            public void load(Object value) {
                if (value instanceof FrameWithBrowserText) {
                    showFrameWithBrowserText((FrameWithBrowserText) value);
                } else {
                    super.load(value);
                }
            }

            private void showFrameWithBrowserText(FrameWithBrowserText fbt) {
                setMainText(StringUtilities.unquote(fbt.getBrowserText()));
                if (fbt.getFrame() != null) {
                    setMainIcon(fbt.getFrame().getIcon()); //should not go to the server
                }
            }
        };
        list.setCellRenderer(listRenderer);
    }

    @Override
    public Collection getSelection() {
        Collection<FrameWithBrowserText> fbts = super.getSelection();
        if (fbts == null) {
            return null;
        }
        Collection<Instance> instances = new HashSet<Instance>();
        for (FrameWithBrowserText fbt : fbts) {
            instances.add((Instance) fbt.getFrame());
        }
        return instances;
    }


    /**
     * Does nothing anymore. This functionality moved to the menu button.
     *
     * @deprecated
     */
    @Deprecated
    public void setShowDisplaySlotPanel(boolean b) {

    }
    
    /*
     * Code for getting the instances of classes that tries to optimize performance
     * for client-server
     */

    private Collection<FrameWithBrowserText> getFramesWithBrowserText(Collection<Cls> clses) {
        if (!showSubclassInstances && useCacheHeuristics() &&
                owlModel.getProject().isMultiUserClient() &&
                hasOnlyDataBrowserSlots(clses) && isCached(clses)) {
            //use the cache
            return getValuesFromCache(clses);
        } else { //go to the server
            GetInstancesAndBrowserTextJob job = new GetInstancesAndBrowserTextJob(owlModel, clses, !showSubclassInstances);
            return job.execute();
        }
    }

    /**
     * Determine whether the browser slots are nested (e.g. they are one level deep).
     * If they are, then don't use the local cache, use the job to get the instances.
     */
    private boolean hasOnlyDataBrowserSlots(Collection<Cls> clses) {
        for (Cls cls : clses) {
            BrowserSlotPattern bsp = cls.getBrowserSlotPattern();
            if (bsp != null) {
                List<Slot> slots = bsp.getSlots();
                for (Slot slot : slots) {
                    if (slot.getValueType() == ValueType.INSTANCE || slot.getValueType() == ValueType.CLS) {
                        return false;
                    }
                }
            }
        }
        return true;
    }


    /**
     * This is a heuristic if the instances of clses are cached
     */
    private boolean isCached(Collection<Cls> clses) {
        for (Cls cls : clses) {
            if (!RemoteClientFrameStore.isCached(cls, owlModel.getSystemFrames().getDirectInstancesSlot(), null, false)) {
                return false;
            }
            Collection<Instance> instances = cls.getDirectInstances(); //should not call the server
            for (Instance instance : instances) {
                if (!RemoteClientFrameStore.isCacheComplete(instance)) {
                    return false;
                }
            }
        }
        return true;
    }


    //TODO: refactor out
    private List<FrameWithBrowserText> getValuesFromCache(Collection<Cls> clses) {
        List<FrameWithBrowserText> framesWithBrowserText = new ArrayList<FrameWithBrowserText>();
        for (Cls cls : clses) {
            Collection<Instance> instances = showSubclassInstances ? cls.getInstances() : cls.getDirectInstances();
            for (Instance instance : instances) {
                framesWithBrowserText.add(
                        new FrameWithBrowserText(instance, instance.getBrowserText(), instance.getDirectTypes()));
            }
        }
        Collections.sort(framesWithBrowserText, new FrameWithBrowserTextComparator());
        return framesWithBrowserText;
    }


    private boolean useCacheHeuristics() {
        return ApplicationProperties.getBooleanProperty(OWLUI.USE_CACHE_HEURISTICS_PROP, true);
    }

}