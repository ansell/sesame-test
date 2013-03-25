package org.example.sesame;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openrdf.OpenRDFException;
import org.openrdf.model.Namespace;
import org.openrdf.model.impl.NamespaceImpl;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;
import org.semanticweb.owlapi.change.OWLOntologyChangeData;
import org.semanticweb.owlapi.formats.OBOOntologyFormatFactory;
import org.semanticweb.owlapi.io.StreamDocumentSource;
import org.semanticweb.owlapi.model.AddOntologyAnnotation;
import org.semanticweb.owlapi.model.AnnotationChange;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChangeVisitor;
import org.semanticweb.owlapi.model.OWLOntologyChangeVisitorEx;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyManagerFactoryRegistry;
import org.semanticweb.owlapi.profiles.OWL2DLProfile;
import org.semanticweb.owlapi.profiles.OWL2Profile;
import org.semanticweb.owlapi.profiles.OWLProfile;
import org.semanticweb.owlapi.profiles.OWLProfileRegistry;
import org.semanticweb.owlapi.profiles.OWLProfileReport;
import org.semanticweb.owlapi.profiles.OWLProfileViolation;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactoryRegistry;
import org.semanticweb.owlapi.rio.RioRenderer;

public class OBO2OWLConverter
{
    public static void convert(final Writer writer, final InputStream inputStream, final String fileName,
            final String inputMimeType, final String baseURI, final String outputMimeType, List<Namespace> namespaces)
        throws OpenRDFException, IOException, OWLOntologyCreationException
    {
        OWLOntologyManager manager = OWLOntologyManagerFactoryRegistry.createOWLOntologyManager();
        OWLDataFactory df = manager.getOWLDataFactory();
        OWLOntology ontology =
                manager.loadOntologyFromOntologyDocument(new StreamDocumentSource(inputStream,
                        new OBOOntologyFormatFactory()));
        
        List<String> missingAnnotationProperties =
                Arrays.asList("http://purl.obolibrary.org/obo/def",
                        "http://www.geneontology.org/formats/oboInOWL#xref",
                        "http://www.geneontology.org/formats/oboInOWL#hasRelatedSynonym");
        for(String nextMissingProperty : missingAnnotationProperties)
        {
            OWLAnnotationProperty owlProperty = df.getOWLAnnotationProperty(IRI.create(nextMissingProperty));
            OWLDeclarationAxiom declarationAxiom = df.getOWLDeclarationAxiom(owlProperty);
            manager.addAxiom(ontology, declarationAxiom);
        }
        
        List<String> missingObjectProperties = Arrays.asList("http://purl.obolibrary.org/obo/part_of");
        for(String nextMissingProperty : missingObjectProperties)
        {
            OWLObjectProperty owlProperty = df.getOWLObjectProperty(IRI.create(nextMissingProperty));
            OWLDeclarationAxiom declarationAxiom = df.getOWLDeclarationAxiom(owlProperty);
            manager.addAxiom(ontology, declarationAxiom);
        }
        
        // To add a specific annotation property and value use this
        // AddOntologyAnnotation change = new AddOntologyAnnotation(ontology,
        // df.getOWLAnnotation(owlAnnotationProperty, value))
        RDFWriter rdfWriter =
                Rio.createWriter(Rio.getWriterFormatForMIMEType(outputMimeType, RDFFormat.RDFXML), writer);
        
        for(Namespace nextNamespace : namespaces)
        {
            rdfWriter.handleNamespace(nextNamespace.getPrefix(), nextNamespace.getName());
        }
        
        OWLProfile profile = OWLProfileRegistry.getInstance().getProfile(OWL2Profile.OWL2_DL);
        
        OWLProfileReport profileReport = profile.checkOntology(ontology);
        
        if(profileReport.isInProfile())
        {
            System.out.println("Ontology is in OWL2 DL profile");
        }
        else
        {
            System.err.println("Ontology is NOT in OWL2 DL profile: violation count="
                    + profileReport.getViolations().size());
            for(OWLProfileViolation nextViolation : profileReport.getViolations())
            {
                System.err.println(nextViolation.toString());
            }
        }
        
        RioRenderer renderer = new RioRenderer(ontology, ontology.getOWLOntologyManager(), rdfWriter, null);
        renderer.render();
        
        OWLReasonerFactory reasonerFactory = OWLReasonerFactoryRegistry.getInstance().getReasonerFactory("Pellet");
        
        OWLReasoner reasoner = reasonerFactory.createReasoner(ontology);
        
        if(!reasoner.isConsistent())
        {
            System.err.println("Reasoner is NOT consistent");
        }
        else
        {
            System.out.println("Reasoner is consistent");
        }
        
    }
    
    /**
     * @param args
     */
    public static void main(final String[] args) throws Exception
    {
        InputStream resource = OBO2OWLConverter.class.getResourceAsStream("/crop-ontology-715.obo");
        
        Writer writer = new FileWriter("/home/peter/temp/crop-ontology-715.owl");
        
        List<Namespace> namespaces = new ArrayList<Namespace>();
        
        namespaces.add(RDF.NS);
        namespaces.add(RDFS.NS);
        namespaces.add(DCTERMS.NS);
        namespaces.add(XMLSchema.NS);
        namespaces.add(OWL.NS);
        
        OBO2OWLConverter.convert(writer, resource, "", null, "", RDFFormat.RDFXML.getDefaultMIMEType(), namespaces);
    }
    
}
