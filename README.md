# Alligator: A Deductive Approach for the Integration of Industry 4.0 Standards

This project contains the rules to integrate two AML files using a cannonical model and rules written in ProLog - **Rules4AMLIntegrator**.

## Dependencies
This tool depends on the following software

* Prolog 7.2.3
* JDK 1.8
* Prolog Connector 3.1.2
* Hermit OWL Reasoner 
* OWL API

Download Prolog: http://www.swi-prolog.org/download/stable
Donwload Prolog Connector: https://sewiki.iai.uni-bonn.de/research/pdt/connector/library
Donwload Hermit OWL Reasoner: http://www.hermit-reasoner.com/download.html
Donwload OWL API: https://sourceforge.net/projects/owlapi/

## IDE support 
The quick and easy way to start compiling, running and coding **Rules4AMLIntegrator** we provide a java project in Eclipse and we a Prolog Connector. Thus, you need to install tools:
* Eclipse IDE: https://www.eclipse.org/downloads/

In windows you need to add the following entries system PATH

    $ C:\Program Files\swipl\bin; C:\Program Files\swipl\lib\jpl.jar; C:\PrologConnectorJarFolder\org.cs3.prolog.connector_3.1.2.201504300958.jar;

## Install and build from the source code  
To obtain the latest version of the project please clone the github repository

    $ git clone https://github.com/i40-Tools/Alligator.git

Make sure to add resources/ and libs/ folder to your build path.

## Running the examples
To run the examples please create a file config.ttl in the main directory of the project. An example is show below:
```
@prefix aml:     <https://w3id.org/i40/aml#> .
@prefix owl:     <http://www.w3.org/2002/07/owl#> .
@prefix rdf:     <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
@prefix rdfs:    <http://www.w3.org/2000/01/rdf-schema#> .
@prefix schema:  <http://schema.org/> .
@prefix skos:    <http://www.w3.org/2004/02/skos/core#> .
@prefix xml:     <http://www.w3.org/XML/1998/namespace> .
@prefix xsd:     <http://www.w3.org/2001/XMLSchema#> .
@prefix uri:     <http://uri4uri.net/vocab.html/#>

aml:conf 
     rdfs:label "General Configuration"@en ;
     uri:path "C:/HeterogeneityExampleData/AutomationML/M2-Granularity/Testbeds-1/";
     uri:URI "C:/Users/omar/Desktop/Alligator-master/resources/aml.ttl".
```

## Updating Krextor Rules 
### What is Krextor?

Krextor is a an extensible XSLT-based framework for extracting RDF from XML.                 

Read more at : https://github.com/EIS-Bonn/krextor

Please navigate to /resources/amlrules/aml.xsl

Here you can update, remove or add rules for RDF conversion.


## License

* Copyright (C) 2015-2016 EIS Uni-Bonn
* Licensed under the Apache License
