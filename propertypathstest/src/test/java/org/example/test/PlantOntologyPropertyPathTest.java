/**
 * 
 */
package org.example.test;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;

/**
 * @author Peter Ansell p_ansell@yahoo.com
 *
 */
public class PlantOntologyPropertyPathTest extends AbstractSesameTest
{
    
    /* (non-Javadoc)
     * @see org.example.test.AbstractSesameTest#setUp()
     */
    @Before
    public void setUp() throws Exception
    {
        super.setUp();

        this.getTestRepositoryConnection().add(this.getClass().getResourceAsStream("/plant_ontology-v16.owl"), "http://test.example.org/", RDFFormat.RDFXML);
        
        this.getTestRepositoryConnection().commit();
        
        Assert.assertEquals(44284, this.getTestRepositoryConnection().size());
    }
    
    /* (non-Javadoc)
     * @see org.example.test.AbstractSesameTest#tearDown()
     */
    @After
    public void tearDown() throws Exception
    {
        super.tearDown();
    }
    
    @Test
    public void testGeneOntologyPropertyPathIsALengthNoReasoning() throws RepositoryException, MalformedQueryException, QueryEvaluationException
    {
        TupleQuery query = this.getTestRepositoryConnection().prepareTupleQuery(QueryLanguage.SPARQL, "select * where { <http://bio2rdf.org/go:0042254> <http://bio2rdf.org/ns/go#is_a>+ ?is_a . }");
        
        TupleQueryResult queryResult = query.evaluate();
        
        AtomicInteger bindingCount = new AtomicInteger(0);
        
        try
        {
            Assert.assertTrue(queryResult.hasNext());
            
            while(queryResult.hasNext())
            {
                BindingSet bindingSet = queryResult.next();
                bindingCount.incrementAndGet();
                
                for(Binding nextBinding : bindingSet)
                {
                    log.info("nextBinding name="+nextBinding.getName()+" value="+nextBinding.getValue().stringValue());
                }
            }
        }
        finally
        {
            queryResult.close();
        }
        
        Assert.assertEquals(6, bindingCount.get());
    }
    
}
