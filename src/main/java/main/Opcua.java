package main;

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
	private String parentNode;
	private Model model;
	private Resource refSemanticValue;
	private Resource IDSubject;
	private Resource refSemanticSubject;
	private LinkedHashSet<String> forAttribute;
	private LinkedHashSet<String> forID;
	private LinkedHashSet<String> forRefSemantic;
	private Set<String> forInternalElements;

	public Opcua(RDFNode subject, RDFNode object, RDFNode predicate, Model model, LinkedHashSet<String> forAttribute,
			LinkedHashSet<String> forID, LinkedHashSet<String> forRefSemantic, Set<String> forInternalElements) {
		this.subject = subject;
		this.object = object;
		this.model = model;
		this.forAttribute = forAttribute;
		this.forID = forID;
		this.forRefSemantic = forRefSemantic;
		this.forInternalElements = forInternalElements;
	}

	/**
	 * To add data to opcua Lists
	 * 
	 * @param allSubjects2
	 */
	void addsDataforOPCUA(ArrayList<Resource> allSubjects) {
		if (object.isLiteral()) {

			// RefSemantic part starts here for opcua
			if (object.asLiteral().getLexicalForm().equals("RefSemantic")) {

				addRefSemantic(allSubjects);
			}

			// ID part starts here
			if (object.asLiteral().getLexicalForm().equals("ID")) {
				addOPCUAid(allSubjects);

			}

		}

	}

	/**
	 * 1- match the vale 2- getParentNode 3- set founded subject/object Values
	 * 4- Use the object and gets its triple and fill the according list 5- Fill
	 * other lists
	 * 
	 * @param allSubjects
	 */
	void addRefSemantic(ArrayList<Resource> allSubjects) {

		// now we gets its parent node to know to which attribute it
		// belong
		parentNode = getParentNode();

		// set RefSemantic Value as an Object based on the current
		// subject , now we have "hasValue" object.
		setValues("RefSemantic");

		// now get the object triple so we can extract its value
		// "hasValue"
		String refSemanticLiteral = getObjectValue(refSemanticValue.asResource());

		// now this part is for Attribute
		// We fill the list forAttribute which has RefSemantic Variable
		// we know its object so get its subject and add it to list for
		// attribute
		addParentSubject(allSubjects, forAttribute, ":" + subject.asNode().getLocalName());

		// this is forRefSemantic.txt adds the subject and its value
		addSubjectURI(refSemanticSubject, forRefSemantic, ":remove" + refSemanticLiteral);

		// RefSemantic part ends here

	}

	/**
	 * Adds OPC UA ids
	 * 
	 * @param allSubjects
	 */
	private void addOPCUAid(ArrayList<Resource> allSubjects) {
		// now we gets its parent node to know to which attribute it
		// belong

		parentNode = getParentNode();

		// set the ID object,subject
		setValues("ID");

		// gets the ID value
		String IDValue = getObjectValue(IDSubject.asResource());

		// adds required values forAttribute.txt
		addParentSubject(allSubjects, forAttribute, ":" + IDSubject.asNode().getLocalName());

		// adds forID.txt its subject and value
		addSubjectURI(IDSubject, forID, ":remove" + IDValue);

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
	void setValues(String type) {

		StmtIterator stmts = model.listStatements(subject.asResource(), null, (RDFNode) null);
		while (stmts.hasNext()) {
			Statement stmte = stmts.nextStatement();

			// get opcua refsemantic subject and its value object
			if (stmte.getPredicate().asNode().getLocalName().equals("hasValue")) {
				if (type == "ID") {
					IDSubject = stmte.getObject().asResource();
				} else if (type == "RefSemantic") {

					refSemanticValue = stmte.getObject().asResource();
					refSemanticSubject = stmte.getSubject();

				}
			}

		}

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
						if (stmte.getSubject().asNode().getLocalName().contains("UAObject")) {

							addSubjectURI(allSubjects.get(i), forInternalElements, value);

						} else {
							// for ID
							addSubjectURI(allSubjects.get(i), forResource, value);
						}

					}
				}

			}
		}

	}

}
