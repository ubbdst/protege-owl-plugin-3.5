# Protege OWL Plugin 3.6
The original Protege 3.5 OWL plugin with some customizations from the University of Bergen Library. There exist new versions of Protege but according to our use cases, we are inspired by the Protege 3.5 for its richness in forms (widgets) and its robust server-client architecture. Hence we decided to modify it to match our requirements. 

## Customizations include :

- Make use of UUID for the individual URI
- Make Individual URI not editable in the Instance Browser panel.
- Create <code>UUIDWidget</code> that automatically stores instance UUID.
- Create <code>ClassHierarchyURIWidget</code> that holds hierarchical URI for an instance. The value of this widget is of type <code>XSD:anyURI</code>
- Create <code>UBBSignatureWidget</code> that stores identifier for a particular instance. This widget communicates with <code>ClassHierarchyURIWidget</code> on value change and modifies the value of <code>ClassHierarchyURIWidget</code> accordingly. Identifier is validated by checking the entire knowledgebase for the uniqueness.
- Modify some instance properties for the copied individual
- Add some utility classes for convenience methods
- Add external UBB plugins to the core codes so that we don't have to install them separately
- Solve some bugs in the original OWL 3.5 plugin, see https://prosjekt.uib.no/issues/7757 
- Add a whole nortion of Trash. When instance is deleted, put it to the Trash class, such that it can be recovered. The logic is that, if instance belongs to a class other that Trash, and deletion is confirmed, then move the instance to the Trash class. Otherwise, delete instance permanently.
- Add new, flat icons for classes and instances and in particular, icons for Trash class and its instances. This will help users not to confuse instances of Trash to other instances.
- Add support for merge feature, such that two instance can be merged to one. Merging means copying all property values of the source instance to the target instance and then make all references of the source instance point to the target instance. A reference instance is an instance whereby source instance is an object for a certain property of this instance. 
In technical words, merging means :
<code>s ?p ?o ===> ?s2 ?p ?o (except dct:identifier, ubbont:uuid og ubbont:classHierarchyURI) </code>  and <code>?o2 ?p2 ?s ===> o2 ?p2 ?s2 </code> 

## Original source codes 
For Protege Core 3.5 : http://smi-protege.stanford.edu/svn/

For Protege OWL Plugin 3.5 : http://smi-protege.stanford.edu/repos/protege/owl/trunk/

## Installation instructions

Download a final release from https://github.com/ubbdst/protege-owl-plugin/releases and extract it (including its dependencies, if any) to the Protege OWL plugin directory. You find it with a name <code>"edu.stanford.smi.protegex.owl"</code> under <code>"plugins"</code> folder in your Protege installation directory. 

However, you will have to replace the old <code>protege-owl.jar</code> file to this new one, otherwise there will be a conflict between the two.
Note that, if you are running Protege server-client mode, you will have to do this both to the server and to all of your clients. 

