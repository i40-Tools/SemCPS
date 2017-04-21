package industryStandard;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

/**
 * Generates data based on OPCUA file methods are sepcific to OPCUA only
 * 
 * @author omar
 *
 */
public class Opcua extends IndustryStandards {

	private RDFNode object;
	private RDFNode subject;
	private RDFNode predicate;
	private String parentNode;
	private Model model;
	private LinkedHashSet<String> forAttribute;
	private LinkedHashSet<String> forID;
	private LinkedHashSet<String> forRefSemantic;
	private Set<String> UAObject;
	private LinkedHashSet<String> UAObjectType;
	private Resource generictValue;
	private Resource genericSubject;
	private LinkedHashSet<String> foreClassVersion;
	private LinkedHashSet<String> foreClassClassificationClass;
	private LinkedHashSet<String> foreClassIRDI;

	public Opcua(RDFNode subject, RDFNode object, RDFNode predicate, Model model, LinkedHashSet<String> forAttribute,
			LinkedHashSet<String> forID, LinkedHashSet<String> forRefSemantic, Set<String> UAObject,
			LinkedHashSet<String> UAObjectType, LinkedHashSet<String> foreClassVersion,
			LinkedHashSet<String> foreClassIRDI, LinkedHashSet<String> foreClassClassificationClass) {
		this.subject = subject;
		this.object = object;
		this.predicate = predicate;
		this.model = model;
		this.forAttribute = forAttribute;
		this.forID = forID;
		this.forRefSemantic = forRefSemantic;
		this.UAObject = UAObject;
		this.UAObjectType = UAObjectType;
		this.foreClassVersion = foreClassVersion;
		this.foreClassIRDI = foreClassIRDI;
		this.foreClassClassificationClass = foreClassClassificationClass;
	}

	/**
	 * To add data to opcua Lists
	 * 
	 * @param allSubjects2
	 */
	public void addsDataforOPCUA(ArrayList<Resource> allSubjects) {
		// return if ontology is aml.
		if (subject.asNode().getNameSpace().contains("aml")) {
			return;
		}

		if (object.isLiteral()) {

			parentNode = getParentNode();
			if (parentNode != null) {
				setValues();
				addParentSubject(allSubjects, UAObject, ":" + subject.asNode().getLocalName());
			}

			// RefSemantic part starts here for opcua
			if (object.asLiteral().getLexicalForm().equals("RefSemantic")) {

				addOPCUAGeneric(allSubjects, forRefSemantic, "RefSemantic");
			}

			// ID part starts here
			if (object.asLiteral().getLexicalForm().equals("ID")) {
				// addOPCUAid(allSubjects);
				addOPCUAGeneric(allSubjects, forID, "ID");

			}

			if (object.asLiteral().getLexicalForm().equals("eClassVersion")) {
				addOPCUAGeneric(allSubjects, foreClassVersion, "eClassVersion");
			}

			if (object.asLiteral().getLexicalForm().equals("eClassIRDI")) {
				addOPCUAGeneric(allSubjects, foreClassIRDI, "eClassIRDI");
			}

			if (object.asLiteral().getLexicalForm().equals("eClassClassificationClass")) {
				addOPCUAGeneric(allSubjects, foreClassClassificationClass, "eClassClassificationClass");
			}

		}

	}

	private void addOPCUAGeneric(ArrayList<Resource> allSubjects, LinkedHashSet<String> generic, String name) {
		// TODO Auto-generated method stub

		// now we gets its parent node to know to which attribute it
		// belong
		parentNode = getParentNode();

		// set RefSemantic/eclass/e,t,c Value as an Object based on the current
		// subject , now we have "hasValue" object.
		setValues();

		// now get the object triple so we can extract its value
		// "hasValue"
		String genericLiteral = getObjectValue(generictValue.asResource());

		// now this part is for Attribute
		// We fill the list forAttribute which has eClassverson e,t,c Variable
		// we know its object so get its subject and add it to list for
		// attribute
		addParentSubject(allSubjects, forAttribute, ":" + subject.asNode().getLocalName());

		// specific case goes into attributes
		if (name == "eClassClassificationClass" || name == "eClassIRDI" || name == "eClassVersion") {

			addSubjectURI(genericSubject, forAttribute, ":" + generictValue.asResource().getLocalName());
			addSubjectURI(generictValue, generic, ":remove" + genericLiteral);
		} else {
			// this is forRefSemantic.txt e.t,c adds the subject and its value
			addSubjectURI(genericSubject, generic, ":remove" + genericLiteral);
		}
	}

