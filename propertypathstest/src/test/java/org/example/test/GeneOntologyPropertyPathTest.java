/**
 * 
 */
package org.example.test;

import org.junit.After;
import org.junit.Assert;
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
public class GeneOntologyPropertyPathTest extends AbstractSesameTest
{
    
    /*
     * (non-Javadoc)
     * 
     * @see org.example.test.AbstractSesameTest#setUp()
     */
    @Override
    @Before
    public void setUp() throws Exception
    {
        super.setUp();
        
        this.getTestRepositoryConnection().add(this.getClass().getResourceAsStream("/geneontologysample1.n3"),
                "http://test.example.org/", RDFFormat.N3);
        
        Assert.assertEquals(58053, this.getTestRepositoryConnection().size());
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.example.test.AbstractSesameTest#tearDown()
     */
    @Override
    @After
    public void tearDown() throws Exception
    {
        super.tearDown();
    }
    
    @Test
    public void testGeneOntologyPropertyPathIsALengthFull() throws RepositoryException, MalformedQueryException,
        QueryEvaluationException
    {
        final TupleQuery query =
                this.getTestRepositoryConnection().prepareTupleQuery(QueryLanguage.SPARQL,
                        "select * where { <http://bio2rdf.org/go:0042254> <http://bio2rdf.org/ns/go#is_a>+ ?is_a . }");
        
        final TupleQueryResult queryResult = query.evaluate();
        
        Assert.assertTrue(queryResult.hasNext());
        
        int bindingCount = 0;
        
        while(queryResult.hasNext())
        {
            final BindingSet bindingSet = queryResult.next();
            bindingCount++;
            
            for(final Binding nextBinding : bindingSet)
            {
                this.log.info("nextBinding name=" + nextBinding.getName() + " value="
                        + nextBinding.getValue().stringValue());
            }
        }
        
        Assert.assertEquals(6, bindingCount);
    }
    
}
