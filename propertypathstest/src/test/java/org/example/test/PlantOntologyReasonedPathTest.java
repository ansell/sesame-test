/**
 * 
 */
package org.example.test;

import static org.junit.Assert.*;

import java.net.URLEncoder;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.impl.DatasetImpl;
import org.openrdf.repository.util.RDFInserter;
import org.openrdf.rio.RDFHandler;
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
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 */
public class PlantOntologyReasonedPathTest extends AbstractSesameTest
{
    private OWLOntologyManager manager;
    private OWLOntology parsedOntology;
    
    private URI testContextUri;
    private URI testInferredContextUri;
    
    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception
    {
        super.setUp();
        
        manager = OWLOntologyManagerFactoryRegistry.createOWLOntologyManager();
        
        parsedOntology =
                manager.loadOntologyFromOntologyDocument(new StreamDocumentSource(this.getClass().getResourceAsStream(
                        "/plant_ontology-v16.owl"), new RDFXMLOntologyFormatFactory()));
        
        assertFalse(parsedOntology.isEmpty());
        
        testContextUri = this.getTestValueFactory().createURI("urn:test:plantontology:context");
        testInferredContextUri = this.getTestValueFactory().createURI("urn:test:plantontology:inferred:context");
        
        RDFInserter repositoryHandler = new RDFInserter(getTestRepositoryConnection());
        repositoryHandler.enforceContext(testContextUri);
        
        RioRenderer renderer = new RioRenderer(parsedOntology, manager, repositoryHandler, null, testContextUri);
        renderer.render();
        this.getTestRepositoryConnection().commit();
        
        assertEquals(44332, this.getTestRepositoryConnection().size(testContextUri));
        
        String reasonerName = "Pellet";
        OWLReasonerFactory configuredReasoner =
                OWLReasonerFactoryRegistry.getInstance().getReasonerFactory(reasonerName);
        
        assertNotNull("Could not find reasoner", configuredReasoner);
        
        OWLReasoner reasoner = configuredReasoner.createReasoner(parsedOntology);
        assertTrue("Ontology was not consistent", reasoner.isConsistent());
        
        reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
        InferredOntologyGenerator iog = new InferredOntologyGenerator(reasoner);
        OWLOntology inferredAxiomsOntology = this.manager.createOntology(IRI.create(testInferredContextUri));
        iog.fillOntology(this.manager, inferredAxiomsOntology);
        
        RDFInserter inferredRepositoryHandler = new RDFInserter(getTestRepositoryConnection());
        repositoryHandler.enforceContext(testContextUri);
        
        RioRenderer inferencesRenderer =
                new RioRenderer(inferredAxiomsOntology, manager, inferredRepositoryHandler, null,
                        testInferredContextUri);
        inferencesRenderer.render();
        this.getTestRepositoryConnection().commit();
        
        assertEquals(2994, this.getTestRepositoryConnection().size(testInferredContextUri));
        
        if(log.isTraceEnabled())
        {
            for(Statement nextStatement : this.getTestRepositoryConnection()
                    .getStatements(null, null, null, true, testInferredContextUri).asList())
            {
                log.trace(nextStatement.toString());
            }
        }
    }
    
    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception
    {
    }
    
    @Test
    public final void testWithInferred() throws Exception
    {
        TupleQuery query =
                this.getTestRepositoryConnection()
                        .prepareTupleQuery(
                                QueryLanguage.SPARQL,
                                "SELECT ?class ?subclassof WHERE { ?class a <http://www.w3.org/2002/07/owl#Class> . ?class <http://www.w3.org/2000/01/rdf-schema#subClassOf>+ ?subclassof . }");
        
        DatasetImpl testDataset = new DatasetImpl();
        testDataset.addDefaultGraph(testContextUri);
        testDataset.addDefaultGraph(testInferredContextUri);
        
        query.setDataset(testDataset);
        
        TupleQueryResult queryResult = query.evaluate();
        
        AtomicInteger bindingCount = new AtomicInteger(0);
        
        try
        {
            Assert.assertTrue(queryResult.hasNext());
            
            while(queryResult.hasNext())
            {
                BindingSet bindingSet = queryResult.next();
                bindingCount.incrementAndGet();
                
                if(log.isTraceEnabled())
                {
                    for(Binding nextBinding : bindingSet)
                    {
                        log.trace("nextBinding name=" + nextBinding.getName() + " value="
                                + nextBinding.getValue().stringValue());
                    }
                }
            }
        }
        finally
        {
            queryResult.close();
        }
        
        Assert.assertEquals(23702, bindingCount.get());
    }
    
