package edu.utdallas.hadooprdf.main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.semanticweb.owl.model.OWLOntologyCreationException;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_Literal;
import com.hp.hpl.jena.graph.Node_URI;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;
import com.hp.hpl.jena.sparql.util.NodeFactory;
import com.hp.hpl.jena.vocabulary.RDF;

import edu.utdallas.hadooprdf.main.HadoopElement.HadoopTriple;
import edu.utdallas.hadooprdf.rdf.uri.prefix.PrefixNamespaceTree;
import edu.utdallas.hadooprdf.rdf.uri.prefix.PrefixTree;



public class QueryRewriter {
	

	private static ArrayList<Integer> getIndicesOfTriplesTobeChanged (int index, ArrayList<HadoopElement.HadoopTriple> tripleList) {
		ArrayList<Integer> indexList = new ArrayList<Integer>();
		
		indexList.add(index);
		for (int i = 0; i < tripleList.size(); i++) { 
			if (tripleList.get(i).getPredicate().hashCode() != 
				tripleList.get(index).getPredicate().hashCode()){
				
				if (tripleList.get(i).getObject().hashCode() == 
					tripleList.get(index).getSubject().hashCode()) {
					indexList.add(i);
				}				
			}
		}		
		
		return indexList;		
	}
	
	private static HashMap <Integer, HadoopElement.HadoopTriple> createTripleListMap (ArrayList<Integer> indices, 
				List<HadoopElement.HadoopTriple> tripleList, PrefixNamespaceTree prefixTree, FileListGenerator filesGen) throws Exception {
		HashMap <Integer, HadoopElement.HadoopTriple> tripleListMap = new HashMap<Integer, HadoopElement.HadoopTriple>();
	
		tripleListMap.put(indices.get(0), tripleList.get(indices.get(0)));
		
		
		for (int i = 1; i < indices.size(); i++) {					
			String URI = tripleList.get(indices.get(i)).getPredicate().getURI() + 
										"_";
			URI += prefixTree.matchAndReplacePrefix(tripleList.get(indices.get(0)).getObject().toString());										
			Node predicate =  Node_Literal.createURI(URI);

			
			HadoopElement.HadoopTriple triple = new HadoopElement.HadoopTriple (tripleList.get(indices.get(i)).getSubject(), 
										predicate, tripleList.get(indices.get(i)).getObject());
			
			
			
			try { triple = convertPredicateToPrefix (triple,prefixTree);} catch (Exception e) {throw e;}
			
			String classAssociated =  tripleList.get(indices.get(i)).getPredicate().getURI();
			int index1 = classAssociated.lastIndexOf("#");
			classAssociated = classAssociated.substring (index1, classAssociated.length ());
			System.out.println (triple.getObject().toString()) ;
			ArrayList<String> files = fetchAssociatedFilesForTriple(triple.getObject().toString(), 
					classAssociated, triple.getPredicate().getURI (), filesGen);
			triple.addFiles (files);
			tripleListMap.put(indices.get(i), triple);			
		}
		
		return tripleListMap;
	}
	
	private static HashMap <Integer, HadoopElement.HadoopTriple> createTripleMap (int index, HadoopTriple triple, 
											PrefixNamespaceTree prefixTree, FileListGenerator filesGen) throws Exception {
		
		HashMap <Integer, HadoopElement.HadoopTriple> tripleMap = new HashMap<Integer, HadoopElement.HadoopTriple>();			
		String URI = triple.getPredicate().getURI() + "_";
		URI += prefixTree.matchAndReplacePrefix(triple.getObject().toString());
		String tURI = prefixTree.matchAndReplacePrefix(triple.getPredicate().getURI());
		Node predicate = Node_URI.createURI(URI);
		
		// Not required - Will not be using object since it is part of the predicate URI already.
		URI = prefixTree.matchAndReplacePrefix(triple.getObject().toString());
		Node object = Node_URI.createURI(URI);
		
	
		HadoopElement.HadoopTriple tTriple = new HadoopElement.HadoopTriple (triple.getSubject(), predicate, object);
		try { tTriple = convertPredicateToPrefix (tTriple,prefixTree);} catch (Exception e) {throw e;}	
		
		String classAssociated =  tTriple.getPredicate().getURI();
		int index1 = classAssociated.lastIndexOf("#");
		classAssociated = classAssociated.substring (index1, classAssociated.length ());
		System.out.println (triple.getObject().toString()) ;
		ArrayList<String> files = fetchAssociatedFilesForTriple(triple.getObject().toString(), classAssociated, 
				tTriple.getPredicate().getURI (), filesGen);
		tTriple.addFiles (files);
		tripleMap.put(index, tTriple);
		tTriple = null;
		return tripleMap;
	}
	
