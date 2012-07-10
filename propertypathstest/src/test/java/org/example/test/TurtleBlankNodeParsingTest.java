/**
 * 
 */
package org.example.test;

import java.io.IOException;

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
public class TurtleBlankNodeParsingTest extends AbstractSesameTest
{
    
    /* (non-Javadoc)
     * @see org.example.test.AbstractSesameTest#setUp()
     */
    @Before
    public void setUp() throws Exception
    {
        super.setUp();
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
    public final void testTurtleBlankNodeParsing() throws RDFParseException, RepositoryException, IOException
    {
        this.getTestRepositoryConnection().add(this.getClass().getResourceAsStream("/testBlankNodes.ttl"), "",
                RDFFormat.TURTLE);
        
        Assert.assertEquals(1, this.getTestRepositoryConnection().size());
        
    }
    
}
