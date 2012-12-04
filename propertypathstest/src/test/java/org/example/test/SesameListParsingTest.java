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
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.UnsupportedRDFormatException;

/**
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 */
public class SesameListParsingTest extends AbstractSesameTest
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
    public final void testTurtleListParsing() throws RDFParseException, RepositoryException, IOException,
        RDFHandlerException, UnsupportedRDFormatException
    {
        this.getTestRepositoryConnection().add(this.getClass().getResourceAsStream("/rioParserTest1.ttl"), "",
                RDFFormat.TURTLE);
        this.getTestRepositoryConnection().commit();
        dumpContexts(RDFFormat.TURTLE);
        dumpContexts(RDFFormat.RDFXML);
        Assert.assertEquals(20, this.getTestRepositoryConnection().size());
    }
    
    @Test
    public final void testRDFXMLListParsing() throws RDFParseException, RepositoryException, IOException,
        RDFHandlerException, UnsupportedRDFormatException
    {
        this.getTestRepositoryConnection().add(this.getClass().getResourceAsStream("/rioParserTest1.rdf"), "",
                RDFFormat.RDFXML);
        this.getTestRepositoryConnection().commit();
        dumpContexts(RDFFormat.TURTLE);
        dumpContexts(RDFFormat.RDFXML);
        Assert.assertEquals(20, this.getTestRepositoryConnection().size());
    }
    
    @Test
    public final void testRDFXMLListParsingShort() throws RDFParseException, RepositoryException, IOException,
        RDFHandlerException, UnsupportedRDFormatException
    {
        this.getTestRepositoryConnection().add(this.getClass().getResourceAsStream("/rioParserTest1-short.rdf"), "",
                RDFFormat.RDFXML);
        this.getTestRepositoryConnection().commit();
        dumpContexts(RDFFormat.TURTLE);
        dumpContexts(RDFFormat.RDFXML);
        Assert.assertEquals(9, this.getTestRepositoryConnection().size());
    }
    
    @Test
    public final void testTurtleListParsingShort() throws RDFParseException, RepositoryException, IOException,
        RDFHandlerException, UnsupportedRDFormatException
    {
        this.getTestRepositoryConnection().add(this.getClass().getResourceAsStream("/rioParserTest1-short.ttl"), "",
                RDFFormat.TURTLE);
        this.getTestRepositoryConnection().commit();
        dumpContexts(RDFFormat.TURTLE);
        dumpContexts(RDFFormat.RDFXML);
        Assert.assertEquals(9, this.getTestRepositoryConnection().size());
    }
    
}