    @Test
    public final void testCountWithInferred() throws Exception
    {
        TupleQuery query =
                this.getTestRepositoryConnection()
                        .prepareTupleQuery(
                                QueryLanguage.SPARQL,
                                "SELECT ?parent (COUNT(?child) AS ?childCount) WHERE { ?parent a <http://www.w3.org/2002/07/owl#Class> . ?parent <http://www.w3.org/2000/01/rdf-schema#subClassOf>+ ?child . } GROUP BY ?parent");
        
        DatasetImpl testDataset = new DatasetImpl();
        testDataset.addDefaultGraph(testContextUri);
        testDataset.addDefaultGraph(testInferredContextUri);
        
        query.setDataset(testDataset);
        
        TupleQueryResult queryResult = query.evaluate();
        
        AtomicInteger bindingCount = new AtomicInteger(0);
        
        try
        {
            Assert.assertTrue(queryResult.hasNext());
            
            while(queryResult.hasNext())
            {
                BindingSet bindingSet = queryResult.next();
                bindingCount.incrementAndGet();
                
                for(Binding nextBinding : bindingSet)
                {
                    log.info("nextBinding name=" + nextBinding.getName() + " value="
                            + nextBinding.getValue().stringValue());
                }
            }
        }
        finally
        {
            queryResult.close();
        }
        
        Assert.assertEquals(1448, bindingCount.get());
    }
    
    @Test
    public final void testCountWithInferredSpecific() throws Exception
    {
        TupleQuery query =
                this.getTestRepositoryConnection()
                        .prepareTupleQuery(
                                QueryLanguage.SPARQL,
                                "SELECT ?parent (COUNT(?child) AS ?childCount) WHERE { ?parent a <http://www.w3.org/2002/07/owl#Class> . ?parent <http://www.w3.org/2000/01/rdf-schema#subClassOf>+ ?child . } GROUP BY ?parent");
        
        DatasetImpl testDataset = new DatasetImpl();
        testDataset.addDefaultGraph(testContextUri);
        testDataset.addDefaultGraph(testInferredContextUri);
        
        query.setDataset(testDataset);
        
        query.clearBindings();
        query.setBinding("parent", this.getTestValueFactory().createURI("http://purl.obolibrary.org/obo/PO_0025215"));
        
        TupleQueryResult queryResult = query.evaluate();
        
        try
        {
            Assert.assertTrue(queryResult.hasNext());
            
            while(queryResult.hasNext())
            {
                BindingSet bindingSet = queryResult.next();
                
                assertTrue(bindingSet.hasBinding("childCount"));
                
                Literal value = (Literal)bindingSet.getBinding("childCount").getValue();
                
                assertEquals(28, value.intValue());
                
                Assert.assertFalse("Should only have been one result binding", queryResult.hasNext());
            }
        }
        finally
        {
            queryResult.close();
        }
    }
    
    @Test
    public final void testWithoutInferred() throws Exception
    {
        TupleQuery query =
                this.getTestRepositoryConnection()
                        .prepareTupleQuery(
                                QueryLanguage.SPARQL,
                                "SELECT ?class ?subclassof WHERE { ?class a <http://www.w3.org/2002/07/owl#Class> . ?class <http://www.w3.org/2000/01/rdf-schema#subClassOf>+ ?subclassof . }");
        
        // test with a dataset that does not contain the inferred statements context
        DatasetImpl testDataset = new DatasetImpl();
        testDataset.addDefaultGraph(testContextUri);
        
        query.setDataset(testDataset);
        
        TupleQueryResult queryResult = query.evaluate();
        
        AtomicInteger bindingCount = new AtomicInteger(0);
        
        try
        {
            Assert.assertTrue(queryResult.hasNext());
            
            while(queryResult.hasNext())
            {
                BindingSet bindingSet = queryResult.next();
                bindingCount.incrementAndGet();
                
                if(log.isTraceEnabled())
                {
                    for(Binding nextBinding : bindingSet)
                    {
                        log.trace("nextBinding name=" + nextBinding.getName() + " value="
                                + nextBinding.getValue().stringValue());
                    }
                }
            }
        }
        finally
        {
            queryResult.close();
        }
        
        Assert.assertEquals(9003, bindingCount.get());
    }
    
}
