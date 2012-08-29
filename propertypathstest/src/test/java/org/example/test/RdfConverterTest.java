package org.example.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.example.sesame.RdfConverter;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.OpenRDFException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 */
public class RdfConverterTest
{
    private static final Logger LOGGER = LoggerFactory.getLogger(RdfConverterTest.class);
    
    private StringWriter testWriter;
    private InputStream testInputStream;
    private String testFileName;
    private String testInputMimeType;
    private String testBaseURI;
    private String testOutputMimeType;
    
    @Before
    public void setUp() throws Exception
    {
    }
    
    @After
    public void tearDown() throws Exception
    {
    }
    
    @Test
    public void testConvertDcAm() throws OpenRDFException, IOException
    {
        this.testWriter = new StringWriter();
        this.testFileName = "/dcam.ttl";
        this.testInputMimeType = "text/turtle";
        this.testBaseURI = "http://purl.org/NET/dc_owl2dl/dcam";
        this.testOutputMimeType = "application/rdf+xml";
        this.testInputStream = this.getClass().getResourceAsStream(this.testFileName);
        
        Assert.assertNotNull(this.testInputStream);
        
        RdfConverter.convert(this.testWriter, this.testInputStream, this.testFileName, this.testInputMimeType,
                this.testBaseURI, this.testOutputMimeType);
        
        RdfConverterTest.LOGGER.info("output=");
        RdfConverterTest.LOGGER.info(this.testWriter.toString());
    }
    
    @Test
    public void testConvertDcmiType() throws OpenRDFException, IOException
    {
        this.testWriter = new StringWriter();
        this.testFileName = "/dcmitype.ttl";
        this.testInputMimeType = "text/turtle";
        this.testBaseURI = "http://purl.org/NET/dc_owl2dl/dcmitype";
        this.testOutputMimeType = "application/rdf+xml";
        this.testInputStream = this.getClass().getResourceAsStream(this.testFileName);
        
        Assert.assertNotNull(this.testInputStream);
        
        RdfConverter.convert(this.testWriter, this.testInputStream, this.testFileName, this.testInputMimeType,
                this.testBaseURI, this.testOutputMimeType);
        
        RdfConverterTest.LOGGER.info("output=");
        RdfConverterTest.LOGGER.info(this.testWriter.toString());
    }
    
    @Test
    public void testConvertDcTermsOd() throws OpenRDFException, IOException
    {
        this.testWriter = new StringWriter();
        this.testFileName = "/dcterms_od.ttl";
        this.testInputMimeType = "text/turtle";
        this.testBaseURI = "http://purl.org/NET/dc_owl2dl/terms_od";
        this.testOutputMimeType = "application/rdf+xml";
        this.testInputStream = this.getClass().getResourceAsStream(this.testFileName);
        
        Assert.assertNotNull(this.testInputStream);
        
        RdfConverter.convert(this.testWriter, this.testInputStream, this.testFileName, this.testInputMimeType,
                this.testBaseURI, this.testOutputMimeType);
        
        RdfConverterTest.LOGGER.info("output=");
        RdfConverterTest.LOGGER.info(this.testWriter.toString());
    }
    
    @Test
    public void testOntologyCompiler() throws Exception
    {
        final List<String> argsList = new ArrayList<String>();
        
        argsList.add("-j");
        argsList.add("target/scufl2-ontology.jar");
        argsList.add("src/test/resources/skos-owl1-dl.rdf");
        argsList.add("src/test/resources/dcterms_od.rdf");
        argsList.add("src/test/resources/dcam.rdf");
        argsList.add("src/test/resources/scufl2.rdf");
        
        // run the compiler using the arguments given above
        org.openrdf.repository.object.compiler.Compiler.main(argsList.toArray(new String[argsList.size()]));
        
        // Bug traced to line 103 of OntologyLoader.followImports in alibaba-2.0-beta14
        // if (!model.contains(null, null, null, uri)) {
        // should be
        // if (!model.contains(null, null, null, uri) && !model.contains(uri, RDF.TYPE,
        // OWL.ONTOLOGY)) {
        //
        // See bug report at: http://www.openrdf.org/issues/browse/ALI-13
    }
}
