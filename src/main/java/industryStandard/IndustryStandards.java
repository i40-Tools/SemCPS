package industryStandard;

import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import com.hp.hpl.jena.rdf.model.RDFNode;

/**
 * This class contains common methods to produce the conversion of Industry 4.0 standards 
 * information models to a canonical model, i.e., RDF
 * @author omar
 */

public class IndustryStandards {

	public Map<String, Collection<String>> generic = new HashMap<String, Collection<String>>();

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
}
