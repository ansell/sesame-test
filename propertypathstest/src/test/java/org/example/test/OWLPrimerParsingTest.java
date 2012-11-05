/**
 * 
 */
package org.example.test;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Set;
import java.util.TreeSet;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.StatementCollector;

/**
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 */
public class OWLPrimerParsingTest extends AbstractSesameTest
{
    
    /**
     * @throws java.lang.Exception
     */
    @Override
    @Before
    public void setUp() throws Exception
    {
        super.setUp();
    }
    
    /**
     * @throws java.lang.Exception
     */
    @Override
    @After
    public void tearDown() throws Exception
    {
        super.tearDown();
    }
    
    @Ignore
    @Test
    public void testDifferenceTurtle() throws RDFParseException, RepositoryException, IOException, RDFHandlerException
    {
        final URI owlapiTurtleContext =
                this.getTestValueFactory().createURI("urn:test:sesame:primer.owlapitestresource.turtle.rdf");
        final URI originalTurtleContext =
                this.getTestValueFactory().createURI("urn:test:sesame:primer.original.turtle.rdf");
        final URI fromrdfxmlTurtleContext =
                this.getTestValueFactory().createURI("urn:test:sesame:primer.fromrdfxml.turtle.ttl");
        
        this.getTestRepositoryConnection().add(
                this.getClass().getResourceAsStream("/primer.owlapitestresource.turtle.rdf"), "", RDFFormat.TURTLE,
                owlapiTurtleContext);
        this.getTestRepositoryConnection().commit();
        Assert.assertEquals(280, this.getTestRepositoryConnection().size(owlapiTurtleContext));
        
        this.getTestRepositoryConnection().add(this.getClass().getResourceAsStream("/primer.original.turtle.ttl"), "",
                RDFFormat.TURTLE, originalTurtleContext);
        this.getTestRepositoryConnection().commit();
        Assert.assertEquals(268, this.getTestRepositoryConnection().size(originalTurtleContext));
        
        this.getTestRepositoryConnection().add(this.getClass().getResourceAsStream("/primer.fromrdfxml.turtle.ttl"),
                "", RDFFormat.TURTLE, fromrdfxmlTurtleContext);
        this.getTestRepositoryConnection().commit();
        Assert.assertEquals(295, this.getTestRepositoryConnection().size(fromrdfxmlTurtleContext));
        
        final StatementCollector newStatementCollector = new StatementCollector();
        
        this.getTestRepositoryConnection().export(newStatementCollector);
        
        Assert.assertEquals(843, newStatementCollector.getStatements().size());
        
        // NOTE: This statement comparator distinguishes based on context
        final Set<Statement> testSet = new TreeSet<Statement>();// new StatementComparator());
        testSet.addAll(newStatementCollector.getStatements());
        
        Assert.assertEquals(843, testSet.size());
        
        final Set<Statement> testSet2 = new TreeSet<Statement>();// new
                                                                 // ContextInsensitiveStatementComparator());
        testSet2.addAll(newStatementCollector.getStatements());
        
        // very high due to the number of blank nodes that cannot be directly matches
        Assert.assertEquals(765, testSet2.size());
        
        final StatementCollector owlapiTurtleContextStatementCollector = new StatementCollector();
        this.getTestRepositoryConnection().export(owlapiTurtleContextStatementCollector, owlapiTurtleContext);
        final Set<Statement> testSetOwlapiTurtleContext = new TreeSet<Statement>();// new
                                                                                   // ContextInsensitiveStatementComparator());
        testSetOwlapiTurtleContext.addAll(owlapiTurtleContextStatementCollector.getStatements());
        
        final StatementCollector fromrdfxmlTurtleContextStatementCollector = new StatementCollector();
        this.getTestRepositoryConnection().export(fromrdfxmlTurtleContextStatementCollector, fromrdfxmlTurtleContext);
        final Set<Statement> testSetFromrdfxmlTurtleContext = new TreeSet<Statement>();// new
                                                                                       // ContextInsensitiveStatementComparator());
        testSetFromrdfxmlTurtleContext.addAll(fromrdfxmlTurtleContextStatementCollector.getStatements());
        // Assert.assertTrue(ModelUtil.isSubset(testSetOwlapiTurtleContext,
        // testSetFromrdfxmlTurtleContext));
    }
    
