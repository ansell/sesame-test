/**
 * 
 */
package org.example.test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;

import net.fortytwo.sesametools.ContextInsensitiveStatementComparator;
import net.fortytwo.sesametools.StatementComparator;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.util.ModelUtil;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.helpers.StatementCollector;
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
    public void testOriginalRDFXMLGraphSize() throws RDFParseException, RepositoryException, IOException,
        RDFHandlerException
    {
        this.getTestRepositoryConnection().add(this.getClass().getResourceAsStream("/primer.original.rdfxml.xml"), "",
                RDFFormat.RDFXML);
        
        Assert.assertEquals(295, this.getTestRepositoryConnection().size());
        
        // this.getTestRepositoryConnection().export(new TurtleWriter(System.out));
    }
    
    @Test
    public void testOWLAPIRDFXMLGraphSize() throws RDFParseException, RepositoryException, IOException,
        RDFHandlerException
    {
        this.getTestRepositoryConnection().add(
                this.getClass().getResourceAsStream("/primer.owlapitestresource.rdfxml.xml"), "", RDFFormat.RDFXML);
        
        Assert.assertEquals(294, this.getTestRepositoryConnection().size());
        
        // this.getTestRepositoryConnection().export(new TurtleWriter(System.out));
    }
    
    @Test
    public void testFromRDFXMLTurtleGraphSize() throws RDFParseException, RepositoryException, IOException,
        RDFHandlerException
    {
        this.getTestRepositoryConnection().add(this.getClass().getResourceAsStream("/primer.fromrdfxml.turtle.ttl"),
                "", RDFFormat.TURTLE);
        
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
    
    @Test
    public void testDifferenceTurtle() throws RDFParseException, RepositoryException, IOException, RDFHandlerException
    {
        URI owlapiTurtleContext =
                this.getTestValueFactory().createURI("urn:test:sesame:primer.owlapitestresource.turtle.rdf");
        URI originalTurtleContext = this.getTestValueFactory().createURI("urn:test:sesame:primer.original.turtle.rdf");
        URI fromrdfxmlTurtleContext =
                this.getTestValueFactory().createURI("urn:test:sesame:primer.fromrdfxml.turtle.ttl");
        
        this.getTestRepositoryConnection().add(
                this.getClass().getResourceAsStream("/primer.owlapitestresource.turtle.rdf"), "", RDFFormat.TURTLE,
                owlapiTurtleContext);
        Assert.assertEquals(280, this.getTestRepositoryConnection().size(owlapiTurtleContext));
        
        this.getTestRepositoryConnection().add(this.getClass().getResourceAsStream("/primer.original.turtle.ttl"), "",
                RDFFormat.TURTLE, originalTurtleContext);
        Assert.assertEquals(268, this.getTestRepositoryConnection().size(originalTurtleContext));
        
        this.getTestRepositoryConnection().add(this.getClass().getResourceAsStream("/primer.fromrdfxml.turtle.ttl"),
                "", RDFFormat.TURTLE, fromrdfxmlTurtleContext);
        Assert.assertEquals(295, this.getTestRepositoryConnection().size(fromrdfxmlTurtleContext));
        
        StatementCollector newStatementCollector = new StatementCollector();
        
        this.getTestRepositoryConnection().export(newStatementCollector);
        
        Assert.assertEquals(843, newStatementCollector.getStatements().size());
        
        // NOTE: This statement comparator distinguishes based on context
        Set<Statement> testSet = new TreeSet<Statement>(new StatementComparator());
        testSet.addAll(newStatementCollector.getStatements());
        
        Assert.assertEquals(843, testSet.size());
        
        Set<Statement> testSet2 = new TreeSet<Statement>(new ContextInsensitiveStatementComparator());
        testSet2.addAll(newStatementCollector.getStatements());
        
        // very high due to the number of blank nodes that cannot be directly matches
        Assert.assertEquals(765, testSet2.size());
        
        StatementCollector owlapiTurtleContextStatementCollector = new StatementCollector();
        this.getTestRepositoryConnection().export(owlapiTurtleContextStatementCollector, owlapiTurtleContext);
        Set<Statement> testSetOwlapiTurtleContext = new TreeSet<Statement>(new ContextInsensitiveStatementComparator());
        testSetOwlapiTurtleContext.addAll(owlapiTurtleContextStatementCollector.getStatements());
        
        StatementCollector fromrdfxmlTurtleContextStatementCollector = new StatementCollector();
        this.getTestRepositoryConnection().export(fromrdfxmlTurtleContextStatementCollector, fromrdfxmlTurtleContext);
        Set<Statement> testSetFromrdfxmlTurtleContext =
                new TreeSet<Statement>(new ContextInsensitiveStatementComparator());
        testSetFromrdfxmlTurtleContext.addAll(fromrdfxmlTurtleContextStatementCollector.getStatements());
        
        //Assert.assertTrue(ModelUtil.isSubset(testSetOwlapiTurtleContext, testSetFromrdfxmlTurtleContext));
    }
}