	private static boolean shouldTripleBePartofQuery (int index, ArrayList<Integer> indicesOfTripleToBeRemoved) {
		
		for (int i = 0; i < indicesOfTripleToBeRemoved.size(); i++) {
			if (index == indicesOfTripleToBeRemoved.get(i))
				return false;
			
		}
		return true;
	}
	private static HadoopElement createQueryElement (HashMap <Integer, HadoopElement.HadoopTriple>  tripleListMap, 
				ArrayList<Integer> indicesOfTripleToBeRemoved) throws Exception {
		
		ArrayList<HadoopElement.HadoopTriple> tripleList = new ArrayList<HadoopElement.HadoopTriple>();
			
		for (int i = 0; i < tripleListMap.size(); i++) {
			if (shouldTripleBePartofQuery (i, indicesOfTripleToBeRemoved) == true) {
				HadoopElement.HadoopTriple triple = (HadoopElement.HadoopTriple)tripleListMap.get(i);				
				tripleList.add(triple);
			}
		}
						
		HadoopElement element = new HadoopElement (tripleList);
		
		return element;
		
	}
	
	private static ArrayList<HadoopElement>
		rewriteBasicElement (List<HadoopElement> queryElementList, PrefixNamespaceTree prefixTree, FileListGenerator filesGen)
										throws Exception {
		
		ArrayList<HadoopElement> hElementList = new ArrayList<HadoopElement>();
		

		for (int i = 0; i < queryElementList.size(); i++) {
			HadoopElement element = queryElementList.get(i);
			
			ArrayList<HadoopElement.HadoopTriple> tripleList = null;
			
			try {
				tripleList = element.getTriple();
			} catch (NotBasicElementException e) {
				throw new UnhandledElementException (e.getMessage());
			}
			
			HashMap<Integer,HadoopElement.HadoopTriple> tripleListMap = new HashMap<Integer, HadoopElement.HadoopTriple>(); 
			ArrayList<Integer> TripleIndexToBeRemoved = new ArrayList<Integer>();

			for (int  j = 0; j < tripleList.size(); j++) 
			{
				HadoopElement.HadoopTriple triple = tripleList.get(j);				
				
				if ((triple.getPredicate().hasURI(RDF.type.getURI())) && 
						(triple.getSubject().isVariable()) && (triple.getObject().isConcrete())) {
					
					ArrayList<Integer> indices = QueryRewriter.getIndicesOfTriplesTobeChanged(j, tripleList);
										
					if (indices.size() > 1) {
						try {
							tripleListMap.putAll(QueryRewriter.createTripleListMap(indices, tripleList, prefixTree, filesGen));
						} catch (Exception e) { throw e;}
						
						// Remove the first indices from the new Query, since its existence is 
						// depicted by the rest of the indices
						TripleIndexToBeRemoved.add(indices.get(0));		
						
					} else {
						try {						
							tripleListMap.putAll(QueryRewriter.createTripleMap(j, triple, prefixTree, filesGen));
						} catch (Exception e) { throw e;}
					}					
				} else {
					String tURI = prefixTree.matchAndReplacePrefix(triple.getPredicate().getURI());
					
				
					String classAssociated =  triple.getPredicate().getURI();
					int index1 = classAssociated.lastIndexOf("#");
					classAssociated = classAssociated.substring (index1, classAssociated.length ());
					ArrayList<String> files = fetchAssociatedFilesForTriple(triple.getObject().toString(), classAssociated, 
							tURI, filesGen);
					triple.addFiles (files);
					
					HadoopElement.HadoopTriple tTriple = null;
					tTriple = tripleListMap.put(j, triple);					
					if (tTriple != null) {
						tTriple = tripleListMap.put(j, tTriple);
					}					
				}
			}
			// Add Hadoop Element to the ArrayList
			HadoopElement ele = QueryRewriter.createQueryElement(tripleListMap, TripleIndexToBeRemoved);
			hElementList.add(ele);			
 		}
				
		return hElementList;
	}
	
	private static HadoopElement.HadoopTriple convertPredicateToPrefix (HadoopElement.HadoopTriple triple, 
			 																		PrefixNamespaceTree prefixTree) throws Exception {
		HadoopElement.HadoopTriple hTriple = null;
		
		
		String prefix = prefixTree.matchAndReplacePrefix(triple.getPredicate().getURI());
		if (prefix != null) {
			Node predicate = Node_Literal.createURI(prefix);
			hTriple = new HadoopElement.HadoopTriple (triple.getSubject(), predicate, triple.getObject());			
			
		} else {
			
			throw new Exception ("Cannot execute the query, no metadata found for the prefix " + 
						triple.getPredicate().getURI());				
			//transformedTriples.add((HadoopElement.HadoopTriple)triples.get(index));
		}		
		
		return hTriple;
	}
	
	private static ArrayList<String> fetchAssociatedFilesForTriple (String uri, String classAssociated, 
										String prefix, FileListGenerator filelistgen)  {		
		return (ArrayList<String>) filelistgen.getFilesAssociatedWithTriple(uri, classAssociated, prefix);
	}
	
	public static List<HadoopElement>  rewriteQuery (edu.utdallas.hadooprdf.main.Query query, PrefixNamespaceTree prefixTree) throws 
								Exception  {		
		
		FileListGenerator fileListGenerator = null;
		try {
			fileListGenerator = new FileListGenerator (query);
		} catch (Exception e) {
			throw e;
		}
		
		ArrayList<HadoopElement> tQueryElementList = (ArrayList<HadoopElement>)query.getElements();
		try {
			tQueryElementList = rewriteBasicElement (tQueryElementList, prefixTree, fileListGenerator);			
		} catch (Exception e) { throw e;}
		
		return tQueryElementList;
	}		
}
