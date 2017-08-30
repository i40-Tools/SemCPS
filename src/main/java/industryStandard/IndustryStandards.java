package industryStandard;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

/**
 * This class contains common methods to produce the conversion of Industry 4.0 standards 
 * information models to a canonical model, i.e., RDF
 * @author omar
 */

public class IndustryStandards {

	public Map<String, Collection<String>> generic = new HashMap<String, Collection<String>>();
	public Model model;
	public Resource subject;
	public Property predicate;
	public RDFNode object;
	int number;

	
	
	public IndustryStandards() {
		super();
	}

	public IndustryStandards(Model newModel, int newNumber) {
		model = newModel;
		number = newNumber;
	}

	/**
	 * add subjects with appropriate ontology All the data is added into generic
	 * Hash map with a key identifying to what it belongs.
	 * 
	 * @param subject
	 * @param subjects
	 * @throws FileNotFoundException
	 */
	protected void addSubjectURI(RDFNode subject, String predicate, int number, String name) {

		predicate = predicate.trim();

		// checks if key is already there if not create a new one
		if (generic.get(name) == null) {
			generic.put(name, new LinkedHashSet<String>());
		}

		if (predicate.equals(":remove")) {
			predicate = ":remove" + "null";
		}

		// adds all data into generic hash map
		if (subject.asNode().getNameSpace().contains("aml")) {
			// checks if its a literal or object

			if (predicate.contains("decimal") || (predicate.contains("string"))
					|| (predicate.contains("integer")) || (predicate.contains("float"))) {
				generic.get(name).add("aml" + number + ":" + subject.asNode().getLocalName() + "\t"
						+ "xsd" + predicate);
			} else {

				if (!predicate.contains("remove")) {
					generic.get(name).add("aml" + number + ":" + subject.asNode().getLocalName()
							+ "\t" + "aml" + number + predicate);
				} else {
					generic.get(name).add("aml" + number + ":" + subject.asNode().getLocalName()
							+ "\t" + "aml" + predicate);
				}
			}
		}
	}
	
	/**
	 * Gets the class of object.
	 * 
	 * @param name
	 * @return
	 */
	String getType(RDFNode name) {
		String type = null;
		StmtIterator stmts = model.listStatements(name.asResource(), null, (RDFNode) null);
		while (stmts.hasNext()) {
			Statement stmte = stmts.nextStatement();

			if (stmte.getPredicate().asNode().getLocalName().toString().equals("type")) {
				type = stmte.getObject().asNode().getLocalName();
			}
		}
		return type;
	}
	
	/**
	 * get predicate Value
	 * @param name
	 * @return
	 */
	String getValue(RDFNode name, String predicate) {
		String type = null;
		StmtIterator stmts = model.listStatements(name.asResource(), null, (RDFNode) null);
		while (stmts.hasNext()) {
			Statement stmte = stmts.nextStatement();

			if (stmte.getPredicate().asNode().getLocalName().toString().equals(predicate)) {
				type = stmte.getObject().asLiteral().getLexicalForm();
			}
		}
		return type;
	}
	
	/**
	 * TODO to write comment
	 * @param firstPredicate
	 * @param secondPredicate
	 * @throws FileNotFoundException
	 */
	public void addGenericObject(String firstPredicate, String secondPredicate ) 
			throws FileNotFoundException{
		if (predicate.asNode().getLocalName().equals(firstPredicate)) {
			// adds for attribute
			addSubjectURI(subject, ":" + object.asNode().getLocalName(), number,
					"has" + getType(subject));
			// adds for refsemantic.txt
			addGenericValue(predicate.asNode().getLocalName(),secondPredicate);
		}
	}
	
	/**
	 * Add refSemantic in the list
	 * 
	 * @throws FileNotFoundException
	 */
	private void addGenericValue(String type, String predicate) throws FileNotFoundException {
		StmtIterator stmts = model.listStatements(object.asResource(), null, (RDFNode) null);
		while (stmts.hasNext()) {
			Statement stmte = stmts.nextStatement();
			if(stmte.getPredicate().getLocalName().equals(predicate)){
				if (stmte.getObject().isLiteral()) {
					addSubjectURI(object, ":remove" + stmte.getObject().asLiteral().getLexicalForm(),
							number, type);
				}
			}
		}
	}
	
	/**
	 * Adds AML Values
	 * @param amlList
	 * @param amlValue
	 * @param aml
	 * @return
	 */
	protected ArrayList<String> addAmlValues(ArrayList<?> amlList,ArrayList<String> amlValue,String aml,
			String predicate){	
		for(int i = 0;i < amlList.size();i++){	
			StmtIterator iterator = model.listStatements();
			while (iterator.hasNext()) {
				Statement stmt = iterator.nextStatement();
				subject = stmt.getSubject();

				if(subject.asResource().getLocalName().equals(amlList.get(i))){
					String value = getValue(subject, predicate);					
					if(value != null){
						amlValue.add(aml + value);
						break;
					}
				}
			}
		}
		return amlValue;
	}
}