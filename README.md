# Combining anatomy and k-anonymity for the anonymization of RDF graphs

## Overview

This project's purpose is two-fold:

1. The anonymization of knowledge graphs via the combined application of 
anatomy and k-anonymity techniques
2. Evaluation of data utility in said 
graph after anonymization with regard to counting queries: average relative 
error and absolute mean error



## Execution

A .jar file is provided for an easier execution. 

A number of options are provided, these can be separated into different 
categories depending on the part of the program to which they apply:

**Loading data** 
 
> -  -d [directory1] [directory2] ...  :   Loads the files contained in the
        directories into the model.
> - -f  [file1] [file2] …   :   Loads the files into the model.
> - -tdb [dataset]    :    Loads a Jena TDB dataset into the model.
  
**Anatomy**

> - -pt  [predicate1] [predicate2] …  : Provide the predicates which link an entity to a sensitive attribute which is part of an ontology.
> - -p   [predicate1] [predicate2] …  : Provide the predicates which link an entity to a sensitive attribute which isn't part of an ontology.

**K-anonymity**

> - -num_pred [predicate1] [predicate2] …  :  Provide the predicates which link an entity to a numerical value.
> - -lit_pred [predicate1] [predicate2] …   : Provide the predicates which link an entity to a string value.
> - -k [N]   :   Provide *k* value for anonymization.
> - -hierarchies [hierarchy1] [hierarchy2] …  :  Provide paths to csv files containing the generalization hierarchies for the QIDs.
> - -produce_csv    :     Produce a csv file containing the entities' ids and their QID values. This file is to be used by the ARX library in order to anonymize the dataset. This option must be present when first anonymizing the graph. It is not needed afterwards.
> - -csv_path   [dataset.csv]    :   Provide path to the csv file used by ARX for the anonymization.
> - -group     :     Apply the by-group algorithm (the global version is executed by default).

**Evaluation**

> - -zipcode [N]  :  Sets the generalization level for the zipcodes (the size of the prefix) used when creating counting queries during the evaluation