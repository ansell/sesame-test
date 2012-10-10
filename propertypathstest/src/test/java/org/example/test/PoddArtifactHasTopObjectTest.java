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
import org.junit.Ignore;
import org.junit.Test;
import org.mindswap.pellet.exceptions.InconsistentOntologyException;
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
import org.openrdf.query.algebra.evaluation.function.string.Contains;
import org.openrdf.query.impl.DatasetImpl;
import org.openrdf.repository.util.RDFInserter;
import org.semanticweb.owlapi.formats.RDFXMLOntologyFormatFactory;
import org.semanticweb.owlapi.formats.TurtleOntologyFormatFactory;
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
public class PoddArtifactHasTopObjectTest extends AbstractSesameTest
{
    private OWLOntologyManager manager;
    private OWLOntology parsedOntology;
    private OWLReasoner reasoner;
    
    private URI testContextUri;
    private URI testInferredContextUri;
    
    private OWLClass myTopObject;
    
    /**
     * @throws java.lang.Exception
     */
    @Override
    @Before
    public void setUp() throws Exception
    {
        super.setUp();
        
        this.manager = OWLOntologyManagerFactoryRegistry.createOWLOntologyManager();
        
        this.myTopObject = OWL.Class(IRI.create("urn:test:#MyTopObject"));
        
        this.parsedOntology =
                this.manager.loadOntologyFromOntologyDocument(new StreamDocumentSource(this.getClass()
                        .getResourceAsStream("/podd-artifact-has-top-object.ttl"), new TurtleOntologyFormatFactory()));
        
        Assert.assertFalse(this.parsedOntology.isEmpty());
        
        this.testContextUri = this.getTestValueFactory().createURI("urn:test:myontology:context");
        this.testInferredContextUri = this.getTestValueFactory().createURI("urn:test:myontology:inferred:context");
        
        final RDFInserter repositoryHandler = new RDFInserter(this.getTestRepositoryConnection());
        repositoryHandler.enforceContext(this.testContextUri);
        
        final RioRenderer renderer =
                new RioRenderer(this.parsedOntology, this.manager, repositoryHandler, null, this.testContextUri);
        renderer.render();
        this.getTestRepositoryConnection().commit();
        
        Assert.assertEquals(17, this.getTestRepositoryConnection().size(this.testContextUri));
        
        final String reasonerName = "Pellet";
        final OWLReasonerFactory configuredReasoner =
                OWLReasonerFactoryRegistry.getInstance().getReasonerFactory(reasonerName);
        
        Assert.assertNotNull("Could not find reasoner", configuredReasoner);
        
        this.reasoner = configuredReasoner.createReasoner(this.parsedOntology);
        // FIXME: We would like to have the following statement fail ideally.
        Assert.assertFalse("Ontology was not consistent", this.reasoner.isConsistent());
        
        try
        {
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
            
            Assert.assertEquals(16, this.getTestRepositoryConnection().size(this.testInferredContextUri));
            
            if(this.log.isTraceEnabled())
            {
                for(final Statement nextStatement : this.getTestRepositoryConnection()
                        .getStatements(null, null, null, true, this.testInferredContextUri).asList())
                {
                    this.log.trace(nextStatement.toString());
                }
            }
            Assert.fail("Expected an exception for an inconsistent ontology");
        }
        catch(Exception ioe)
        {
            Assert.assertTrue(ioe.getMessage().contains("Reason for inconsistency"));
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
    
    @Ignore
    @Test
    public final void testClassHierarchyRenderingSubDirect() throws Exception
    {
        final Set<OWLClass> flattened = this.reasoner.getSubClasses(this.myTopObject, true).getFlattened();
        
        for(final OWLClass nextClass : flattened)
        {
            System.out.println(nextClass);
        }
        
        Assert.assertEquals(1, flattened.size());
    }
    
    @Ignore
    @Test
    public final void testClassHierarchyRenderingSubDirectTopLevel() throws Exception
    {
        final Set<OWLClass> flattened = this.reasoner.getSubClasses(OWL.Thing, true).getFlattened();
        
        for(final OWLClass nextClass : flattened)
        {
            System.out.println(nextClass);
        }
        
        Assert.assertEquals(1, flattened.size());
    }
    
    @Ignore
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
        
        Assert.assertEquals(1, flattened.size());
    }
    
    @Ignore
    @Test
    public final void testClassHierarchyRenderingSubNotDirect() throws Exception
    {
        final Set<OWLClass> flattened = this.reasoner.getSubClasses(this.myTopObject, false).getFlattened();
        
        for(final OWLClass nextClass : flattened)
        {
            System.out.println(nextClass);
        }
        
        Assert.assertEquals(1, flattened.size());
    }
    
    @Ignore
    @Test
    public final void testClassHierarchyRenderingSubNotDirectTopLevel() throws Exception
    {
        final Set<OWLClass> flattened = this.reasoner.getSubClasses(OWL.Thing, false).getFlattened();
        
        for(final OWLClass nextClass : flattened)
        {
            System.out.println(nextClass);
        }
        
        Assert.assertEquals(4, flattened.size());
    }
    
    @Ignore
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
        
        Assert.assertEquals(4, flattened.size());
    }
    
    /**
     * Code originally from
     * pellet/example/src/test/java/org/mindswap/pellet/examples/ExplanationExample.java
     * 
     * @throws Exception
     */
    @Ignore
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
        
        // final Set<Set<OWLAxiom>> exp =
        // expGen.getSubClassExplanations(this.bractStomatalComplex, this.phylomeStomatalComplex);
        // Assert.assertFalse(exp.isEmpty());
        // out.println("Why is " + this.bractStomatalComplex + " subclass of " +
        // this.phylomeStomatalComplex + "?");
        // renderer.render(exp);
        //
        // final Set<Set<OWLAxiom>> exp2 =
        // expGen.getSubClassExplanations(this.bractStomatalComplex, this.plantAnatomicalEntity);
        // Assert.assertFalse(exp2.isEmpty());
        // out.println("Why is " + this.bractStomatalComplex + " subclass of " +
        // this.plantAnatomicalEntity + "?");
        // renderer.render(exp2);
        
        renderer.endRendering();
    }
    
    @Ignore
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
    
}
