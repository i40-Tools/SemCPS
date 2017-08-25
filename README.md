# SemCPS: Integrating Industry 4.0 Standards in Knowledge Graphs

This project contains the PSL Models, i.e., rules to align heterogeneious Industry Standards using Probabilistic Soft logic.         
The project has now AutomationML as focus but can be extended to other standards, e.g., OPC UA

## What is Probabilistic Soft logic ?                      
Please read it here : http://psl.linqs.org/                     

## Dependencies
This tool depends on the following software

* JDK 1.8
* Hermit OWL Reasoner 
* OWL API
* Eclipse plugin for Groovy

Donwload Hermit OWL Reasoner: http://www.hermit-reasoner.com/download.html                    
Donwload OWL API: https://sourceforge.net/projects/owlapi/                                                     
Donwload Eclipse groovy plugin: https://github.com/groovy/groovy-eclipse/wiki           
Makes sure to download plugin according to your ide version.

## IDE support Running Project in Eclipse
The quick and easy way to start compiling, running and coding **SemCPS** is we provide a java project in Eclipse .

Thus, you need to install tools:
* Eclipse IDE: https://www.eclipse.org/downloads/

Donwload Eclipse groovy plugin: https://github.com/groovy/groovy-eclipse/wiki                                   

Make sure you download correct version of the plugin according to your eclipse otherwise it wont compile.                        

Import the project in eclipse and click build. The maven dependancies will be downloaded automatically.                      

Add config.ttl to the root of your project. This file configures how the experiments will be run                

Run SemCPSMain.java                   

## Install and build from the source code  
To obtain the latest version of the project please clone the github repository

    $ git clone https://github.com/i40-Tools/SemCPS.git

Make sure to add resources/ and libs/ folder to your build path.

## Running the examples
To run the PSL examples please go to Main.java

give path to groovy script : 
		script.evaluate(new File("src/main/java/pslApproach/KGAlignment.groovy"));


You can find Heterogeneity examples at :                         
https://github.com/i40-Tools/HeterogeneityExampleData                                

To run the AML examples please create a file config.ttl in the main directory of the project. An example is show below:
```@prefix aml:     <http://vocab.cs.uni-bonn.de/aml#> .
@prefix het:     <http://vocab.cs.uni-bonn.de/het#> .
@prefix owl:     <http://www.w3.org/2002/07/owl#> .
@prefix rdf:     <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
@prefix rdfs:    <http://www.w3.org/2000/01/rdf-schema#> .
@prefix schema:  <http://schema.org/> .
@prefix skos:    <http://www.w3.org/2004/02/skos/core#> .
@prefix xml:     <http://www.w3.org/XML/1998/namespace> .
@prefix xsd:     <http://www.w3.org/2001/XMLSchema#> .
@prefix uri:     <http://uri4uri.net/vocab.html/#>
@prefix aml:     <https://w3id.org/i40/aml#> .
@prefix sto:     <https://w3id.org/i40/sto#>.
@prefix ontosec: <http://www.semanticweb.org/ontologies/2008/11/OntologySecurity.owl#>


aml:conf 
     rdfs:label "General Configuration"@en ;
     uri:path "C:/HeterogeneityExampleData/AutomationML/Single-Heterogeneity/M2/Testbeds-2/";
	 uri:experimentFolder "E:/ExperimentsToKCAP/Experiment1/run -1/";
     sto:Standard "aml";
     ontosec:Training "false";     
     uri:URI "C:/Users/omar/Desktop/SemCPS-/resources/".     
```
Please note:  
```
uri:path refers to Heterogeneity path                    
uri:URI refers to the ontology path                      
```

Just give path of AML heterogenity and folders will be created automatically.                  

Then you can add GoldStandard and training data.                           

To create folders manually before running, you can create and put Goldstandard.txt and training data.           
```
.../TestBed1/PSL/test/              
.../TestBed1/PSL/train/                          
```
If you want to reproduce the results, please point your path to the generated folder in config.ttl

```@prefix aml:     <http://vocab.cs.uni-bonn.de/aml#> .
@prefix het:     <http://vocab.cs.uni-bonn.de/het#> .
@prefix owl:     <http://www.w3.org/2002/07/owl#> .
@prefix rdf:     <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
@prefix rdfs:    <http://www.w3.org/2000/01/rdf-schema#> .
@prefix schema:  <http://schema.org/> .
@prefix skos:    <http://www.w3.org/2004/02/skos/core#> .
@prefix xml:     <http://www.w3.org/XML/1998/namespace> .
@prefix xsd:     <http://www.w3.org/2001/XMLSchema#> .
@prefix uri:     <http://uri4uri.net/vocab.html/#>
@prefix aml:     <https://w3id.org/i40/aml#> .
@prefix sto:     <https://w3id.org/i40/sto#>.
@prefix ontosec: <http://www.semanticweb.org/ontologies/2008/11/OntologySecurity.owl#>


aml:conf 
     rdfs:label "General Configuration"@en ;
     uri:path "C:/HeterogeneityExampleData/AutomationML/Single-Heterogeneity/M2/Testbeds-1/";
     sto:Standard "aml";
     ontosec:Training "false";
     uri:URI "C:/Users/omar/Desktop/SemCPS-/resources/".     
```


```
If you want to get the report in one go you can run the function getReport(String root) in Alligator main.

root is the base of the examples :
e.g 

C:/HeterogeneityExampleData/AutomationML/Single-Heterogeneity/
```


## Updating Krextor Rules 
### What is Krextor?

Krextor is a an extensible XSLT-based framework for extracting RDF from XML.
Please note that the resources folder should be added to the project in order to run Krextor.

Read more at : https://github.com/EIS-Bonn/krextor

Please navigate to /resources/amlrules/aml.xsl

Here you can update, remove or add rules for RDF conversion.


## License

* Copyright (C) 2015-2017 EIS Uni-Bonn
* Licensed under the Apache License
