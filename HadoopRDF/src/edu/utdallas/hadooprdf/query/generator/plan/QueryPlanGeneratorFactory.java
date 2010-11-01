package edu.utdallas.hadooprdf.query.generator.plan;

import edu.utdallas.hadooprdf.data.metadata.DataSet;
import edu.utdallas.hadooprdf.data.metadata.StringIdPairsException;
import edu.utdallas.hadooprdf.query.generator.plan.impl.SimpleQueryPlanGeneratorImpl;

/**
 * A factory class to generate different query plan generators
 * @author vaibhav
 *
 */
public class QueryPlanGeneratorFactory 
{
	/** A private constructor **/
	private QueryPlanGeneratorFactory() { }
	
	/**
	 * Factory method that returns a QueryPlanGenerator based on the elimination count algorithm
	 * @return a QueryPlanGenerator object that is based on the elimination count algorithm
	 * @throws StringIdPairsException 
	 * @throws InterruptedException 
	 */
	public static QueryPlanGenerator createSimpleQueryPlanGenerator( DataSet dataset ) throws StringIdPairsException, InterruptedException
	{
		return new SimpleQueryPlanGeneratorImpl( dataset );
	}
}