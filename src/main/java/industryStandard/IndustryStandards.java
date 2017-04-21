package industryStandard;

import java.util.Set;

import com.hp.hpl.jena.rdf.model.RDFNode;

/**
 * common methods come here needs to be updated later currently working in ID
 * issue
 * 
 * @author omar
 *
 */
public class IndustryStandards {

	/**
	 * add subjects with appropriate ontology
	 * 
	 * @param subject
	 * @param subjects
	 * @TODO don't understand the comment
	 */

	protected void addSubjectURI(RDFNode subject, Set<String> subjects, String predicate, int number) {
		if (subject.asNode().getNameSpace().contains("aml")) {
			if (!predicate.contains("remove")) {
				subjects.add(
						"aml" + number + ":" + subject.asNode().getLocalName() + "\t" + "aml" + number + predicate);
			} else {
				subjects.add("aml" + number + ":" + subject.asNode().getLocalName() + "\t" + "aml" + predicate);
			}
		}

		else if (subject.asNode().getNameSpace().contains("opcua")) {
			subjects.add("opcua:" + subject.asNode().getLocalName() + "\t" + "opcua" + predicate);
		}
	}

	void addSubjectURI(RDFNode subject, Set<String> subjects, String predicate) {
		if (subject.asNode().getNameSpace().contains("aml")) {
			subjects.add("aml:" + subject.asNode().getLocalName() + "\t" + "aml" + predicate);
		}

		else if (subject.asNode().getNameSpace().contains("opcua")) {
			subjects.add("opcua:" + subject.asNode().getLocalName() + "\t" + "opcua" + predicate);
		}
	}

}
