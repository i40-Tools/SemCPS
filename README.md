# Alligator: A Deductive Approach for the Integration of Industry 4.0 Standards

This project contains the rules to integrate two AML files using a cannonical model and rules written in ProLog - **Rules4AMLIntegrator**.

## Dependencies
This tool depends on the following software

* Prolog 7.2.3
* JDK 1.8
* Prolog Connector 3.1.2
* Hermit OWL Reasoner 
* OWL API
* Eclipse plugin for Groovy

Download Prolog: http://www.swi-prolog.org/download/stable                                  
Donwload Prolog Connector: https://sewiki.iai.uni-bonn.de/research/pdt/connector/library                 
Donwload Hermit OWL Reasoner: http://www.hermit-reasoner.com/download.html                    
Donwload OWL API: https://sourceforge.net/projects/owlapi/                                                     
Donwload Eclipse groovy plugin: https://github.com/groovy/groovy-eclipse/wiki           
Makes sure to download plugin according to your ide version.

## IDE support Running Project in Eclipse
The quick and easy way to start compiling, running and coding **Rules4AMLIntegrator** we provide a java project in Eclipse and we a Prolog Connector. Thus, you need to install tools:
* Eclipse IDE: https://www.eclipse.org/downloads/

In windows you need to add the following entries system PATH

    $ C:\Program Files\swipl\bin; C:\Program Files\swipl\lib\jpl.jar; C:\PrologConnectorJarFolder\org.cs3.prolog.connector_3.1.2.201504300958.jar;

Donwload Eclipse groovy plugin: https://github.com/groovy/groovy-eclipse/wiki                                   
Make sure you download correct version of plugin according to your eclipse otherwise it wont compile.                        

Import the project in eclipse and click build. The maven dependancies will be downloaded automatically.                      

Add config.ttl                 

Run alligatorMain.java                   

## Install and build from the source code  
To obtain the latest version of the project please clone the github repository

    $ git clone https://github.com/i40-Tools/Alligator.git

Make sure to add resources/ and libs/ folder to your build path.

## Running the examples
To run the PSL examples please go AlligatorMain.java

give path to groovy script :

		script.evaluate(new File("src/main/java/edu/umd/cs/example/OntologyAlignment.groovy"));


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
@prefix sto:     <https://w3id.org/i40/sto#>.
@prefix ontosec: <http://www.semanticweb.org/ontologies/2008/11/OntologySecurity.owl#>


aml:conf 
     rdfs:label "General Configuration"@en ;
     uri:path "C:/HeterogeneityExampleData/AutomationML/Single-Heterogeneity/M2/Testbeds-2/";
     sto:Standard "aml";
     ontosec:Training "false";
     uri:URI "C:/Users/omar/Desktop/Alligator-master/resources/aml.ttl".
     
```

Just give path of AML heterogenity and folders will be created automatically.                  
Then you can add GoldStandard and training data.                           
To create folders manually before running, you can create and put Goldstandard.txt and training data.            
.../TestBed1/PSL/test/              
.../TestBed1/PSL/train/                          


## Updating Krextor Rules 
### What is Krextor?

Krextor is a an extensible XSLT-based framework for extracting RDF from XML.                 

Read more at : https://github.com/EIS-Bonn/krextor

Please navigate to /resources/amlrules/aml.xsl

Here you can update, remove or add rules for RDF conversion.


## License

* Copyright (C) 2015-2016 EIS Uni-Bonn
* Licensed under the Apache License
