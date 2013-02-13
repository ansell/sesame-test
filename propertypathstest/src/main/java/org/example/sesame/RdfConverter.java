package org.example.sesame;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.openrdf.OpenRDFException;
import org.openrdf.model.Namespace;
import org.openrdf.model.impl.NamespaceImpl;
import org.openrdf.model.vocabulary.DC;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;

public class RdfConverter
{
    public static void convert(final Writer writer, final InputStream inputStream, final String fileName,
            final String inputMimeType, final String baseURI, final String outputMimeType, List<Namespace> namespaces)
        throws OpenRDFException, IOException
    {
        RDFFormat rdfFormat = RDFFormat.RDFXML;
        if(inputMimeType != null)
        {
            rdfFormat = Rio.getParserFormatForMIMEType(inputMimeType, RDFFormat.RDFXML);
        }
        else if(fileName != null)
        {
            rdfFormat = Rio.getParserFormatForFileName(fileName, RDFFormat.RDFXML);
        }
        
        final RDFParser createParser = Rio.createParser(rdfFormat);
        
        RDFWriter rdfWriter =
                Rio.createWriter(Rio.getWriterFormatForMIMEType(outputMimeType, RDFFormat.RDFXML), writer);
        
        for(Namespace nextNamespace : namespaces)
        {
            rdfWriter.handleNamespace(nextNamespace.getPrefix(), nextNamespace.getName());
        }
        
        createParser.setRDFHandler(rdfWriter);
        createParser.parse(inputStream, baseURI);
    }
    
    /**
     * @param args
     */
    public static void main(final String[] args) throws Exception
    {
        InputStream resource = RdfConverter.class.getResourceAsStream("/bio2rdf-providers-release2-sparql.n3");
        
        Writer writer = new FileWriter("/home/peter/temp/bio2rdf-providers-release2-sparql.n3");
        
        List<Namespace> namespaces = new ArrayList<Namespace>();
        
        namespaces.add(new NamespaceImpl("rdf", RDF.NAMESPACE));
        namespaces.add(new NamespaceImpl("rdfs", RDFS.NAMESPACE));
        namespaces.add(new NamespaceImpl("dcterms", "http://purl.org/dc/terms/"));
        namespaces.add(new NamespaceImpl("xsd", "http://www.w3.org/2001/XMLSchema#"));
        
        namespaces.add(new NamespaceImpl("queryall_profile", "http://purl.org/queryall/profile:"));
        namespaces.add(new NamespaceImpl("queryall_provider", "http://purl.org/queryall/provider:"));
        
        namespaces.add(new NamespaceImpl("bio2rdf_provider", "http://bio2rdf.org/provider:"));
        namespaces.add(new NamespaceImpl("bio2rdf_query", "http://bio2rdf.org/query:"));
        namespaces.add(new NamespaceImpl("bio2rdf_rdfrule", "http://bio2rdf.org/rdfrule:"));
        namespaces.add(new NamespaceImpl("bio2rdf_ns", "http://bio2rdf.org/ns:"));
        
        namespaces.add(new NamespaceImpl("queryall", "http://purl.org/queryall/"));
        namespaces.add(new NamespaceImpl("bio2rdf", "http://bio2rdf.org/"));
        
        RdfConverter.convert(writer, resource, "bio2rdf-providers-release2-sparql.n3",
                RDFFormat.N3.getDefaultMIMEType(), "", RDFFormat.TURTLE.getDefaultMIMEType(), namespaces);
    }
    
}
