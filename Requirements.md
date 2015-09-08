#This page will list the requirements of the project

# Introduction #

The HadoopRDF project has to be able to handle any RDF data set and SPARQL query.


# Details #

Here are the requirements known to me so far:
  * Import data files to Hadoop
    * The serialization format may be RDF/XML, N3, Turtle, N-Triples. We need to convert the data to N-Triples if it is not already in that format.
  * Identify prefixes and modify data to use them.
  * Split data according to Predicates.
  * Split further according to the type of Object in a triple.
  * Parse SPARQL query and convert to an internal representation.
  * Write generic MapReduce job to handle any query plan.