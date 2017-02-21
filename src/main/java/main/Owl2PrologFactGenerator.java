package main;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.net.URI;
import java.util.Set;

import org.semanticweb.HermiT.Reasoner;
//import org.apache.commons.lang3.StringUtils;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLNamedObject;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLProperty;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import util.StringUtil;

/**
 * 
 * Generates the Datalog facts from a given Ontology
 */
public class Owl2PrologFactGenerator {

	private OWLOntology ont;
	private OWLReasoner reasoner;

	/**
	 * Read the ontology given a local path
	 * 
	 * @throws OWLOntologyCreationException
	 */
	public void readOntology(String localIRI) throws OWLOntologyCreationException {
		String prefix = "file:";
		// windows path fixing
		if (!localIRI.startsWith("/"))
			prefix += "/";
		URI basePhysicalURI = URI.create(prefix + localIRI.replace("\\", "/"));
		OWLOntologyManager baseM = OWLManager.createOWLOntologyManager();
		this.ont = baseM.loadOntologyFromOntologyDocument(IRI.create(basePhysicalURI));
		reasoner = new Reasoner.ReasonerFactory().createReasoner(this.ont);
	}

	public void generateIntentionalDB(String outputPrologFilename) throws Exception {
		PrintWriter prologWriter = new PrintWriter(new FileWriter(outputPrologFilename), true);
		prologWriter.println(factsFromProperties());
		prologWriter.println(factsFromTBox());
		prologWriter.flush();
		prologWriter.close();
	}

	@SuppressWarnings("deprecation")
	public void generateABoxFacts(String AboxFilePath) throws Exception {
		PrintWriter prologWriter = new PrintWriter(new FileWriter(AboxFilePath), true);
		for (OWLClass cls : ont.getClassesInSignature()) {
			prologWriter.println(factsFromABox(cls));
		}
		prologWriter.flush();
		prologWriter.close();
	}

	/**
	 * Write properties on top of the file
	 * 
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public String factsFromProperties() {
		StringBuilder buf = new StringBuilder();

		for (OWLProperty objProp : ont.getObjectPropertiesInSignature()) {
			buf.append(":-dynamic(" + StringUtil.lowerCaseFirstChar(objProp.getIRI().getFragment()) + "/2).")
					.append(System.getProperty("line.separator"));
		}

		for (OWLProperty dataProp : ont.getDataPropertiesInSignature()) {
			if (dataProp.getIRI().getFragment() != null) {
				buf.append(":-dynamic(" + StringUtil.lowerCaseFirstChar(dataProp.getIRI().getFragment()) + "/2).")
						.append(System.getProperty("line.separator"));
			}
		}

		return buf.toString();
	}

	/**
	 * Generate facts from the TBOX. Classes, objects and datatype properties
	 * 
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public String factsFromTBox() {
		StringBuilder buf = new StringBuilder();

		for (OWLClass cls : ont.getClassesInSignature()) {
			buf.append("clause1(type(").append(StringUtil.lowerCaseFirstChar(cls.getIRI().getFragment())).append(",")
					.append("class),true).");
			buf.append(System.getProperty("line.separator"));
		}

		for (OWLProperty objProp : ont.getObjectPropertiesInSignature()) {
			buf.append("clause1(type(").append(StringUtil.lowerCaseFirstChar(objProp.getIRI().getFragment()))
					.append(",").append("objectproperty),true).");
			buf.append(System.getProperty("line.separator"));
		}

		for (OWLProperty datatypeProp : ont.getDataPropertiesInSignature()) {
			buf.append("clause1(type(");
			if (datatypeProp.getIRI().getFragment() != null) {
				buf.append(StringUtil.lowerCaseFirstChar(datatypeProp.getIRI().getFragment())).append(",")
						.append("datatypeproperty),true).");
				buf.append(System.getProperty("line.separator"));
			}
		}

		return buf.toString();
	}

	/**
	 * Gets the Individuals of all the classes in the ontology and generates the
	 * corresponding facts
	 * 
	 * @return A buffer of Strings
	 */
	@SuppressWarnings("deprecation")
	public String factsFromABox(OWLClass cls) {
		StringBuilder buf = new StringBuilder();
		NodeSet<OWLNamedIndividual> instances = reasoner.getInstances(cls, false);
		for (OWLNamedIndividual ind : instances.getFlattened()) {
			buf.append("clause1(type(").append(StringUtil.lowerCaseFirstChar(ind.getIRI().getFragment())).append(",")
					.append(StringUtil.lowerCaseFirstChar(cls.getIRI().getFragment())).append("),true).");
			buf.append(System.getProperty("line.separator"));

			// look up all object property assertions
			for (OWLObjectProperty op : ont.getObjectPropertiesInSignature()) {
				NodeSet<OWLNamedIndividual> objPropSet = reasoner.getObjectPropertyValues(ind, op);
				for (OWLNamedIndividual value : objPropSet.getFlattened()) {
					// System.out.println(ind.getIRI().getFragment() + " " +
					// op.getIRI().getFragment() + " " +
					// value.getIRI().getFragment());
					buf.append("clause1(" + op.getIRI().getFragment() + "(")
							.append(StringUtil.lowerCaseFirstChar(ind.getIRI().getFragment())).append(",")
							.append(StringUtil.lowerCaseFirstChar(value.getIRI().getFragment())).append("),true).");
					buf.append(System.getProperty("line.separator"));
				}
			}

			// look up all datatype property assertions
			for (OWLDataProperty dp : ont.getDataPropertiesInSignature()) {
				Set<OWLLiteral> dataPropSet = reasoner.getDataPropertyValues(ind, dp);
				for (OWLLiteral value : dataPropSet) {
					buf.append("clause1(" + dp.getIRI().getFragment() + "(")
							.append(ind.getIRI().getFragment().toLowerCase()).append(",");
					if (value.hasLang()) {
						// @todo Removing the @en from the last part of the
						// string. This should be a different way to do it
						String removeAdd = value.toString().substring(0, value.toString().length() - 3);
						buf.append(removeAdd);
					} else {
						buf.append(value);
					}
					buf.append("),true).").append(System.getProperty("line.separator"));
				}
			}
		}

		return buf.toString();
	}

