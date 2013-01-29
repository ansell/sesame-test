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
    public final void testTurtleBlankNodeParsing() throws RDFParseException, RepositoryException, IOException
    {
        this.getTestRepositoryConnection().add(this.getClass().getResourceAsStream("/testBlankNodes.ttl"), "",
                RDFFormat.TURTLE);
        this.getTestRepositoryConnection().commit();
        Assert.assertEquals(9, this.getTestRepositoryConnection().size());
        
    }
    
    @Test
    public final void testTurtleBlankNodeParsing10() throws RDFParseException, RepositoryException, IOException
    {
        this.getTestRepositoryConnection().add(this.getClass().getResourceAsStream("/testBlankNodes-10.ttl"), "",
                RDFFormat.TURTLE);
        this.getTestRepositoryConnection().commit();
        Assert.assertEquals(1, this.getTestRepositoryConnection().size());
        
    }
    
    @Test
    public final void testTurtleBlankNodeParsing11() throws RDFParseException, RepositoryException, IOException
    {
        this.getTestRepositoryConnection().add(this.getClass().getResourceAsStream("/testBlankNodes-11.ttl"), "",
                RDFFormat.TURTLE);
        this.getTestRepositoryConnection().commit();
        Assert.assertEquals(1, this.getTestRepositoryConnection().size());
        
    }
    
    @Test
    public final void testTurtleBlankNodeParsing2() throws RDFParseException, RepositoryException, IOException
    {
        this.getTestRepositoryConnection().add(this.getClass().getResourceAsStream("/testBlankNodes-2.ttl"), "",
                RDFFormat.TURTLE);
        this.getTestRepositoryConnection().commit();
        Assert.assertEquals(8, this.getTestRepositoryConnection().size());
        
    }
    
    @Test
    public final void testTurtleBlankNodeParsing3() throws RDFParseException, RepositoryException, IOException
    {
        this.getTestRepositoryConnection().add(this.getClass().getResourceAsStream("/testBlankNodes-3.ttl"), "",
                RDFFormat.TURTLE);
        this.getTestRepositoryConnection().commit();
        Assert.assertEquals(2, this.getTestRepositoryConnection().size());
        
    }
    
    @Test
    public final void testTurtleBlankNodeParsing4() throws RDFParseException, RepositoryException, IOException
    {
        this.getTestRepositoryConnection().add(this.getClass().getResourceAsStream("/testBlankNodes-4.ttl"), "",
                RDFFormat.TURTLE);
        this.getTestRepositoryConnection().commit();
        Assert.assertEquals(1, this.getTestRepositoryConnection().size());
        
    }
    
    @Test
    public final void testTurtleBlankNodeParsing5() throws RDFParseException, RepositoryException, IOException
    {
        this.getTestRepositoryConnection().add(this.getClass().getResourceAsStream("/testBlankNodes-5.ttl"), "",
                RDFFormat.TURTLE);
        this.getTestRepositoryConnection().commit();
        Assert.assertEquals(1, this.getTestRepositoryConnection().size());
        
    }
    
    @Test
    public final void testTurtleBlankNodeParsing6() throws RDFParseException, RepositoryException, IOException
    {
        this.getTestRepositoryConnection().add(this.getClass().getResourceAsStream("/testBlankNodes-6.ttl"), "",
                RDFFormat.TURTLE);
        this.getTestRepositoryConnection().commit();
        Assert.assertEquals(1, this.getTestRepositoryConnection().size());
        
    }
    
    @Test
    public final void testTurtleBlankNodeParsing7() throws RDFParseException, RepositoryException, IOException
    {
        this.getTestRepositoryConnection().add(this.getClass().getResourceAsStream("/testBlankNodes-7.ttl"), "",
                RDFFormat.TURTLE);
        this.getTestRepositoryConnection().commit();
        Assert.assertEquals(1, this.getTestRepositoryConnection().size());
        
    }
    
    @Test
    public final void testTurtleBlankNodeParsing8() throws RDFParseException, RepositoryException, IOException
    {
        this.getTestRepositoryConnection().add(this.getClass().getResourceAsStream("/testBlankNodes-8.ttl"), "",
                RDFFormat.TURTLE);
        this.getTestRepositoryConnection().commit();
        Assert.assertEquals(1, this.getTestRepositoryConnection().size());
        
    }
    
    @Test
    public final void testTurtleBlankNodeParsing9() throws RDFParseException, RepositoryException, IOException
    {
        this.getTestRepositoryConnection().add(this.getClass().getResourceAsStream("/testBlankNodes-9.ttl"), "",
                RDFFormat.TURTLE);
        this.getTestRepositoryConnection().commit();
        Assert.assertEquals(1, this.getTestRepositoryConnection().size());
        
    }
    
    /**
     * Test for SES-1647
     * 
     * @throws RDFParseException
     * @throws RepositoryException
     * @throws IOException
     */
    @Test
    public final void testTurtleBlankNodeParsingInseeGeo() throws RDFParseException, RepositoryException, IOException
    {
        this.getTestRepositoryConnection().add(this.getClass().getResourceAsStream("/insee-geo-onto.ttl"), "",
                RDFFormat.TURTLE);
        this.getTestRepositoryConnection().commit();
        Assert.assertEquals(781, this.getTestRepositoryConnection().size());
        
    }
    
}
