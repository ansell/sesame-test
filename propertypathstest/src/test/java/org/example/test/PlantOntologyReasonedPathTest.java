/**
 * 
 */
package org.example.test;

import java.io.PrintWriter;
import java.util.LinkedHashSet;
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
import org.openrdf.query.GraphQuery;
import org.openrdf.query.GraphQueryResult;
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
    
    private OWLClass phylomeStomatalComplex;
    private OWLClass bractStomatalComplex;
    private OWLClass plantAnatomicalEntity;
    private OWLClass phylome;
    
    /**
     * @throws java.lang.Exception
     */
    @Override
    @Before
    public void setUp() throws Exception
    {
        super.setUp();
        
        this.manager = OWLOntologyManagerFactoryRegistry.createOWLOntologyManager();
        
        this.phylomeStomatalComplex = OWL.Class(IRI.create("http://purl.obolibrary.org/obo/PO_0025215"));
        this.bractStomatalComplex = OWL.Class(IRI.create("http://purl.obolibrary.org/obo/PO_0025216"));
        this.plantAnatomicalEntity = OWL.Class(IRI.create("http://purl.obolibrary.org/obo/PO_0025131"));
        this.phylome = OWL.Class(IRI.create("http://purl.obolibrary.org/obo/PO_0006001"));
        
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
    
    @Test
    public final void testClassHierarchyRenderingSubDirect() throws Exception
    {
        final Set<OWLClass> flattened = this.reasoner.getSubClasses(this.phylome, true).getFlattened();
        
        for(final OWLClass nextClass : flattened)
        {
            System.out.println(nextClass);
        }
        
        Assert.assertEquals(14, flattened.size());
    }
    
    @Test
    public final void testClassHierarchyRenderingSubDirectTopLevel() throws Exception
    {
        final Set<OWLClass> flattened = this.reasoner.getSubClasses(OWL.Thing, true).getFlattened();
        
        for(final OWLClass nextClass : flattened)
        {
            System.out.println(nextClass);
        }
        
        Assert.assertEquals(132, flattened.size());
    }
    
    @Test
    public final void testClassHierarchyRenderingSubDirectTopLevelReasonerTopClassNode() throws Exception
    {
        final Set<OWLClass> flattened =
                this.reasoner.getSubClasses(this.reasoner.getTopClassNode().getRepresentativeElement(), true)
                        .getFlattened();
        
        for(final OWLClass nextClass : flattened)
        {
            System.out.println(nextClass);
        }
        
        Assert.assertEquals(132, flattened.size());
    }
    
    @Test
    public final void testClassHierarchyRenderingSubNotDirect() throws Exception
    {
        final Set<OWLClass> flattened = this.reasoner.getSubClasses(this.phylome, false).getFlattened();
        
        for(final OWLClass nextClass : flattened)
        {
            System.out.println(nextClass);
        }
        
        Assert.assertEquals(79, flattened.size());
    }
    
    @Test
    public final void testClassHierarchyRenderingSubNotDirectTopLevel() throws Exception
    {
        final Set<OWLClass> flattened = this.reasoner.getSubClasses(OWL.Thing, false).getFlattened();
        
        for(final OWLClass nextClass : flattened)
        {
            System.out.println(nextClass);
        }
        
        Assert.assertEquals(1449, flattened.size());
    }
    
    @Test
    public final void testClassHierarchyRenderingSubNotDirectTopLevelReasonerTopClassNode() throws Exception
    {
        final Set<OWLClass> flattened =
                this.reasoner.getSubClasses(this.reasoner.getTopClassNode().getRepresentativeElement(), false)
                        .getFlattened();
        
        for(final OWLClass nextClass : flattened)
        {
            System.out.println(nextClass);
        }
        
        Assert.assertEquals(1449, flattened.size());
    }
    
    @Test
    public final void testClassHierarchyRenderingSuperDirect() throws Exception
    {
        final Set<OWLClass> flattened = this.reasoner.getSuperClasses(this.phylome, true).getFlattened();
        
        for(final OWLClass nextClass : flattened)
        {
            System.out.println(nextClass);
        }
        
        Assert.assertEquals(1, flattened.size());
    }
    
    @Test
    public final void testClassHierarchyRenderingSuperNotDirect() throws Exception
    {
        final Set<OWLClass> flattened = this.reasoner.getSuperClasses(this.phylome, false).getFlattened();
        
        for(final OWLClass nextClass : flattened)
        {
            System.out.println(nextClass);
        }
        
        Assert.assertEquals(4, flattened.size());
    }
    
    @Test
    public final void testClassHierarchyRenderingToHTML() throws Exception
    {
        final Set<OWLClass> flattened = this.reasoner.getSubClasses(OWL.Thing, true).getFlattened();
        
        final StringBuilder sb = new StringBuilder(1024);
        
        sb.append("<div>");
        sb.append(OWL.Thing.getIRI().toString());
        sb.append("</div>\n");
        
        sb.append("<ol>\n");
        for(final OWLClass nextClass : flattened)
        {
            sb.append("<li>");
            
            System.out.println(nextClass);
            sb.append(nextClass.getIRI().toString());
            
            sb.append("</li>\n");
        }
        sb.append("</ol>\n");
        
        Assert.assertEquals(132, flattened.size());
        
        System.out.println(sb.toString());
    }
    
    @Test
    public final void testCountWithInferred() throws Exception
    {
        final TupleQuery query =
                this.getTestRepositoryConnection()
                        .prepareTupleQuery(
                                QueryLanguage.SPARQL,
                                "SELECT ?parent (COUNT(?child) AS ?childCount) WHERE { ?parent a <http://www.w3.org/2002/07/owl#Class> . ?parent <http://www.w3.org/2000/01/rdf-schema#subClassOf>+ ?child . FILTER(isIRI(?child) && isIRI(?parent)) } GROUP BY ?parent");
        
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
                    this.log.info("nextBinding name=" + nextBinding.getName() + " value="
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
    public final void testCountWithInferredDistinct() throws Exception
    {
        final TupleQuery query =
                this.getTestRepositoryConnection()
                        .prepareTupleQuery(
                                QueryLanguage.SPARQL,
                                "SELECT DISTINCT ?parent (COUNT(DISTINCT ?child) AS ?childCount) WHERE { ?parent a <http://www.w3.org/2002/07/owl#Class> . ?parent <http://www.w3.org/2000/01/rdf-schema#subClassOf>+ ?child . FILTER(isIRI(?child) && isIRI(?parent)) } GROUP BY ?parent");
        
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
                    this.log.info("nextBinding name=" + nextBinding.getName() + " value="
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
                                "SELECT ?parent (COUNT(?child) AS ?childCount) WHERE { ?parent a <http://www.w3.org/2002/07/owl#Class> . ?parent <http://www.w3.org/2000/01/rdf-schema#subClassOf>+ ?child . FILTER(isIRI(?child) && isIRI(?parent)) } GROUP BY ?parent");
        
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
                
                Assert.assertEquals(20, value.intValue());
                
                Assert.assertFalse("Should only have been one result binding", queryResult.hasNext());
            }
        }
        finally
        {
            queryResult.close();
        }
    }
    
    @Test
    public final void testCountWithInferredSpecificDistinct() throws Exception
    {
        final TupleQuery query =
                this.getTestRepositoryConnection()
                        .prepareTupleQuery(
                                QueryLanguage.SPARQL,
                                "SELECT DISTINCT ?parent (COUNT(DISTINCT ?child) AS ?childCount) WHERE { ?parent a <http://www.w3.org/2002/07/owl#Class> . ?parent <http://www.w3.org/2000/01/rdf-schema#subClassOf>+ ?child . FILTER(isIRI(?child) && isIRI(?parent)) } GROUP BY ?parent");
        
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
                
                Assert.assertEquals(8, value.intValue());
                
                Assert.assertFalse("Should only have been one result binding", queryResult.hasNext());
            }
        }
        finally
        {
            queryResult.close();
        }
    }
    
    @Test
    public final void testCountWithInferredSpecificDistinctOWLThing() throws Exception
    {
        final TupleQuery query =
                this.getTestRepositoryConnection()
                        .prepareTupleQuery(
                                QueryLanguage.SPARQL,
                                "SELECT DISTINCT ?parent (COUNT(DISTINCT ?child) AS ?childCount) WHERE { ?child a <http://www.w3.org/2002/07/owl#Class> . ?child <http://www.w3.org/2000/01/rdf-schema#subClassOf> ?parent . FILTER(isIRI(?child) && isIRI(?parent)) } GROUP BY ?parent");
        
        final DatasetImpl testDataset = new DatasetImpl();
        testDataset.addDefaultGraph(this.testContextUri);
        testDataset.addDefaultGraph(this.testInferredContextUri);
        
        query.setDataset(testDataset);
        
        query.clearBindings();
        query.setBinding("parent", this.getTestValueFactory().createURI("http://www.w3.org/2002/07/owl#Thing"));
        
        final TupleQueryResult queryResult = query.evaluate();
        
        try
        {
            Assert.assertTrue(queryResult.hasNext());
            
            while(queryResult.hasNext())
            {
                final BindingSet bindingSet = queryResult.next();
                
                Assert.assertTrue(bindingSet.hasBinding("childCount"));
                
                final Literal value = (Literal)bindingSet.getBinding("childCount").getValue();
                
                Assert.assertEquals(132, value.intValue());
                
                Assert.assertFalse("Should only have been one result binding", queryResult.hasNext());
            }
        }
        finally
        {
            queryResult.close();
        }
    }
    
    @Test
    public final void testCountWithInferredSpecificDistinctOWLThingAlternative() throws Exception
    {
        final TupleQuery query =
                this.getTestRepositoryConnection()
                        .prepareTupleQuery(
                                QueryLanguage.SPARQL,
                                "SELECT DISTINCT ?parent (COUNT(DISTINCT ?child) AS ?childCount) WHERE { ?child a <http://www.w3.org/2002/07/owl#Class> . ?child <http://www.w3.org/2000/01/rdf-schema#subClassOf>+ ?parent . FILTER(isIRI(?child) && isIRI(?parent)) } GROUP BY ?parent");
        
        final DatasetImpl testDataset = new DatasetImpl();
        testDataset.addDefaultGraph(this.testContextUri);
        testDataset.addDefaultGraph(this.testInferredContextUri);
        
        query.setDataset(testDataset);
        
        query.clearBindings();
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
    
    @Test
    public final void testCountWithInferredSpecificDistinctOWLThingNoExternalBinding() throws Exception
    {
        final TupleQuery query =
                this.getTestRepositoryConnection()
                        .prepareTupleQuery(
                                QueryLanguage.SPARQL,
                                "SELECT (COUNT(DISTINCT ?child) AS ?childCount) WHERE { ?child a <http://www.w3.org/2002/07/owl#Class> . ?child <http://www.w3.org/2000/01/rdf-schema#subClassOf>+ <http://www.w3.org/2002/07/owl#Thing> . FILTER(isIRI(?child) ) }");
        
        final DatasetImpl testDataset = new DatasetImpl();
        testDataset.addDefaultGraph(this.testContextUri);
        testDataset.addDefaultGraph(this.testInferredContextUri);
        
        query.setDataset(testDataset);
        
        // query.clearBindings();
        // query.setBinding("parent",
        // this.getTestValueFactory().createURI("http://www.w3.org/2002/07/owl#Thing"));
        
        final TupleQueryResult queryResult = query.evaluate();
        
        try
        {
            Assert.assertTrue(queryResult.hasNext());
            
            while(queryResult.hasNext())
            {
                final BindingSet bindingSet = queryResult.next();
                
                this.log.info("nextBinding: {}", bindingSet);
                
                // Assert.assertTrue(bindingSet.hasBinding("parent"));
                
                Assert.assertTrue(bindingSet.hasBinding("childCount"));
                
                final Literal value = (Literal)bindingSet.getBinding("childCount").getValue();
                
                Assert.assertEquals(1448, value.intValue());
                
                // FIXME: This is failing for this query for some reason
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
        
        final Set<Set<OWLAxiom>> exp =
                expGen.getSubClassExplanations(this.bractStomatalComplex, this.phylomeStomatalComplex);
        Assert.assertFalse(exp.isEmpty());
        out.println("Why is " + this.bractStomatalComplex + " subclass of " + this.phylomeStomatalComplex + "?");
        renderer.render(exp);
        
        final Set<Set<OWLAxiom>> exp2 =
                expGen.getSubClassExplanations(this.bractStomatalComplex, this.plantAnatomicalEntity);
        Assert.assertFalse(exp2.isEmpty());
        out.println("Why is " + this.bractStomatalComplex + " subclass of " + this.plantAnatomicalEntity + "?");
        renderer.render(exp2);
        
        renderer.endRendering();
    }
    
    @Test
    public final void testTriplesWithInferredSpecific() throws Exception
    {
        final GraphQuery query =
                this.getTestRepositoryConnection()
                        .prepareGraphQuery(
                                QueryLanguage.SPARQL,
                                "CONSTRUCT { ?parent <http://purl.org/oas/ontology#hasChildLink> ?child . } WHERE { ?child a <http://www.w3.org/2002/07/owl#Class> . ?parent a <http://www.w3.org/2002/07/owl#Class> . ?child <http://www.w3.org/2000/01/rdf-schema#subClassOf>+ ?parent . FILTER(isIRI(?child) && isIRI(?parent)) } ");
        
        final DatasetImpl testDataset = new DatasetImpl();
        testDataset.addDefaultGraph(this.testContextUri);
        testDataset.addDefaultGraph(this.testInferredContextUri);
        
        query.setDataset(testDataset);
        
        query.clearBindings();
        query.setBinding("parent", this.getTestValueFactory().createURI("http://purl.obolibrary.org/obo/PO_0025215"));
        
        final GraphQueryResult queryResult = query.evaluate();
        
        final Set<Statement> resultStatements = new LinkedHashSet<Statement>();
        
        try
        {
            Assert.assertTrue(queryResult.hasNext());
            
            while(queryResult.hasNext())
            {
                final Statement statement = queryResult.next();
                
                if(resultStatements.add(statement))
                {
                    this.log.info("nextStatement: {}", statement);
                }
                else
                {
                    this.log.info("Ignored duplicate statement: {}", statement);
                }
            }
        }
        finally
        {
            queryResult.close();
        }
        
        Assert.assertEquals(11, resultStatements.size());
    }
    
    @Test
    public final void testTuplesWithInferredSpecific() throws Exception
    {
        final TupleQuery query =
                this.getTestRepositoryConnection()
                        .prepareTupleQuery(
                                QueryLanguage.SPARQL,
                                "SELECT ?parent ?child WHERE { ?parent a <http://www.w3.org/2002/07/owl#Class> . ?parent <http://www.w3.org/2000/01/rdf-schema#subClassOf>+ ?child . FILTER(isIRI(?child) && isIRI(?parent)) } ");
        
        final DatasetImpl testDataset = new DatasetImpl();
        testDataset.addDefaultGraph(this.testContextUri);
        testDataset.addDefaultGraph(this.testInferredContextUri);
        
        query.setDataset(testDataset);
        
        query.clearBindings();
        query.setBinding("parent", this.getTestValueFactory().createURI("http://purl.obolibrary.org/obo/PO_0025215"));
        
        final TupleQueryResult queryResult = query.evaluate();
        
        final AtomicInteger bindingCount = new AtomicInteger(0);
        
        try
        {
            Assert.assertTrue(queryResult.hasNext());
            
            while(queryResult.hasNext())
            {
                final BindingSet bindingSet = queryResult.next();
                
                bindingCount.incrementAndGet();
                
                if(this.log.isInfoEnabled())
                {
                    for(final Binding nextBinding : bindingSet)
                    {
                        this.log.info("nextBinding name=" + nextBinding.getName() + " value="
                                + nextBinding.getValue().stringValue());
                    }
                }
            }
        }
        finally
        {
            queryResult.close();
        }
        
        Assert.assertEquals(20, bindingCount.get());
    }
    
    @Test
    public final void testTuplesWithInferredSpecificDistinct() throws Exception
    {
        final TupleQuery query =
                this.getTestRepositoryConnection()
                        .prepareTupleQuery(
                                QueryLanguage.SPARQL,
                                "SELECT DISTINCT ?parent ?child WHERE { ?parent a <http://www.w3.org/2002/07/owl#Class> . ?parent <http://www.w3.org/2000/01/rdf-schema#subClassOf>+ ?child . FILTER(isIRI(?child) && isIRI(?parent)) } ");
        
        final DatasetImpl testDataset = new DatasetImpl();
        testDataset.addDefaultGraph(this.testContextUri);
        testDataset.addDefaultGraph(this.testInferredContextUri);
        
        query.setDataset(testDataset);
        
        query.clearBindings();
        query.setBinding("parent", this.getTestValueFactory().createURI("http://purl.obolibrary.org/obo/PO_0025215"));
        
        final TupleQueryResult queryResult = query.evaluate();
        
        final AtomicInteger bindingCount = new AtomicInteger(0);
        
        try
        {
            Assert.assertTrue(queryResult.hasNext());
            
            while(queryResult.hasNext())
            {
                final BindingSet bindingSet = queryResult.next();
                
                bindingCount.incrementAndGet();
                
                if(this.log.isInfoEnabled())
                {
                    for(final Binding nextBinding : bindingSet)
                    {
                        this.log.info("nextBinding name=" + nextBinding.getName() + " value="
                                + nextBinding.getValue().stringValue());
                    }
                }
            }
        }
        finally
        {
            queryResult.close();
        }
        
        Assert.assertEquals(8, bindingCount.get());
    }
    
    @Test
    public final void testWithInferred() throws Exception
    {
        final TupleQuery query =
                this.getTestRepositoryConnection()
                        .prepareTupleQuery(
                                QueryLanguage.SPARQL,
                                "SELECT ?class ?subclassof WHERE { ?class a <http://www.w3.org/2002/07/owl#Class> . ?class <http://www.w3.org/2000/01/rdf-schema#subClassOf>+ ?subclassof . FILTER(isIRI(?class) && isIRI(?subclassof)) }");
        
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
                
                if(this.log.isTraceEnabled())
                {
                    for(final Binding nextBinding : bindingSet)
                    {
                        this.log.trace("nextBinding name=" + nextBinding.getName() + " value="
                                + nextBinding.getValue().stringValue());
                    }
                }
            }
        }
        finally
        {
            queryResult.close();
        }
        
        Assert.assertEquals(18832, bindingCount.get());
    }
    
    @Test
    public final void testWithoutInferred() throws Exception
    {
        final TupleQuery query =
                this.getTestRepositoryConnection()
                        .prepareTupleQuery(
                                QueryLanguage.SPARQL,
                                "SELECT ?class ?subclassof WHERE { ?class a <http://www.w3.org/2002/07/owl#Class> . ?class <http://www.w3.org/2000/01/rdf-schema#subClassOf>+ ?subclassof . FILTER(isIRI(?class) && isIRI(?subclassof)) }");
        
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
                
                if(this.log.isTraceEnabled())
                {
                    for(final Binding nextBinding : bindingSet)
                    {
                        this.log.trace("nextBinding name=" + nextBinding.getName() + " value="
                                + nextBinding.getValue().stringValue());
                    }
                }
            }
        }
        finally
        {
            queryResult.close();
        }
        
        Assert.assertEquals(6568, bindingCount.get());
    }
    
}
