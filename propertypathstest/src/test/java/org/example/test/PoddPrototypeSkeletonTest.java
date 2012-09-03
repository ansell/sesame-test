/**
 * 
 */
package org.example.test;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.repository.util.RDFInserter;
import org.semanticweb.owlapi.formats.RDFXMLOntologyFormatFactory;
import org.semanticweb.owlapi.io.StreamDocumentSource;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyManagerFactoryRegistry;
import org.semanticweb.owlapi.model.SetOntologyID;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactoryRegistry;
import org.semanticweb.owlapi.rio.RioRenderer;
import org.semanticweb.owlapi.util.InferredOntologyGenerator;

import com.clarkparsia.owlapiv3.OWL;

/**
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 */
public class PoddPrototypeSkeletonTest extends AbstractSesameTest
{
    
    private OWLOntologyManager manager;
    private OWLClass phylomeStomatalComplex;
    private OWLClass bractStomatalComplex;
    private OWLClass plantAnatomicalEntity;
    private OWLClass phylome;
    private OWLOntology initialSchemaOntology;
    private URI testContextUri;
    private URI testInferredContextUri;
    private OWLReasoner reasoner;
    
    @Before
    public void setUp() throws Exception
    {
        super.setUp();
        
        // create the manager to use for the test
        this.manager = OWLOntologyManagerFactoryRegistry.createOWLOntologyManager();
        
        // create owl class objects to use in queries
        this.phylomeStomatalComplex = OWL.Class(IRI.create("http://purl.obolibrary.org/obo/PO_0025215"));
        this.bractStomatalComplex = OWL.Class(IRI.create("http://purl.obolibrary.org/obo/PO_0025216"));
        this.plantAnatomicalEntity = OWL.Class(IRI.create("http://purl.obolibrary.org/obo/PO_0025131"));
        this.phylome = OWL.Class(IRI.create("http://purl.obolibrary.org/obo/PO_0006001"));
        
        // create URI references for the SPARQL Graph URIs for the ontology and the inferred
        // ontology statements respectively
        this.testContextUri = this.getTestValueFactory().createURI("urn:test:plantontology:context");
        this.testInferredContextUri = this.getTestValueFactory().createURI("urn:test:plantontology:inferred:context");
        
        // load the ontology from an RDF/XML file
        this.initialSchemaOntology =
                this.manager.loadOntologyFromOntologyDocument(new StreamDocumentSource(this.getClass()
                        .getResourceAsStream("/plant_ontology-v16.owl"), new RDFXMLOntologyFormatFactory()));
        Assert.assertFalse(this.initialSchemaOntology.isEmpty());
        
        // fix up the ontology id that was parsed using the values that we wish to use here
        OWLOntologyID fullId =
                new OWLOntologyID(this.initialSchemaOntology.getOntologyID().getOntologyIRI(),
                        IRI.create("urn:test:plantontology:version:16.0"));
        this.manager.applyChange(new SetOntologyID(initialSchemaOntology, fullId));
        Assert.assertEquals(fullId, this.initialSchemaOntology.getOntologyID());
        
        // Create an RDFHandler that will insert all triples after they are emitted from OWLAPI into
        // a single context in the Sesame Repository
        final RDFInserter repositoryHandler = new RDFInserter(this.getTestRepositoryConnection());
        repositoryHandler.enforceContext(this.testContextUri);
        
        // Render the triples out from OWLAPI into a Sesame Repository
        final RioRenderer renderer =
                new RioRenderer(this.initialSchemaOntology, this.manager, repositoryHandler, null, this.testContextUri);
        renderer.render();
        
        // Commit the connection and test to ensure that the expected number of triples were added,
        // and ensure that no other triples are in the repository at this point
        this.getTestRepositoryConnection().commit();
        Assert.assertEquals(44333, this.getTestRepositoryConnection().size(this.testContextUri));
        Assert.assertEquals(44333, this.getTestRepositoryConnection().size());
        
        // create an OWL Reasoner using the Pellet library and ensure that the reasoner thinks the
        // ontology is consistent so far
        final String reasonerName = "Pellet";
        final OWLReasonerFactory configuredReasoner =
                OWLReasonerFactoryRegistry.getInstance().getReasonerFactory(reasonerName);
        Assert.assertNotNull("Could not find reasoner", configuredReasoner);
        this.reasoner = configuredReasoner.createReasoner(this.initialSchemaOntology);
        Assert.assertTrue("Ontology was not consistent", this.reasoner.isConsistent());
        
        // use the reasoner to infer concrete axioms and deposit them in a new ontology
        this.reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
        final InferredOntologyGenerator iog = new InferredOntologyGenerator(this.reasoner);
        final OWLOntology inferredAxiomsOntology =
                this.manager.createOntology(new OWLOntologyID(IRI.create(this.testInferredContextUri), IRI
                        .create("urn:test:inferredontology:plantontology:version:16.0")));
        iog.fillOntology(this.manager, inferredAxiomsOntology);
        
        // Dump the triples from the inferred axioms into a separate SPARQL Graph/Context in the
        // Sesame Repository
        final RDFInserter inferredRepositoryHandler = new RDFInserter(this.getTestRepositoryConnection());
        inferredRepositoryHandler.enforceContext(this.testInferredContextUri);
        
        final RioRenderer inferencesRenderer =
                new RioRenderer(inferredAxiomsOntology, this.manager, inferredRepositoryHandler, null,
                        this.testInferredContextUri);
        inferencesRenderer.render();
        
        // verify that the triples were inserted into the repository correctly
        this.getTestRepositoryConnection().commit();
        Assert.assertEquals(2995, this.getTestRepositoryConnection().size(this.testInferredContextUri));
        
        if(this.log.isTraceEnabled())
        {
            for(final Statement nextStatement : this.getTestRepositoryConnection()
                    .getStatements(null, null, null, true, this.testInferredContextUri).asList())
            {
                this.log.trace(nextStatement.toString());
            }
        }
        
        // TODO:
        
        OWLOntology updatedSchemaOntology = this.manager.createOntology(new OWLOntologyID(this.initialSchemaOntology.getOntologyID().getOntologyIRI(), IRI.create("urn:test:plantontology:version:16.1")));
        this.manager.addAxioms(updatedSchemaOntology, initialSchemaOntology.getAxioms());
        
        
        // Load a set of objects in as an ontology that imports the plant ontology into the system
        // and verify that it is consistent
        
        // TODO: Decide on a consistent strategy for linking the object with the version of the
        // ontology.
        // OWL:IMPORTS will work but we will need to know when and how to update the version, so it
        // may be useful to create another ontology annotation property to detail the current
        // version in use so we can query directly for managed ontologies
        
        // Make a change to the objects and store the resulting ontology as a new version of the
        // objects ontology that links to the first version
        
        // Update the plant ontology with a new axiom/class/property and store it and an inferred
        // ontology along with it
        
        // Make another change to the object to use the new axiom/class/property and verify that it
        // is consistent and references the new version
        
        // Verify that the original version can be loaded and references the first version of the
        // ontology schema
    }
    
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
    public final void test()
    {
        
    }
    
}
