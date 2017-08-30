package industryStandard;

import java.io.FileNotFoundException;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

/**
 * 
 * @author Irlan Grangel
 *
 * Represents the AutomationML Standard as an RDF graph
 */
public class AML extends IndustryStandards {
	
	
	
	public AML(Model model, int newNumber) {
		super(model, newNumber);
	}

	/**
	 * Automation ML part for data population
	 * @throws FileNotFoundException 
	 */
	public void addsDataforAML() throws FileNotFoundException {

		StmtIterator iterator = model.listStatements();
		// RefSemantic part starts here
		while (iterator.hasNext()) {

			Statement stmt = iterator.nextStatement();
			subject = stmt.getSubject();
			predicate = stmt.getPredicate();
			object = stmt.getObject();

			if(number == 3){

			}// all subjects are added according to ontology e.g aml
			else{
				addSubjectURI(subject, "", number, "hasDocument");
			}

			hasAttributeName();
			hasAttribute();
			addDomainRange();
			addHasType();
			hasAttributeValue();
			hasIdentifier();
			eClassCheck();

		}

		addGenericObject("hasExternalReference","refBaseClassPath" );
		addGenericObject("hasInternalLink","hasRefPartnerSideB" );
		addGenericObject("hasInternalLink","hasRefPartnerSideA" );
		addGenericObject("hasRefSemantic","hasCorrespondingAttributePath");
	}


	private void eClassCheck() {
		/***
		 * Eclass part starts here. TODO refactoring and functions only for test
		 * purpose now
		 */
		if (object.isLiteral()) {
			if (checkEclass(object)) {
				StmtIterator stmts = model.listStatements(subject.asResource(), null,(RDFNode) null);
				while (stmts.hasNext()) {
					Statement stmte = stmts.nextStatement();

					if (stmte.getPredicate().asNode().getLocalName().equals("hasAttributeValue")) {

						if (object.asLiteral().getLexicalForm()
								.equals("eClassClassificationClass")) {
							addSubjectURI(subject,":remove" + 
									stmte.getObject().asLiteral().getLexicalForm(),
									number, "hasEClassClassificationClass");
						}

						if (object.asLiteral().getLexicalForm().equals("eClassVersion")) {
							addSubjectURI(subject, ":remove" + 
									stmte.getObject().asLiteral().getLexicalForm(),
									number, "hasEclassVersion");
						}

						if (object.asLiteral().getLexicalForm().equals("eClassIRDI")) {
							addSubjectURI(subject, ":remove" + 
									stmte.getObject().asLiteral().getLexicalForm(),
									number, "hasEclassIRDI");
						}

					}
				}
			}
		}
	}



	private void hasIdentifier() {
		// RefSemantic part starts here
		if (predicate.asNode().getLocalName().equals("identifier")) {
			// gets the literal ID value and add it to hasID
			addSubjectURI(subject, ":remove" + object.asLiteral().getLexicalForm(), number,
					"has" + getType(subject) + "ID");
		}
	}



	private void hasAttribute() {
		if (predicate.asNode().getLocalName().equals("hasAttribute")) {
			// gets all classes which hasAttribute relation
			addSubjectURI(subject, ":" + object.asNode().getLocalName(), number,
					"has" + getType(subject));
		}
	}

	private void hasAttributeValue() {
		if (predicate.asNode().getLocalName().equals("hasAttributeValue")) {
			if (!checkEclass(object)) {
				addSubjectURI(subject, ":remove" +object.asLiteral().getLexicalForm(), number,
						predicate.asNode().getLocalName());
			}
		}
	}

	private void addHasType() {
		if (predicate.asNode().getLocalName().equals("type")) {
			if (number != 3)
				addSubjectURI(subject, ":" + object.asNode().getLocalName(), number,
						"has" + predicate.asNode().getLocalName());
		}
	}

	private void addDomainRange() {
		// Adds domain and range
		if (predicate.asNode().getLocalName().equals("domain")
				|| predicate.asNode().getLocalName().equals("range")) {

			addSubjectURI(subject, ":" + object.asNode().getLocalName(), 1,
					"has" + predicate.asNode().getLocalName());
			addSubjectURI(subject, ":" + object.asNode().getLocalName(), 2,
					"has" + predicate.asNode().getLocalName());
			addSubjectURI(subject, "", 1, "hasDocument");
			addSubjectURI(subject, "", 2, "hasDocument");
			addSubjectURI(object, "", 1, "hasDocument");
			addSubjectURI(object, "", 2, "hasDocument");			
		}
	}

	private void hasAttributeName() {
		if (predicate.asNode().getLocalName().equals("hasAttributeName")) {
			if (!checkEclass(object)) {
				if (!getType(subject).equals("Attribute")) {
					addSubjectURI(subject, ":remove" + object.asLiteral().getLexicalForm(), number,
							"has" + getType(subject) + predicate.asNode().getLocalName()
							.replace("has", ""));
				} else {
					addSubjectURI(subject, ":remove" + object.asLiteral().getLexicalForm(), number,
							predicate.asNode().getLocalName());
				}
			}
		}
	}

	

	

	/**
	 * checks if its elcass then ignore it because we only need values with object which 
	 * can be unique.
	 * @param object
	 * @return
	 */
	boolean checkEclass(RDFNode object){
		if (object.asLiteral().getLexicalForm().equals("eClassClassificationClass")
				|| object.asLiteral().getLexicalForm().equals("eClassVersion")
				|| object.asLiteral().getLexicalForm().equals("eClassIRDI")) {
			return true;
		}
		return false;
	}


}
