/*
 * This file is part of the PSL software.
 * Copyright 2011-2015 University of Maryland
 * Copyright 2013-2015 The Regents of the University of California
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.umd.cs.example;

import java.text.DecimalFormat;

import edu.umd.cs.psl.application.inference.MPEInference;
import edu.umd.cs.psl.application.learning.weight.maxlikelihood.MaxLikelihoodMPE;
import edu.umd.cs.psl.config.*;
import edu.umd.cs.psl.database.DataStore;
import edu.umd.cs.psl.database.Database;
import edu.umd.cs.psl.database.Partition;
import edu.umd.cs.psl.database.rdbms.RDBMSDataStore;
import edu.umd.cs.psl.database.rdbms.driver.H2DatabaseDriver;
import edu.umd.cs.psl.database.rdbms.driver.H2DatabaseDriver.Type;
import edu.umd.cs.psl.groovy.*;
import edu.umd.cs.psl.model.argument.ArgumentType;
import edu.umd.cs.psl.model.argument.GroundTerm;
import edu.umd.cs.psl.model.argument.type.*;
import edu.umd.cs.psl.model.atom.GroundAtom;
import edu.umd.cs.psl.model.atom.RandomVariableAtom
import edu.umd.cs.psl.model.predicate.Predicate;
import edu.umd.cs.psl.model.predicate.type.*;
import edu.umd.cs.psl.ui.functions.textsimilarity.*;
import edu.umd.cs.psl.ui.loading.InserterUtils;
import edu.umd.cs.psl.util.database.Queries;

////////////////////////// initial setup ////////////////////////
ConfigManager cm = ConfigManager.getManager()
ConfigBundle config = cm.getBundle("ontology-alignment")

def defaultPath = System.getProperty("java.io.tmpdir")
String dbpath = config.getString("dbpath", defaultPath + File.separator + "ontology-alignment")
DataStore data = new RDBMSDataStore(new H2DatabaseDriver(Type.Disk, dbpath, true), config)
PSLModel m = new PSLModel(this, data);

////////////////////////// predicate declaration ////////////////////////

m.add predicate: "name"        , types: [
	ArgumentType.UniqueID,
	ArgumentType.UniqueID
]

m.add predicate: "fromOntology", types: [
	ArgumentType.UniqueID,
	ArgumentType.UniqueID
]


m.add predicate: "Attribute"     , types: [
	ArgumentType.UniqueID,
	ArgumentType.UniqueID
]

m.add predicate: "InternalElement"     , types: [
	ArgumentType.UniqueID,
	ArgumentType.UniqueID
]


m.add predicate: "hasRefSemantic"     , types: [
	ArgumentType.UniqueID,
	ArgumentType.String
]

m.add predicate: "hasID"     , types: [
	ArgumentType.UniqueID,
	ArgumentType.String
]

m.add predicate: "hasInternalLink"     , types: [
	ArgumentType.UniqueID,
	ArgumentType.String
]

m.add predicate: "hasEClassVersion"     , types: [
	ArgumentType.UniqueID,
	ArgumentType.String
]

m.add predicate: "hasEClassClassificationClass"     , types: [
	ArgumentType.UniqueID,
	ArgumentType.String
]
m.add predicate: "hasEClassIRDI"     , types: [
	ArgumentType.UniqueID,
	ArgumentType.String
]

//target predicate
m.add predicate: "similar"     , types: [
	ArgumentType.UniqueID,
	ArgumentType.UniqueID
]

m.add predicate: "similarType"     , types: [
	ArgumentType.UniqueID,
	ArgumentType.UniqueID
]


m.add function: "similarValue"  , implementation: new MyStringSimilarity();

// if u want 0 or 1 result use this
//m.add function: "similarValue"  , implementation: new MyStringSimilarity();


///////////////////////////// rules ////////////////////////////////////

/* (O1-O2) means that O1 and O2 are not equal */



//// Refsemantic is equal if its value is equal
//
//m.add rule : (hasRefSemantic(X,Z) & hasRefSemantic(Y,W) & similarValue(Z,W) & fromOntology(X,O1) & fromOntology(Y,O2) & (O1-O2)) >> similar(X,Y), weight : 1000;
//
//// ID is equal if its value is equal
//
//m.add rule : (hasID(X,Z) & hasID(Y,W) & similarValue(Z,W) & fromOntology(X,O1) & fromOntology(Y,O2) & (O1-O2)) >> similar(X,Y), weight : 1000;


