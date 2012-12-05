/**
 * 
 */
package org.example.test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.Assert;
import org.junit.Test;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.StatementCollector;

/**
 * Test for fix to SES-666 to wrap up the Content is not allowed in prolog message that was
 * previously being printed to System.err.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 */
public class RDFXMLSystemErrPrintTest extends AbstractSesameTest
{
    @Test
    public void testRDFXMLSystemErrPrint() throws Exception
    {
        PrintStream priorErr = System.err;
        try
        {
            // Override System.err with our own printstream
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            System.setErr(new PrintStream(outputStream, true, "UTF-8"));
            
            RDFParser rdfParser = Rio.createParser(RDFFormat.RDFXML);
            RDFHandler handler = new StatementCollector();
            rdfParser.setRDFHandler(handler);
            
            rdfParser.parse(this.getClass().getResourceAsStream("/testBlankNodes.ttl"), "");
            
            Assert.assertEquals("System.err was printed to during the parse process", 0, outputStream.size());
        }
        catch(RDFParseException rdfpe)
        {
            Assert.assertEquals("Content is not allowed in prolog. [line 1, column 1]", rdfpe.getMessage());
        }
        finally
        {
            // Reset the System.err to whatever it was previously
            System.setErr(priorErr);
        }
    }
}
