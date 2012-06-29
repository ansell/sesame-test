/**
 * 
 */
package org.example.test;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.turtle.TurtleWriter;

/**
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 */
public class OWLPrimerParsingTest extends AbstractSesameTest
{
    
    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception
    {
        super.setUp();
    }
    
    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception
    {
        super.tearDown();
    }
    
    @Test
    public void testOriginalRDFXMLGraphSize() throws RDFParseException, RepositoryException, IOException, RDFHandlerException
    {
        this.getTestRepositoryConnection().add(this.getClass().getResourceAsStream("/primer.original.rdfxml.xml"), "",
                RDFFormat.RDFXML);
        
        Assert.assertEquals(295, this.getTestRepositoryConnection().size());
        
        //this.getTestRepositoryConnection().export(new TurtleWriter(System.out));
    }
    
    @Test
    public void testOWLAPIRDFXMLGraphSize() throws RDFParseException, RepositoryException, IOException, RDFHandlerException
    {
        this.getTestRepositoryConnection().add(this.getClass().getResourceAsStream("/primer.owlapitestresource.rdfxml.xml"), "",
                RDFFormat.RDFXML);
        
        Assert.assertEquals(294, this.getTestRepositoryConnection().size());
        
        // this.getTestRepositoryConnection().export(new TurtleWriter(System.out));
    }
    
    @Test
    public void testFromRDFXMLTurtleGraphSize() throws RDFParseException, RepositoryException, IOException, RDFHandlerException
    {
        this.getTestRepositoryConnection().add(this.getClass().getResourceAsStream("/primer.fromrdfxml.turtle.ttl"), "", RDFFormat.TURTLE);
        
        Assert.assertEquals(295, this.getTestRepositoryConnection().size());
        
        // this.getTestRepositoryConnection().export(new TurtleWriter(System.out));
    }

    @Test
    public void testOriginalTurtleGraphSize() throws RDFParseException, RepositoryException, IOException
    {
        this.getTestRepositoryConnection().add(this.getClass().getResourceAsStream("/primer.original.turtle.ttl"), "",
                RDFFormat.TURTLE);
        
        Assert.assertEquals(268, this.getTestRepositoryConnection().size());
    }
    
    @Test
    public void testOWLAPITurtleGraphSize() throws RDFParseException, RepositoryException, IOException
    {
        this.getTestRepositoryConnection().add(
                this.getClass().getResourceAsStream("/primer.owlapitestresource.turtle.rdf"), "", RDFFormat.TURTLE);
        
        Assert.assertEquals(280, this.getTestRepositoryConnection().size());
    }
}
