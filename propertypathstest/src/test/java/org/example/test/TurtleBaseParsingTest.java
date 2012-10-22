/**
 * 
 */
package org.example.test;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.Statement;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;

/**
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 */
public class TurtleBaseParsingTest extends AbstractSesameTest
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
        this.getTestRepositoryConnection().add(new StringReader("@base <http://test.org/path#> .\n <a1> <b1> <c1> ."), "",
                RDFFormat.TURTLE);
        this.getTestRepositoryConnection().commit();
        
        Assert.assertEquals(1, this.getTestRepositoryConnection().size());
        
        List<Statement> asList = this.getTestRepositoryConnection().getStatements(null, null, null, false).asList();
        
        Assert.assertEquals(1, asList.size());
        
        Statement st = asList.get(0);
        
        Assert.assertEquals(this.getTestValueFactory().createURI("http://test.org/a1"), st.getSubject());
        Assert.assertEquals(this.getTestValueFactory().createURI("http://test.org/b1"), st.getPredicate());
        Assert.assertEquals(this.getTestValueFactory().createURI("http://test.org/c1"), st.getObject());
        
    }
    
    @Test
    public final void testTurtleBlankNodeParsingAlternate() throws RDFParseException, RepositoryException, IOException
    {
        this.getTestRepositoryConnection().add(new StringReader("@base <http://test.org/path> .\n <#a1> <#b1> <#c1> ."), "",
                RDFFormat.TURTLE);
        this.getTestRepositoryConnection().commit();
        
        Assert.assertEquals(1, this.getTestRepositoryConnection().size());
        
        List<Statement> asList = this.getTestRepositoryConnection().getStatements(null, null, null, false).asList();
        
        Assert.assertEquals(1, asList.size());
        
        Statement st = asList.get(0);
        
        Assert.assertEquals(this.getTestValueFactory().createURI("http://test.org/path#a1"), st.getSubject());
        Assert.assertEquals(this.getTestValueFactory().createURI("http://test.org/path#b1"), st.getPredicate());
        Assert.assertEquals(this.getTestValueFactory().createURI("http://test.org/path#c1"), st.getObject());
        
    }

    
}