// Attribute is same if its RefSemantic is Same
m.add rule : (Attribute(A,X) & Attribute(B,Y)  & hasRefSemantic(X,Z) & hasRefSemantic(Y,W) & similarValue(Z,W)
& fromOntology(A,O1) & fromOntology(B,O2) & (O1-O2)) >> similar(A,B) , weight : 1000;

// Attribute is same if its ID is Same
m.add rule : (Attribute(A,X) & Attribute(B,Y)  & hasID(X,Z) & hasID(Y,W) & similarValue(Z,W)
& fromOntology(A,O1) & fromOntology(B,O2) & (O1-O2)) >> similar(A,B) , weight : 1000;



// Attribute is same if its eclass,IRDI and classifcation class is Same
m.add rule :( Attribute(E,X) & Attribute(U,Y)  & hasEClassIRDI(X,Z) & hasEClassIRDI(Y,W) & similarValue(Z,W)
& Attribute(E,Q) & Attribute(U,T)  & hasEClassVersion(Q,M) & hasEClassVersion(T,N) & similarValue(M,N)
& Attribute(E,D) & Attribute(U,K)  & hasEClassVersion(D,O) & hasEClassVersion(K,L) & similarValue(O,L)
& fromOntology(E,O1) & fromOntology(U,O2) & (O1-O2)) >> similar(E,U) , weight : 1000;


// InternalElement is same if its InternalLink is Same
m.add rule : (InternalElement(A,X) & InternalElement(B,Y)  & hasInternalLink(X,Z) & hasInternalLink(Y,W) & similarValue(Z,W)
& fromOntology(A,O1) & fromOntology(B,O2) & (O1-O2)) >> similar(A,B) , weight : 1000;



/*
 //// Attribute is same if it has ID or RefSemantic Same
 //m.add rule : (Attribute(A,X) & Attribute(B,Y)  & hasValue(X,Z) & hasValue(Y,W) & similarValue(Z,W)
 //& fromOntology(A,O1) & fromOntology(B,O2) & (O1-O2)) >> similar(A,B) , weight : 1000;
 // Attribute is equal if its Refsemantic is equal
 m.add rule : ( name(A,X) & name(B,Y)
 & fromOntology(A,O1) & fromOntology(B,O2) & (O1-O2)) >> similar(A,B), weight : 1;
 m.add rule : ( name(A,X) & name(B,Y) & similarName(X,Y) & hasType(A,T) & hasType(B,T)
 & fromOntology(A,O1) & fromOntology(B,O2) & (O1-O2)) >> similar(A,B), weight : 8;
 m.add rule : ( similar(A,B) & name(A,X) & name(B,Y)
 & fromOntology(A,O1) & fromOntology(B,O2) & (O1-O2)) >> similarName(X,Y), weight : 1;
 m.add rule : (domainOf(R,A) & domainOf(T,B) & similar(A,B)
 & fromOntology(A,O1) & fromOntology(B,O2) & (O1-O2)) >> similar(R,T), weight : 2;
 m.add rule : (rangeOf(R,A)  & rangeOf(T,B)  & similar(A,B)
 & fromOntology(A,O1) & fromOntology(B,O2) & (O1-O2)) >> similar(R,T), weight : 2;
 m.add rule : (domainOf(R,A) & domainOf(T,B) & similar(R,T)
 & fromOntology(A,O1) & fromOntology(B,O2) & (O1-O2)) >> similar(A,B), weight : 2;
 m.add rule : (rangeOf(R,A)  & rangeOf(T,B)  & similar(R,T)
 & fromOntology(A,O1) & fromOntology(B,O2) & (O1-O2)) >> similar(A,B), weight : 2;
 GroundTerm classID = data.getUniqueID("class");
 m.add rule : (similar(A,B) & hasType(A, classID) & hasType(B, classID)
 & subclass(A, S1) & subclass(B, S2)
 & fromOntology(A,O1) & fromOntology(B,O2) & (O1-O2)) >> similar(S1, S2), weight: 3;
 */


GroundTerm classID = data.getUniqueID("class");

// constraints
m.add PredicateConstraint.PartialFunctional , on : similar;
m.add PredicateConstraint.PartialInverseFunctional , on : similar;
m.add PredicateConstraint.Symmetric , on : similar;

// prior
m.add rule : ~similar(A,B), weight: 1;