	/**
	 * Get all the domain and range axioms of the ontology
	 * 
	 * @todo
	 */
	@SuppressWarnings("deprecation")
	public void getDomainRangeAxioms() {
		Set<OWLDataPropertyDomainAxiom> dataPropDomain = ont.getAxioms(AxiomType.DATA_PROPERTY_DOMAIN);
		for (OWLDataPropertyDomainAxiom owlDataPropertyDomainAxiom : dataPropDomain) {
			System.out.println(((OWLNamedObject) owlDataPropertyDomainAxiom.getDomain()).getIRI().getFragment() + " "
					+ ((OWLNamedObject) owlDataPropertyDomainAxiom.getProperty()).getIRI().getFragment());
		}

		Set<OWLDataPropertyRangeAxiom> dataPropRange = ont.getAxioms(AxiomType.DATA_PROPERTY_RANGE);
		for (OWLDataPropertyRangeAxiom dataPropRangeAxiom : dataPropRange) {
			System.out.println(((OWLNamedObject) dataPropRangeAxiom.getRange()) + " "
					+ ((OWLNamedObject) dataPropRangeAxiom.getProperty()).getIRI().getFragment());
		}

		Set<OWLObjectPropertyDomainAxiom> objPropDomain = ont.getAxioms(AxiomType.OBJECT_PROPERTY_DOMAIN);
		for (OWLObjectPropertyDomainAxiom owlObjPropDomainAxiom : objPropDomain) {
			System.out.println(((OWLNamedObject) owlObjPropDomainAxiom.getDomain()).getIRI().getFragment() + " "
					+ ((OWLNamedObject) owlObjPropDomainAxiom.getProperty()).getIRI().getFragment());
		}

		Set<OWLObjectPropertyRangeAxiom> objPropRange = ont.getAxioms(AxiomType.OBJECT_PROPERTY_RANGE);
		for (OWLObjectPropertyRangeAxiom ObjPropRangeAxiom : objPropRange) {
			System.out.println(((OWLNamedObject) ObjPropRangeAxiom.getRange()).getIRI().getFragment() + " "
					+ ((OWLNamedObject) ObjPropRangeAxiom.getProperty()).getIRI().getFragment());
		}
	}

	public OWLOntology getOnt() {
		return ont;
	}

	public void setOnt(OWLOntology ont) {
		this.ont = ont;
	}

}
