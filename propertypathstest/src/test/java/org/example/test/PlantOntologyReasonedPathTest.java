/**
 * 
 */
package org.example.test;

import java.io.PrintWriter;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.Literal;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.impl.DatasetImpl;
import org.openrdf.repository.util.RDFInserter;
import org.semanticweb.owlapi.formats.RDFXMLOntologyFormatFactory;
import org.semanticweb.owlapi.io.StreamDocumentSource;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyManagerFactoryRegistry;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactoryRegistry;
import org.semanticweb.owlapi.rio.RioRenderer;
import org.semanticweb.owlapi.util.InferredOntologyGenerator;

import com.clarkparsia.owlapi.explanation.PelletExplanation;
import com.clarkparsia.owlapi.explanation.io.manchester.ManchesterSyntaxExplanationRenderer;
import com.clarkparsia.owlapiv3.OWL;
import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;

/**
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 */
public class PlantOntologyReasonedPathTest extends AbstractSesameTest
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
        repositoryHandler.enforceContext(this.testContextUri);
        
        final RioRenderer inferencesRenderer =
                new RioRenderer(inferredAxiomsOntology, this.manager, inferredRepositoryHandler, null,
                        this.testInferredContextUri);
        inferencesRenderer.render();
        this.getTestRepositoryConnection().commit();
        
        Assert.assertEquals(2994, this.getTestRepositoryConnection().size(this.testInferredContextUri));
        
        if(AbstractSesameTest.log.isTraceEnabled())
        {
            for(final Statement nextStatement : this.getTestRepositoryConnection()
                    .getStatements(null, null, null, true, this.testInferredContextUri).asList())
            {
                AbstractSesameTest.log.trace(nextStatement.toString());
            }
        }
    }
    
    /**
     * @throws java.lang.Exception
     */
    @Override
    @After
    public void tearDown() throws Exception
    {
    }
    
    @Test
    public final void testCountWithInferred() throws Exception
    {
        final TupleQuery query =
                this.getTestRepositoryConnection()
                        .prepareTupleQuery(
                                QueryLanguage.SPARQL,
                                "SELECT ?parent (COUNT(?child) AS ?childCount) WHERE { ?parent a <http://www.w3.org/2002/07/owl#Class> . ?parent <http://www.w3.org/2000/01/rdf-schema#subClassOf>+ ?child . } GROUP BY ?parent");
        
        final DatasetImpl testDataset = new DatasetImpl();
        testDataset.addDefaultGraph(this.testContextUri);
        testDataset.addDefaultGraph(this.testInferredContextUri);
        
        query.setDataset(testDataset);
        
        final TupleQueryResult queryResult = query.evaluate();
        
        final AtomicInteger bindingCount = new AtomicInteger(0);
        
        try
        {
            Assert.assertTrue(queryResult.hasNext());
            
            while(queryResult.hasNext())
            {
                final BindingSet bindingSet = queryResult.next();
                bindingCount.incrementAndGet();
                
                for(final Binding nextBinding : bindingSet)
                {
                    AbstractSesameTest.log.info("nextBinding name=" + nextBinding.getName() + " value="
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
        final TupleQuery query =
                this.getTestRepositoryConnection()
                        .prepareTupleQuery(
                                QueryLanguage.SPARQL,
                                "SELECT ?parent (COUNT(?child) AS ?childCount) WHERE { ?parent a <http://www.w3.org/2002/07/owl#Class> . ?parent <http://www.w3.org/2000/01/rdf-schema#subClassOf>+ ?child . } GROUP BY ?parent");
        
        final DatasetImpl testDataset = new DatasetImpl();
        testDataset.addDefaultGraph(this.testContextUri);
        testDataset.addDefaultGraph(this.testInferredContextUri);
        
        query.setDataset(testDataset);
        
        query.clearBindings();
        query.setBinding("parent", this.getTestValueFactory().createURI("http://purl.obolibrary.org/obo/PO_0025215"));
        
        final TupleQueryResult queryResult = query.evaluate();
        
        try
        {
            Assert.assertTrue(queryResult.hasNext());
            
            while(queryResult.hasNext())
            {
                final BindingSet bindingSet = queryResult.next();
                
                Assert.assertTrue(bindingSet.hasBinding("childCount"));
                
                final Literal value = (Literal)bindingSet.getBinding("childCount").getValue();
                
                Assert.assertEquals(28, value.intValue());
                
                Assert.assertFalse("Should only have been one result binding", queryResult.hasNext());
            }
        }
        finally
        {
            queryResult.close();
        }
    }
    
    /**
     * Code originally from
     * pellet/example/src/test/java/org/mindswap/pellet/examples/ExplanationExample.java
     * 
     * @throws Exception
     */
    @Test
    public final void testExplanation() throws Exception
    {
        // TODO: Create an arbitrary renderer to print to any textual OWLOntologyStorer
        final ManchesterSyntaxExplanationRenderer renderer = new ManchesterSyntaxExplanationRenderer();
        // The writer used for the explanation rendered
        final PrintWriter out = new PrintWriter(System.out);
        renderer.startRendering(out);
        
        final PelletExplanation expGen =
                new PelletExplanation(PelletReasonerFactory.getInstance().createReasoner(this.parsedOntology));
        
        // Create some concepts
        // OWLClass madCow = OWL.Class( IRI.create("") );
        
        // Set<Set<OWLAxiom>> exp = expGen.getUnsatisfiableExplanations( madCow );
        // out.println( "Why is " + madCow + " concept unsatisfiable?" );
        // renderer.render( exp );
        
        final OWLClass phylomeStomatalComplex = OWL.Class(IRI.create("http://purl.obolibrary.org/obo/PO_0025215"));
        final OWLClass bractStomatalComplex = OWL.Class(IRI.create("http://purl.obolibrary.org/obo/PO_0025216"));
        final OWLClass plantAnatomicalEntity = OWL.Class(IRI.create("http://purl.obolibrary.org/obo/PO_0025131"));
        final OWLClass phylome = OWL.Class(IRI.create("http://purl.obolibrary.org/obo/PO_0006001"));
        
        final Set<Set<OWLAxiom>> exp = expGen.getSubClassExplanations(bractStomatalComplex, phylomeStomatalComplex);
        Assert.assertFalse(exp.isEmpty());
        out.println("Why is " + bractStomatalComplex + " subclass of " + phylomeStomatalComplex + "?");
        renderer.render(exp);
        
        final Set<Set<OWLAxiom>> exp2 = expGen.getSubClassExplanations(bractStomatalComplex, plantAnatomicalEntity);
        Assert.assertFalse(exp2.isEmpty());
        out.println("Why is " + bractStomatalComplex + " subclass of " + plantAnatomicalEntity + "?");
        renderer.render(exp2);
        
        renderer.endRendering();
    }
    
    @Test
    public final void testWithInferred() throws Exception
    {
        final TupleQuery query =
                this.getTestRepositoryConnection()
                        .prepareTupleQuery(
                                QueryLanguage.SPARQL,
                                "SELECT ?class ?subclassof WHERE { ?class a <http://www.w3.org/2002/07/owl#Class> . ?class <http://www.w3.org/2000/01/rdf-schema#subClassOf>+ ?subclassof . }");
        
        final DatasetImpl testDataset = new DatasetImpl();
        testDataset.addDefaultGraph(this.testContextUri);
        testDataset.addDefaultGraph(this.testInferredContextUri);
        
        query.setDataset(testDataset);
        
        final TupleQueryResult queryResult = query.evaluate();
        
        final AtomicInteger bindingCount = new AtomicInteger(0);
        
        try
        {
            Assert.assertTrue(queryResult.hasNext());
            
            while(queryResult.hasNext())
            {
                final BindingSet bindingSet = queryResult.next();
                bindingCount.incrementAndGet();
                
                if(AbstractSesameTest.log.isTraceEnabled())
                {
                    for(final Binding nextBinding : bindingSet)
                    {
                        AbstractSesameTest.log.trace("nextBinding name=" + nextBinding.getName() + " value="
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
    public final void testWithoutInferred() throws Exception
    {
        final TupleQuery query =
                this.getTestRepositoryConnection()
                        .prepareTupleQuery(
                                QueryLanguage.SPARQL,
                                "SELECT ?class ?subclassof WHERE { ?class a <http://www.w3.org/2002/07/owl#Class> . ?class <http://www.w3.org/2000/01/rdf-schema#subClassOf>+ ?subclassof . }");
        
        // test with a dataset that does not contain the inferred statements context
        final DatasetImpl testDataset = new DatasetImpl();
        testDataset.addDefaultGraph(this.testContextUri);
        
        query.setDataset(testDataset);
        
        final TupleQueryResult queryResult = query.evaluate();
        
        final AtomicInteger bindingCount = new AtomicInteger(0);
        
        try
        {
            Assert.assertTrue(queryResult.hasNext());
            
            while(queryResult.hasNext())
            {
                final BindingSet bindingSet = queryResult.next();
                bindingCount.incrementAndGet();
                
                if(AbstractSesameTest.log.isTraceEnabled())
                {
                    for(final Binding nextBinding : bindingSet)
                    {
                        AbstractSesameTest.log.trace("nextBinding name=" + nextBinding.getName() + " value="
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
