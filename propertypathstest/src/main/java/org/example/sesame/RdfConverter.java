package org.example.sesame;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;

import org.openrdf.OpenRDFException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;

public class RdfConverter
{
    public static void convert(Writer writer, InputStream inputStream, String fileName, String inputMimeType, String baseURI, String outputMimeType) throws OpenRDFException, IOException
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
        
        RDFParser createParser = Rio.createParser(rdfFormat);
        
        
        createParser.setRDFHandler(Rio.createWriter(Rio.getWriterFormatForMIMEType(outputMimeType, RDFFormat.RDFXML), writer));
        createParser.parse(inputStream, baseURI);
    }
    
    /**
     * @param args
     */
    public static void main(String[] args)
    {
        
    }
    
}