    @Test
    public void testFromRDFXMLTurtleGraphSize() throws RDFParseException, RepositoryException, IOException,
        RDFHandlerException
    {
        this.getTestRepositoryConnection().add(this.getClass().getResourceAsStream("/primer.fromrdfxml.turtle.ttl"),
                "", RDFFormat.TURTLE);
        this.getTestRepositoryConnection().commit();
        Assert.assertEquals(295, this.getTestRepositoryConnection().size());
        // this.getTestRepositoryConnection().export(new TurtleWriter(System.out));
    }
    
    @Test
    public void testOriginalRDFXMLGraphSize() throws RDFParseException, RepositoryException, IOException,
        RDFHandlerException
    {
        this.getTestRepositoryConnection().add(this.getClass().getResourceAsStream("/primer.original.rdfxml.xml"), "",
                RDFFormat.RDFXML);
        this.getTestRepositoryConnection().commit();
        Assert.assertEquals(295, this.getTestRepositoryConnection().size());
        // this.getTestRepositoryConnection().export(new TurtleWriter(System.out));
    }
    
    @Test
    public void testOriginalTurtleGraphSize() throws RDFParseException, RepositoryException, IOException
    {
        this.getTestRepositoryConnection().add(this.getClass().getResourceAsStream("/primer.original.turtle.ttl"), "",
                RDFFormat.TURTLE);
        this.getTestRepositoryConnection().commit();
        Assert.assertEquals(268, this.getTestRepositoryConnection().size());
    }
    
    @Test
    public void testOWLAPIRDFXMLGraphSize() throws RDFParseException, RepositoryException, IOException,
        RDFHandlerException
    {
        this.getTestRepositoryConnection().add(
                this.getClass().getResourceAsStream("/primer.owlapitestresource.rdfxml.xml"), "", RDFFormat.RDFXML);
        this.getTestRepositoryConnection().commit();
        Assert.assertEquals(294, this.getTestRepositoryConnection().size());
        
        // this.getTestRepositoryConnection().export(new TurtleWriter(System.out));
    }
    
    @Test
    public void testOWLAPITurtleGraphSize() throws RDFParseException, RepositoryException, IOException
    {
        this.getTestRepositoryConnection().add(
                this.getClass().getResourceAsStream("/primer.owlapitestresource.turtle.rdf"), "", RDFFormat.TURTLE);
        this.getTestRepositoryConnection().commit();
        Assert.assertEquals(280, this.getTestRepositoryConnection().size());
    }
    
    @Test
    public void testPrimerSubsetRdfXml() throws Exception
    {
        this.getTestRepositoryConnection().add(this.getClass().getResourceAsStream("/rioParserTest1.rdf"), "",
                RDFFormat.RDFXML);
        this.getTestRepositoryConnection().commit();
        Assert.assertEquals(20, this.getTestRepositoryConnection().size());
        
        final StringWriter writer = new StringWriter(2048);
        
        this.getTestRepositoryConnection().export(Rio.createWriter(RDFFormat.TURTLE, writer));
        
        this.getTestRepositoryConnection().clear();
        this.getTestRepositoryConnection().commit();
        
        System.out.println(writer.toString());
        
        this.getTestRepositoryConnection().add(new StringReader(writer.toString()), "", RDFFormat.TURTLE);
        this.getTestRepositoryConnection().commit();
        Assert.assertEquals(20, this.getTestRepositoryConnection().size());
        
    }
    
    @Test
    public void testPrimerSubsetTurtle() throws Exception
    {
        this.getTestRepositoryConnection().add(this.getClass().getResourceAsStream("/rioParserTest1.ttl"), "",
                RDFFormat.TURTLE);
        this.getTestRepositoryConnection().commit();
        Assert.assertEquals(20, this.getTestRepositoryConnection().size());
        
        final StringWriter writer = new StringWriter(2048);
        
        this.getTestRepositoryConnection().export(Rio.createWriter(RDFFormat.RDFXML, writer));
        
        this.getTestRepositoryConnection().clear();
        this.getTestRepositoryConnection().commit();
        
        System.out.println(writer.toString());
        
        this.getTestRepositoryConnection().add(new StringReader(writer.toString()), "", RDFFormat.RDFXML);
        this.getTestRepositoryConnection().commit();
        Assert.assertEquals(20, this.getTestRepositoryConnection().size());
        
    }
}
