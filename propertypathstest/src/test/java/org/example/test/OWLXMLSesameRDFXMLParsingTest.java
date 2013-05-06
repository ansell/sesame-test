/**
 * 
 */
package org.example.test;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;

/**
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 */
public class OWLXMLSesameRDFXMLParsingTest extends AbstractSesameTest
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
    public final void testOWLXMLParsingUsingRDFXMLParser() throws RDFParseException, RepositoryException, IOException
    {
        final AtomicInteger failures = new AtomicInteger(0);
        for(int i = 0; i < 10000; i++)
        {
            try
            {
                this.getTestRepositoryConnection().add(this.getClass().getResourceAsStream("/primer.owlxml.xml"), "",
                        RDFFormat.RDFXML);
                this.getTestRepositoryConnection().commit();
                
                // If the parse succeeds for any reason verify that there are not any generated
                // triples in the repository
                Assert.assertEquals(0, this.getTestRepositoryConnection().size());
                
                this.getTestRepositoryConnection().clear();
            }
            catch(final RDFParseException rdfpe)
            {
                failures.incrementAndGet();
            }
        }
        
        Assert.assertEquals(10000, failures.get());
    }
    
}
