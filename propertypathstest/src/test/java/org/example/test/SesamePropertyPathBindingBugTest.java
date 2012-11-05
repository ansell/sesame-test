/**
 * 
 */
package org.example.test;

import java.io.FileOutputStream;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.model.Literal;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.query.BindingSet;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.impl.DatasetImpl;
import org.openrdf.repository.util.RDFInserter;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;
import org.semanticweb.owlapi.formats.RDFXMLOntologyFormatFactory;
import org.semanticweb.owlapi.io.StreamDocumentSource;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyManagerFactoryRegistry;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactoryRegistry;
import org.semanticweb.owlapi.rio.RioRenderer;
import org.semanticweb.owlapi.util.InferredOntologyGenerator;

/**
 * This is a test case for the issue:
 * 
 * http://www.openrdf.org/issues/browse/SES-1091
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 */
public class SesamePropertyPathBindingBugTest extends AbstractSesameTest
{
    private OWLOntologyManager manager;
    private OWLOntology parsedOntology;
    private OWLReasoner reasoner;
    
    private URI testContextUri;
    private URI testInferredContextUri;
    
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
        this.manager = null;
        if(this.reasoner != null)
        {
            this.reasoner.dispose();
            this.reasoner = null;
        }
    }
    
    /**
     * Complete version of the test, now focusing on reducing the case down to a small set of
     * statements.
     * 
     * @throws Exception
     */
    @Ignore
    @Test
    public final void testFromCountWithInferredSpecificDistinctOWLThingAlternative() throws Exception
    {
        this.manager = OWLOntologyManagerFactoryRegistry.createOWLOntologyManager();
        
        this.parsedOntology =
                this.manager.loadOntologyFromOntologyDocument(new StreamDocumentSource(this.getClass()
                        .getResourceAsStream("/plant_ontology-v16.owl"), new RDFXMLOntologyFormatFactory()));
        
        Assert.assertFalse(this.parsedOntology.isEmpty());
        
        this.testContextUri = this.getTestValueFactory().createURI("urn:test:plantontology:context");
        this.testInferredContextUri = this.getTestValueFactory().createURI("urn:test:plantontology:inferred:context");
        
        final RDFInserter repositoryHandler = new RDFInserter(this.getTestRepositoryConnection());
        repositoryHandler.enforceContext(this.testContextUri);
        
        final RioRenderer renderer =
                new RioRenderer(this.parsedOntology, this.manager, repositoryHandler, null, this.testContextUri);
        renderer.render();
        this.getTestRepositoryConnection().commit();
        
        Assert.assertEquals(44332, this.getTestRepositoryConnection().size(this.testContextUri));
        
        final String reasonerName = "Pellet";
        final OWLReasonerFactory configuredReasoner =
                OWLReasonerFactoryRegistry.getInstance().getReasonerFactory(reasonerName);
        
        Assert.assertNotNull("Could not find reasoner", configuredReasoner);
        
        this.reasoner = configuredReasoner.createReasoner(this.parsedOntology);
        Assert.assertTrue("Ontology was not consistent", this.reasoner.isConsistent());
        
        this.reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
        final InferredOntologyGenerator iog = new InferredOntologyGenerator(this.reasoner);
        final OWLOntology inferredAxiomsOntology = this.manager.createOntology(IRI.create(this.testInferredContextUri));
        iog.fillOntology(this.manager, inferredAxiomsOntology);
        
        final RDFInserter inferredRepositoryHandler = new RDFInserter(this.getTestRepositoryConnection());
        inferredRepositoryHandler.enforceContext(this.testInferredContextUri);
        
        final RioRenderer inferencesRenderer =
                new RioRenderer(inferredAxiomsOntology, this.manager, inferredRepositoryHandler, null,
                        this.testInferredContextUri);
        inferencesRenderer.render();
        this.getTestRepositoryConnection().commit();
        
        Assert.assertEquals(2994, this.getTestRepositoryConnection().size(this.testInferredContextUri));
        
        if(this.log.isTraceEnabled())
        {
            for(final Statement nextStatement : this.getTestRepositoryConnection()
                    .getStatements(null, null, null, true, this.testInferredContextUri).asList())
            {
                this.log.trace(nextStatement.toString());
            }
        }
        
        // Uncomment the following code lines to dump the fully inferred ontology file at this
        // point.
        // Dump to a concrete set of triples to narrow down the cause
        // RDFWriter writer = Rio.createWriter(RDFFormat.NTRIPLES, new
        // FileOutputStream("/home/peter/temp/inferredplantontology-v16.nt"));
        // this.getTestRepositoryConnection().export(writer);
        
        final TupleQuery query =
                this.getTestRepositoryConnection()
                        .prepareTupleQuery(
                                QueryLanguage.SPARQL,
                                "SELECT DISTINCT ?parent (COUNT(DISTINCT ?child) AS ?childCount) WHERE { ?child a <http://www.w3.org/2002/07/owl#Class> . ?child <http://www.w3.org/2000/01/rdf-schema#subClassOf>+ ?parent . FILTER(isIRI(?child) && isIRI(?parent)) } GROUP BY ?parent");
        
        final DatasetImpl testDataset = new DatasetImpl();
        testDataset.addDefaultGraph(this.testContextUri);
        testDataset.addDefaultGraph(this.testInferredContextUri);
        
        query.setDataset(testDataset);
        
        query.setBinding("parent", this.getTestValueFactory().createURI("http://www.w3.org/2002/07/owl#Thing"));
        
        final TupleQueryResult queryResult = query.evaluate();
        
        try
        {
            Assert.assertTrue(queryResult.hasNext());
            
            while(queryResult.hasNext())
            {
                final BindingSet bindingSet = queryResult.next();
                
                this.log.info("nextBinding: {}", bindingSet);
                
                Assert.assertTrue(bindingSet.hasBinding("parent"));
                
                Assert.assertTrue(bindingSet.hasBinding("childCount"));
                
                final Literal value = (Literal)bindingSet.getBinding("childCount").getValue();
                
                Assert.assertEquals(1448, value.intValue());
                
                Assert.assertFalse("Should only have been one result binding", queryResult.hasNext());
            }
        }
        finally
        {
            queryResult.close();
        }
    }
    
    /**
     * This test fails still.
     * 
     * @throws Exception
     */
    @Test
    public final void testFromFullConcreteTripleTestFile() throws Exception
    {
        this.getTestRepositoryConnection().add(this.getClass().getResourceAsStream("/inferredplantontology-v16.nt"),
                "", RDFFormat.NTRIPLES, this.testContextUri);
        this.getTestRepositoryConnection().commit();
        
        final TupleQuery query =
                this.getTestRepositoryConnection()
                        .prepareTupleQuery(
                                QueryLanguage.SPARQL,
                                "SELECT DISTINCT ?parent (COUNT(DISTINCT ?child) AS ?childCount) WHERE { ?child a <http://www.w3.org/2002/07/owl#Class> . ?child <http://www.w3.org/2000/01/rdf-schema#subClassOf>+ ?parent . FILTER(isIRI(?child) && isIRI(?parent)) } GROUP BY ?parent");
        
        final DatasetImpl testDataset = new DatasetImpl();
        testDataset.addDefaultGraph(this.testContextUri);
        // switch to a single context to see if that makes a difference
        // testDataset.addDefaultGraph(this.testInferredContextUri);
        
        query.setDataset(testDataset);
        
        query.setBinding("parent", this.getTestValueFactory().createURI("http://www.w3.org/2002/07/owl#Thing"));
        
        final TupleQueryResult queryResult = query.evaluate();
        
        try
        {
            Assert.assertTrue(queryResult.hasNext());
            
            while(queryResult.hasNext())
            {
                final BindingSet bindingSet = queryResult.next();
                
                this.log.info("nextBinding: {}", bindingSet);
                
                Assert.assertTrue(bindingSet.hasBinding("parent"));
                
                Assert.assertTrue(bindingSet.hasBinding("childCount"));
                
                final Literal value = (Literal)bindingSet.getBinding("childCount").getValue();
                
                Assert.assertEquals(1448, value.intValue());
                
                Assert.assertFalse("Should only have been one result binding", queryResult.hasNext());
            }
        }
        finally
        {
            queryResult.close();
        }
    }
    
    /**
     * This test still failing
     * 
     * @throws Exception
     */
    @Test
    public final void testFromGrepReducedConcreteTripleTestFile() throws Exception
    {
        this.getTestRepositoryConnection().add(
                this.getClass().getResourceAsStream("/grep-reducedinferredplantontology-v16.nt"), "",
                RDFFormat.NTRIPLES, this.testContextUri);
        this.getTestRepositoryConnection().commit();
        
        final TupleQuery query =
                this.getTestRepositoryConnection()
                        .prepareTupleQuery(
                                QueryLanguage.SPARQL,
                                "SELECT DISTINCT ?parent (COUNT(DISTINCT ?child) AS ?childCount) WHERE { ?child a <http://www.w3.org/2002/07/owl#Class> . ?child <http://www.w3.org/2000/01/rdf-schema#subClassOf>+ ?parent . FILTER(isIRI(?child) && isIRI(?parent)) } GROUP BY ?parent");
        
        final DatasetImpl testDataset = new DatasetImpl();
        testDataset.addDefaultGraph(this.testContextUri);
        // switch to a single context to see if that makes a difference
        // testDataset.addDefaultGraph(this.testInferredContextUri);
        
        query.setDataset(testDataset);
        
        query.setBinding("parent", this.getTestValueFactory().createURI("http://www.w3.org/2002/07/owl#Thing"));
        
        final TupleQueryResult queryResult = query.evaluate();
        
        try
        {
            Assert.assertTrue(queryResult.hasNext());
            
            while(queryResult.hasNext())
            {
                final BindingSet bindingSet = queryResult.next();
                
                this.log.info("nextBinding: {}", bindingSet);
                
                Assert.assertTrue(bindingSet.hasBinding("parent"));
                
                Assert.assertTrue(bindingSet.hasBinding("childCount"));
                
                final Literal value = (Literal)bindingSet.getBinding("childCount").getValue();
                
                Assert.assertEquals(2, value.intValue());
                
                Assert.assertFalse("Should only have been one result binding", queryResult.hasNext());
            }
        }
        finally
        {
            queryResult.close();
        }
    }
    
    /**
     * This test still failing
     * 
     * @throws Exception
     */
    @Test
    public final void testFromMinimalConcreteTripleTestFile() throws Exception
    {
        this.getTestRepositoryConnection().add(
                this.getClass().getResourceAsStream("/minimalinferredplantontology-v16.nt"), "", RDFFormat.NTRIPLES,
                this.testContextUri);
        this.getTestRepositoryConnection().commit();
        
        final TupleQuery query =
                this.getTestRepositoryConnection()
                        .prepareTupleQuery(
                                QueryLanguage.SPARQL,
                                "SELECT DISTINCT ?parent (COUNT(DISTINCT ?child) AS ?childCount) WHERE { ?child a <http://www.w3.org/2002/07/owl#Class> . ?child <http://www.w3.org/2000/01/rdf-schema#subClassOf>+ ?parent . FILTER(isIRI(?child) && isIRI(?parent)) } GROUP BY ?parent");
        
        final DatasetImpl testDataset = new DatasetImpl();
        testDataset.addDefaultGraph(this.testContextUri);
        // switch to a single context to see if that makes a difference
        // testDataset.addDefaultGraph(this.testInferredContextUri);
        
        query.setDataset(testDataset);
        
        query.setBinding("parent", this.getTestValueFactory().createURI("http://www.w3.org/2002/07/owl#Thing"));
        
        final TupleQueryResult queryResult = query.evaluate();
        
        try
        {
            Assert.assertTrue(queryResult.hasNext());
            
            final AtomicInteger count = new AtomicInteger(0);
            final AtomicInteger missingParentCount = new AtomicInteger(0);
            
            while(queryResult.hasNext())
            {
                final BindingSet bindingSet = queryResult.next();
                
                this.log.info("nextBinding: {}", bindingSet);
                
                if(!bindingSet.hasBinding("parent"))
                {
                    missingParentCount.incrementAndGet();
                }
                
                Assert.assertTrue(bindingSet.hasBinding("childCount"));
                
                final Literal value = (Literal)bindingSet.getBinding("childCount").getValue();
                
                Assert.assertEquals(157, value.intValue());
                
                Assert.assertFalse("Should only have been one result binding", queryResult.hasNext());
                count.incrementAndGet();
            }
            
            Assert.assertEquals("Parent should have been bound to each binding", 0, missingParentCount.get());
            
            Assert.assertEquals("There should have only been one result from the query", 1, count.get());
        }
        finally
        {
            queryResult.close();
        }
    }
    
    /**
     * This test still failing
     * 
     * @throws Exception
     */
    @Test
    public final void testFromMinimalConcreteTripleTestFileWithoutCountAndGroupBy() throws Exception
    {
        this.getTestRepositoryConnection().add(
                this.getClass().getResourceAsStream("/minimalinferredplantontology-v16.nt"), "", RDFFormat.NTRIPLES,
                this.testContextUri);
        this.getTestRepositoryConnection().commit();
        
        final TupleQuery query =
                this.getTestRepositoryConnection()
                        .prepareTupleQuery(
                                QueryLanguage.SPARQL,
                                "SELECT DISTINCT ?parent ?child WHERE { ?child a owl:Class . ?child rdfs:subClassOf+ ?parent . FILTER(isIRI(?child) && isIRI(?parent)) } ORDER BY DESC(?child) LIMIT 10000 OFFSET 0");
        
        final DatasetImpl testDataset = new DatasetImpl();
        testDataset.addDefaultGraph(this.testContextUri);
        // switch to a single context to see if that makes a difference
        // testDataset.addDefaultGraph(this.testInferredContextUri);
        
        query.setDataset(testDataset);
        
        query.setBinding("parent", this.getTestValueFactory().createURI("http://www.w3.org/2002/07/owl#Thing"));
        
        final TupleQueryResult queryResult = query.evaluate();
        
        try
        {
            Assert.assertTrue(queryResult.hasNext());
            
            final AtomicInteger count = new AtomicInteger(0);
            final AtomicInteger missingParentCount = new AtomicInteger(0);
            
            while(queryResult.hasNext())
            {
                final BindingSet bindingSet = queryResult.next();
                
                this.log.info("nextBinding: {}", bindingSet);
                
                Assert.assertTrue(bindingSet.hasBinding("parent"));
                if(!bindingSet.hasBinding("parent"))
                {
                    missingParentCount.incrementAndGet();
                }
                
                Assert.assertTrue(bindingSet.hasBinding("child"));
                
                count.incrementAndGet();
            }
            
            Assert.assertEquals(157, count.get());
            
            Assert.assertEquals("Parent should have been bound to each binding", 0, missingParentCount.get());
        }
        finally
        {
            queryResult.close();
        }
    }
    
    /**
     * This test still failing
     * 
     * @throws Exception
     */
    @Test
    public final void testFromMinimalConcreteTripleTestFileWithoutCountAndGroupByOrFilterIsIRI() throws Exception
    {
        this.getTestRepositoryConnection().add(
                this.getClass().getResourceAsStream("/minimalinferredplantontology-v16.nt"), "", RDFFormat.NTRIPLES,
                this.testContextUri);
        this.getTestRepositoryConnection().commit();
        
        final TupleQuery query =
                this.getTestRepositoryConnection()
                        .prepareTupleQuery(QueryLanguage.SPARQL,
                                "SELECT ?parent ?child WHERE { ?child a <http://www.w3.org/2002/07/owl#Class> . ?child rdfs:subClassOf+ ?parent . } ");
        
        final DatasetImpl testDataset = new DatasetImpl();
        testDataset.addDefaultGraph(this.testContextUri);
        // switch to a single context to see if that makes a difference
        // testDataset.addDefaultGraph(this.testInferredContextUri);
        
        query.setDataset(testDataset);
        
        query.setBinding("parent", this.getTestValueFactory().createURI("http://www.w3.org/2002/07/owl#Thing"));
        
        final TupleQueryResult queryResult = query.evaluate();
        
        try
        {
            Assert.assertTrue(queryResult.hasNext());
            
            final AtomicInteger count = new AtomicInteger(0);
            final AtomicInteger missingParentCount = new AtomicInteger(0);
            
            while(queryResult.hasNext())
            {
                final BindingSet bindingSet = queryResult.next();
                
                this.log.info("nextBinding: {}", bindingSet);
                
                Assert.assertTrue(bindingSet.hasBinding("parent"));
                
                if(!bindingSet.hasBinding("parent"))
                {
                    missingParentCount.incrementAndGet();
                }
                
                Assert.assertTrue(bindingSet.hasBinding("child"));
                
                count.incrementAndGet();
            }
            
            Assert.assertEquals(157, count.get());
            
            Assert.assertEquals("Parent should have been bound to each binding", 0, missingParentCount.get());
        }
        finally
        {
            queryResult.close();
        }
    }
    
    /**
     * This test still failing
     * 
     * @throws Exception
     */
    @Test
    public final void testFromMinimalConcreteTripleTestFileWithoutCountAndGroupByOrFilterIsIRIOrOWLClass()
        throws Exception
    {
        this.getTestRepositoryConnection().add(
                this.getClass().getResourceAsStream("/minimalinferredplantontology-v16.nt"), "", RDFFormat.NTRIPLES,
                this.testContextUri);
        this.getTestRepositoryConnection().commit();
        
        final TupleQuery query =
                this.getTestRepositoryConnection().prepareTupleQuery(QueryLanguage.SPARQL,
                        "SELECT ?parent ?child WHERE { ?child rdfs:subClassOf+ ?parent . } ");
        
        final DatasetImpl testDataset = new DatasetImpl();
        testDataset.addDefaultGraph(this.testContextUri);
        // switch to a single context to see if that makes a difference
        // testDataset.addDefaultGraph(this.testInferredContextUri);
        
        query.setDataset(testDataset);
        
        query.setBinding("parent", this.getTestValueFactory().createURI("http://www.w3.org/2002/07/owl#Thing"));
        
        final TupleQueryResult queryResult = query.evaluate();
        
        try
        {
            Assert.assertTrue(queryResult.hasNext());
            
            final AtomicInteger count = new AtomicInteger(0);
            final AtomicInteger missingParentCount = new AtomicInteger(0);
            
            while(queryResult.hasNext())
            {
                final BindingSet bindingSet = queryResult.next();
                
                this.log.info("nextBinding: {}", bindingSet);
                
                Assert.assertTrue(bindingSet.hasBinding("parent"));
                if(!bindingSet.hasBinding("parent"))
                {
                    missingParentCount.incrementAndGet();
                }
                
                Assert.assertTrue(bindingSet.hasBinding("child"));
                
                count.incrementAndGet();
            }
            
            Assert.assertEquals(157, count.get());
            
            Assert.assertEquals("Parent should have been bound to each binding", 0, missingParentCount.get());
        }
        finally
        {
            queryResult.close();
        }
    }
    
    /**
     * This test still failing
     * 
     * @throws Exception
     */
    @Test
    public final void testFromReducedConcreteTripleTestFile() throws Exception
    {
        this.getTestRepositoryConnection().add(
                this.getClass().getResourceAsStream("/reducedinferredplantontology-v16.nt"), "", RDFFormat.NTRIPLES,
                this.testContextUri);
        this.getTestRepositoryConnection().commit();
        
        final TupleQuery query =
                this.getTestRepositoryConnection()
                        .prepareTupleQuery(
                                QueryLanguage.SPARQL,
                                "SELECT DISTINCT ?parent (COUNT(DISTINCT ?child) AS ?childCount) WHERE { ?child a <http://www.w3.org/2002/07/owl#Class> . ?child <http://www.w3.org/2000/01/rdf-schema#subClassOf>+ ?parent . FILTER(isIRI(?child) && isIRI(?parent)) } GROUP BY ?parent");
        
        final DatasetImpl testDataset = new DatasetImpl();
        testDataset.addDefaultGraph(this.testContextUri);
        // switch to a single context to see if that makes a difference
        // testDataset.addDefaultGraph(this.testInferredContextUri);
        
        query.setDataset(testDataset);
        
        query.setBinding("parent", this.getTestValueFactory().createURI("http://www.w3.org/2002/07/owl#Thing"));
        
        final TupleQueryResult queryResult = query.evaluate();
        
        try
        {
            Assert.assertTrue(queryResult.hasNext());
            
            while(queryResult.hasNext())
            {
                final BindingSet bindingSet = queryResult.next();
                
                this.log.info("nextBinding: {}", bindingSet);
                
                Assert.assertTrue(bindingSet.hasBinding("parent"));
                
                Assert.assertTrue(bindingSet.hasBinding("childCount"));
                
                final Literal value = (Literal)bindingSet.getBinding("childCount").getValue();
                
                Assert.assertEquals(1448, value.intValue());
                
                Assert.assertFalse("Should only have been one result binding", queryResult.hasNext());
            }
        }
        finally
        {
            queryResult.close();
        }
    }
    
    /**
     * This test still failing
     * 
     * @throws Exception
     */
    @Test
    public final void testFromSimpleConcreteTripleTestFileWithCountAndGroupBy() throws Exception
    {
        this.getTestRepositoryConnection().add(this.getClass().getResourceAsStream("/simpletestfile.nt"), "",
                RDFFormat.NTRIPLES, this.testContextUri);
        this.getTestRepositoryConnection().commit();
        
        final TupleQuery query =
                this.getTestRepositoryConnection()
                        .prepareTupleQuery(
                                QueryLanguage.SPARQL,
                                "SELECT DISTINCT ?parent (COUNT(DISTINCT ?child) AS ?childCount) WHERE { ?child a <http://www.w3.org/2002/07/owl#Class> . ?child <http://www.w3.org/2000/01/rdf-schema#subClassOf>+ ?parent . FILTER(isIRI(?child) && isIRI(?parent)) } GROUP BY ?parent");
        
        final DatasetImpl testDataset = new DatasetImpl();
        testDataset.addDefaultGraph(this.testContextUri);
        // switch to a single context to see if that makes a difference
        // testDataset.addDefaultGraph(this.testInferredContextUri);
        
        query.setDataset(testDataset);
        
        query.setBinding("parent", this.getTestValueFactory().createURI("http://www.w3.org/2002/07/owl#Thing"));
        
        final TupleQueryResult queryResult = query.evaluate();
        
        try
        {
            Assert.assertTrue(queryResult.hasNext());
            
            final AtomicInteger missingParentCount = new AtomicInteger(0);
            
            while(queryResult.hasNext())
            {
                final BindingSet bindingSet = queryResult.next();
                
                this.log.info("nextBinding: {}", bindingSet);
                
                Assert.assertTrue(bindingSet.hasBinding("parent"));
                
                Assert.assertTrue(bindingSet.hasBinding("childCount"));
                
                final Literal value = (Literal)bindingSet.getBinding("childCount").getValue();
                
                Assert.assertEquals(4, value.intValue());
                
                Assert.assertTrue(bindingSet.hasBinding("parent"));
                if(!bindingSet.hasBinding("parent"))
                {
                    missingParentCount.incrementAndGet();
                }
                
                Assert.assertFalse("Should only have been one result binding", queryResult.hasNext());
            }
            
            Assert.assertEquals("Parent should have been bound to each binding", 0, missingParentCount.get());
        }
        finally
        {
            queryResult.close();
        }
    }
    
    /**
     * This test still failing
     * 
     * @throws Exception
     */
    @Test
    public final void testFromSimpleConcreteTripleTestFileWithOnlyPropertyPathAndQuerySetBinding() throws Exception
    {
        this.getTestRepositoryConnection().add(this.getClass().getResourceAsStream("/simpletestfile.nt"), "",
                RDFFormat.NTRIPLES, this.testContextUri);
        this.getTestRepositoryConnection().commit();
        
        final TupleQuery query = this.getTestRepositoryConnection().prepareTupleQuery(QueryLanguage.SPARQL,
        // "SELECT ?parent ?child WHERE { ?child a <http://www.w3.org/2002/07/owl#Class> . ?child rdfs:subClassOf+ ?parent . } ");
                "SELECT ?parent ?child WHERE { ?child rdfs:subClassOf+ ?parent . } ");
        
        // The bug appears even if the dataset is not replaced
        final DatasetImpl testDataset = new DatasetImpl();
        testDataset.addDefaultGraph(this.testContextUri);
        // switch to a single context to see if that makes a difference
        // testDataset.addDefaultGraph(this.testInferredContextUri);
        query.setDataset(testDataset);
        
        query.setBinding("parent", this.getTestValueFactory().createURI("http://www.w3.org/2002/07/owl#Thing"));
        
        final TupleQueryResult queryResult = query.evaluate();
        
        try
        {
            Assert.assertTrue(queryResult.hasNext());
            
            final AtomicInteger count = new AtomicInteger(0);
            final AtomicInteger missingParentCount = new AtomicInteger(0);
            
            while(queryResult.hasNext())
            {
                final BindingSet bindingSet = queryResult.next();
                
                this.log.info("nextBinding: {}", bindingSet);
                
                Assert.assertTrue(bindingSet.hasBinding("parent"));
                
                if(!bindingSet.hasBinding("parent"))
                {
                    missingParentCount.incrementAndGet();
                }
                
                Assert.assertTrue(bindingSet.hasBinding("child"));
                
                count.incrementAndGet();
            }
            
            Assert.assertEquals(4, count.get());
            
            Assert.assertEquals("Parent should have been bound to each binding", 0, missingParentCount.get());
        }
        finally
        {
            queryResult.close();
        }
    }
    
    /**
     * This test still failing
     * 
     * @throws Exception
     */
    @Test
    public final void testFromSimpleConcreteTripleTestFileWithOnlyPropertyPathAndQuerySetBindingWithChildClass()
        throws Exception
    {
        this.getTestRepositoryConnection().add(this.getClass().getResourceAsStream("/simpletestfile.nt"), "",
                RDFFormat.NTRIPLES, this.testContextUri);
        this.getTestRepositoryConnection().commit();
        
        final TupleQuery query =
                this.getTestRepositoryConnection()
                        .prepareTupleQuery(QueryLanguage.SPARQL,
                                "SELECT ?parent ?child WHERE { ?child a <http://www.w3.org/2002/07/owl#Class> . ?child rdfs:subClassOf+ ?parent . } ");
        // "SELECT ?parent ?child WHERE { ?child rdfs:subClassOf+ ?parent . } ");
        
        // The bug appears even if the dataset is not replaced
        final DatasetImpl testDataset = new DatasetImpl();
        testDataset.addDefaultGraph(this.testContextUri);
        // switch to a single context to see if that makes a difference
        // testDataset.addDefaultGraph(this.testInferredContextUri);
        query.setDataset(testDataset);
        
        query.setBinding("parent", this.getTestValueFactory().createURI("http://www.w3.org/2002/07/owl#Thing"));
        
        final TupleQueryResult queryResult = query.evaluate();
        
        try
        {
            Assert.assertTrue(queryResult.hasNext());
            
            final AtomicInteger count = new AtomicInteger(0);
            final AtomicInteger missingParentCount = new AtomicInteger(0);
            
            while(queryResult.hasNext())
            {
                final BindingSet bindingSet = queryResult.next();
                
                this.log.info("nextBinding: {}", bindingSet);
                
                Assert.assertTrue(bindingSet.hasBinding("parent"));
                
                if(!bindingSet.hasBinding("parent"))
                {
                    missingParentCount.incrementAndGet();
                }
                
                Assert.assertTrue(bindingSet.hasBinding("child"));
                
                count.incrementAndGet();
            }
            
            Assert.assertEquals(4, count.get());
            
            Assert.assertEquals("Parent should have been bound to each binding", 0, missingParentCount.get());
        }
        finally
        {
            queryResult.close();
        }
    }
    
    /**
     * This test still failing
     * 
     * @throws Exception
     */
    @Test
    public final void testFromSimpleConcreteTripleTestFileWithoutCountAndGroupBy() throws Exception
    {
        this.getTestRepositoryConnection().add(this.getClass().getResourceAsStream("/simpletestfile.nt"), "",
                RDFFormat.NTRIPLES, this.testContextUri);
        this.getTestRepositoryConnection().commit();
        
        final TupleQuery query =
                this.getTestRepositoryConnection()
                        .prepareTupleQuery(
                                QueryLanguage.SPARQL,
                                "SELECT DISTINCT ?parent ?child WHERE { ?child a <http://www.w3.org/2002/07/owl#Class> . ?child rdfs:subClassOf+ ?parent . FILTER(isIRI(?child) && isIRI(?parent)) } ORDER BY DESC(?child) LIMIT 10000 OFFSET 0");
        
        // The bug appears even if the dataset is not replaced
        final DatasetImpl testDataset = new DatasetImpl();
        testDataset.addDefaultGraph(this.testContextUri);
        // switch to a single context to see if that makes a difference
        // testDataset.addDefaultGraph(this.testInferredContextUri);
        query.setDataset(testDataset);
        
        query.setBinding("parent", this.getTestValueFactory().createURI("http://www.w3.org/2002/07/owl#Thing"));
        
        final TupleQueryResult queryResult = query.evaluate();
        
        try
        {
            Assert.assertTrue(queryResult.hasNext());
            
            final AtomicInteger count = new AtomicInteger(0);
            final AtomicInteger missingParentCount = new AtomicInteger(0);
            
            while(queryResult.hasNext())
            {
                final BindingSet bindingSet = queryResult.next();
                
                this.log.info("nextBinding: {}", bindingSet);
                
                Assert.assertTrue(bindingSet.hasBinding("parent"));
                
                if(!bindingSet.hasBinding("parent"))
                {
                    missingParentCount.incrementAndGet();
                }
                
                Assert.assertTrue(bindingSet.hasBinding("child"));
                
                count.incrementAndGet();
            }
            
            Assert.assertEquals(4, count.get());
            
            Assert.assertEquals("Parent should have been bound to each binding", 0, missingParentCount.get());
        }
        finally
        {
            queryResult.close();
        }
    }
    
    /**
     * This method attempts to generate a minimal set of triples for this test case.
     * 
     * Unignore this method and change the file output location to regenerate more similar test
     * files.
     * 
     * @throws Exception
     */
    @Ignore
    @Test
    public final void testGenerateMinimalConcreteTripleTestFile() throws Exception
    {
        this.getTestRepositoryConnection().add(this.getClass().getResourceAsStream("/inferredplantontology-v16.nt"),
                "", RDFFormat.NTRIPLES, this.testContextUri);
        this.getTestRepositoryConnection().commit();
        
        // Dump to a concrete set of triples to narrow down the cause
        final RDFWriter writer =
                Rio.createWriter(RDFFormat.NTRIPLES, new FileOutputStream(
                        "/home/peter/temp/minimalinferredplantontology-v16.nt"));
        
        // GraphQuery query =
        // this.getTestRepositoryConnection().prepareGraphQuery(QueryLanguage.SPARQL,
        // "CONSTRUCT { ?parent a ?parentType . ?class a ?childType . ?class <http://www.w3.org/2000/01/rdf-schema#subClassOf> ?parent . } WHERE { ?class <http://www.w3.org/2000/01/rdf-schema#subClassOf> ?parent . OPTIONAL { ?parent a ?parentType . } OPTIONAL { ?class a ?childType . } } ORDER BY ?parent");
        final GraphQuery query =
                this.getTestRepositoryConnection()
                        .prepareGraphQuery(
                                QueryLanguage.SPARQL,
                                "CONSTRUCT { ?parent a ?parentType . ?child a ?childType . ?child <http://www.w3.org/2000/01/rdf-schema#subClassOf> ?parent . } WHERE { ?child <http://www.w3.org/2000/01/rdf-schema#subClassOf> ?parent . OPTIONAL { ?parent a ?parentType . } OPTIONAL { ?child a ?childType . } FILTER(isIRI(?child) && isIRI(?parent))} ORDER BY DESC(?parent) OFFSET 0 LIMIT 1000");
        
        query.evaluate(writer);
    }
    
    /**
     * Use this method to load up all of the concrete triples and then export the interesting
     * statements to a reduced file.
     * 
     * @throws Exception
     */
    @Ignore
    @Test
    public final void testGenerateReducedConcreteTriplesTestFile() throws Exception
    {
        this.getTestRepositoryConnection().add(this.getClass().getResourceAsStream("/inferredplantontology-v16.nt"),
                "", RDFFormat.NTRIPLES, this.testContextUri);
        this.getTestRepositoryConnection().commit();
        
        // Dump to a concrete set of triples to narrow down the cause
        final RDFWriter writer =
                Rio.createWriter(RDFFormat.NTRIPLES, new FileOutputStream(
                        "/home/peter/temp/reducedinferredplantontology-v16.nt"));
        
        final GraphQuery query =
                this.getTestRepositoryConnection()
                        .prepareGraphQuery(
                                QueryLanguage.SPARQL,
                                "CONSTRUCT { ?parent a ?parentType . ?class a ?childType . ?class <http://www.w3.org/2000/01/rdf-schema#subClassOf> ?parent . } WHERE { ?class <http://www.w3.org/2000/01/rdf-schema#subClassOf> ?parent . OPTIONAL { ?parent a ?parentType . } OPTIONAL { ?class a ?childType . } }");
        
        query.evaluate(writer);
    }
}