	/**
	 * Gets the literal value for the given object as subject
	 * 
	 * @param rdfSubject
	 * @return
	 */

	String getObjectValue(Resource rdfSubject) {

		String rdfValue = null;
		StmtIterator stmts = model.listStatements(rdfSubject.asResource(), null, (RDFNode) null);
		while (stmts.hasNext()) {
			Statement stmte = stmts.nextStatement();
			if (stmte.getObject().isLiteral()) {
				// gets Value
				rdfValue = stmte.getObject().asLiteral().getLexicalForm();
				// optional part for ID only
				rdfValue = rdfValue.replace("{", "");
				rdfValue = rdfValue.replace("}", "");
			}
		}

		return rdfValue;
	}

	/**
	 * Just returns parents node for opcua
	 * 
	 * @return
	 */
	private String getParentNode() {
		// TODO Auto-generated method stub

		String parentNode = null;
		StmtIterator stmts = model.listStatements(subject.asResource(), null, (RDFNode) null);

		// goes through its statements and gets parent node id
		while (stmts.hasNext()) {
			Statement stmte = stmts.nextStatement();
			if (stmte.getPredicate().asNode().getLocalName().equals("parentNodeId")) {
				parentNode = stmte.getObject().asLiteral().getLexicalForm();
			}

		}

		return parentNode;
	}

	/**
	 * Sets values and subject for the required value Will need to be updated
	 * for other Classes and attributes Currently only has ID and refSemantic
	 * 
	 * @param type
	 */
	void setValues() {

		StmtIterator stmts = model.listStatements(subject.asResource(), null, (RDFNode) null);
		while (stmts.hasNext()) {
			Statement stmte = stmts.nextStatement();

			// get opcua refsemantic subject and its value object
			if (stmte.getPredicate().asNode().getLocalName().equals("hasValue")) {
				generictValue = stmte.getObject().asResource();
				genericSubject = stmte.getSubject();
				break;

			}

		}

	}

	/**
	 * checks if current object is id or not
	 * 
	 * @return
	 */
	boolean checkIfId() {
		StmtIterator stmts = model.listStatements(genericSubject.asResource(), null, (RDFNode) null);
		while (stmts.hasNext()) {
			Statement stmte = stmts.nextStatement();
			if (stmte.getPredicate().asNode().getLocalName().equals("hasBrowseName")) {
				if (stmte.getObject().asLiteral().getLexicalForm().equals("ID")) {

					return true;

				}
			}
		}
		return false;

	}

	/**
	 * Searches parent subject and adds it to list based on parentnodeID for OPC
	 * UA.
	 * 
	 * @param allSubjects
	 * @param forID
	 */
	void addParentSubject(ArrayList<Resource> allSubjects, Set<String> forResource, String value) {
		for (int i = 0; i < allSubjects.size(); i++) {
			StmtIterator stmts = model.listStatements(allSubjects.get(i), null, (RDFNode) null);
			while (stmts.hasNext()) {
				Statement stmte = stmts.nextStatement();

				if (stmte.getPredicate().asNode().getLocalName().equals("hasNodeId")) {

					// matches parent node with node id
					if (stmte.getObject().asLiteral().getLexicalForm().equals(parentNode)) {

						// this part is just for ID value. can be improved
						if (stmte.getSubject().asNode().getLocalName().contains("UAObject")
								&& !stmte.getSubject().asNode().getLocalName().contains("Type")
								&& !stmte.getSubject().asNode().getLocalName().contains("Variable")) {
							addSubjectURI(stmte.getSubject(), UAObject, value);

						} else if (!stmte.getSubject().asNode().getLocalName().contains("UAObject")) {
							// for ID
							addSubjectURI(allSubjects.get(i), forResource, value);
						} else {
							// adds role class objects
							// TODO : add other roleclass attributes priority :3

							addSubjectURI(allSubjects.get(i), UAObjectType, value);

						}

					}
				}

			}

		}

	}

}