//////////////////////////// data setup ///////////////////////////

/* Loads data */
def dir = 'data'+java.io.File.separator+'ontology'+java.io.File.separator;
def trainDir = dir+'train'+java.io.File.separator;

Partition trainObservations = new Partition(0);
Partition trainPredictions = new Partition(1);
Partition truth = new Partition(2);

for (Predicate p : [
	fromOntology,
	name,
	Attribute,
	hasRefSemantic,
	hasID,
	hasInternalLink,
	hasEClassVersion,
	hasEClassClassificationClass,
	hasEClassIRDI,
	InternalElement
])
{
	insert = data.getInserter(p, trainObservations)
	InserterUtils.loadDelimitedData(insert, trainDir+p.getName().toLowerCase()+".txt");
}

insert = data.getInserter(similar, truth)
InserterUtils.loadDelimitedDataTruth(insert, trainDir+"similar.txt");

Database trainDB = data.getDatabase(trainPredictions, [
	name,
	fromOntology,
	Attribute,
	hasRefSemantic,
	hasInternalLink,
	hasID,
	hasEClassVersion,
	hasEClassClassificationClass,
	hasEClassIRDI,
	InternalElement] as Set, trainObservations);

populateSimilar(trainDB);

Database truthDB = data.getDatabase(truth, [similar] as Set);

//////////////////////////// weight learning ///////////////////////////
println "LEARNING WEIGHTS...";

MaxLikelihoodMPE weightLearning = new MaxLikelihoodMPE(m, trainDB, truthDB, config);
weightLearning.learn();
weightLearning.close();

println "LEARNING WEIGHTS DONE";

println m

/////////////////////////// test setup //////////////////////////////////

def testDir = dir+'test'+java.io.File.separator;
Partition testObservations = new Partition(3);
Partition testPredictions = new Partition(4);
for (Predicate p : [
	fromOntology,
	name,
	Attribute,
	hasRefSemantic,
	hasID,
	hasInternalLink,
	hasEClassVersion,
	hasEClassClassificationClass,
	hasEClassIRDI,
	InternalElement
])
{
	insert = data.getInserter(p, testObservations);
	InserterUtils.loadDelimitedData(insert, testDir+p.getName().toLowerCase()+".txt");

}

Database testDB = data.getDatabase(testPredictions, [
	name,
	fromOntology,
	Attribute,
	hasRefSemantic,
	hasID,
	hasInternalLink,
	hasEClassVersion,
	hasEClassClassificationClass,
	hasEClassIRDI,
	InternalElement] as Set, testObservations);

populateSimilar(testDB);

/////////////////////////// test inference //////////////////////////////////
println "INFERRING...";

MPEInference inference = new MPEInference(m, testDB, config);
inference.mpeInference();
inference.close();

println "INFERENCE DONE";
def file1 = new File('data/ontology/test/similar.txt')
file1.write('')
DecimalFormat formatter = new DecimalFormat("#.##");
for (GroundAtom atom : Queries.getAllAtoms(testDB, similar)){
	println atom.toString() + ": " + formatter.format(atom.getValue());
	// only writes if its equal to 1 or u can set the threshold
	if(formatter.format(atom.getValue())>="0.5"){
		println 'matches threshold writing to similar.txt'

		file1.append('\n'+atom.toString())
	}

}


/**
 * Populates all the similiar atoms between the concepts of two ontologies using
 * the fromOntology predicate.
 * 
 * @param db  The database to populate. It should contain the fromOntology atoms
 */
void populateSimilar(Database db) {
	/* Collects the ontology concepts */
	Set<GroundAtom> concepts = Queries.getAllAtoms(db, fromOntology);
	Set<GroundTerm> o1 = new HashSet<GroundTerm>();
	Set<GroundTerm> o2 = new HashSet<GroundTerm>();
	for (GroundAtom atom : concepts) {
		if (atom.getArguments()[1].toString().equals("aml"))
			o1.add(atom.getArguments()[0]);
		else
			o2.add(atom.getArguments()[0]);
	}

	/* Populates manually (as opposed to using DatabasePopulator) */
	for (GroundTerm o1Concept : o1) {
		for (GroundTerm o2Concept : o2) {
			((RandomVariableAtom) db.getAtom(similar, o1Concept, o2Concept)).commitToDB();
			((RandomVariableAtom) db.getAtom(similar, o2Concept, o1Concept)).commitToDB();
		}
	}

}

