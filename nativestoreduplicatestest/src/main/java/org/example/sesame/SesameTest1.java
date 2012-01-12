/**
 * 
 */
package org.example.sesame;

import java.io.File;
import java.io.IOException;

import org.openrdf.OpenRDFException;
import org.openrdf.model.URI;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.sail.inferencer.fc.ForwardChainingRDFSInferencer;
import org.openrdf.sail.nativerdf.NativeStore;

/**
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 */
public class SesameTest1
{
    public static void main(final String[] args)
    {
        // creating and initializing the repository
        final File repoFile = new File("/tmp/sesamenativestoretest/");
        final Repository repository = new SailRepository(new ForwardChainingRDFSInferencer(new NativeStore(repoFile)));
        
        try
        {
            repository.initialize();
        }
        catch(final RepositoryException e)
        {
            throw new RuntimeException(e);
        }
        
        RepositoryConnection con = null;
        
        // now adding the file
        // this snippet is repeated 2 times to check if the repository stores
        // duplicates.
        try
        {
            con = repository.getConnection();
            
            final URI contextUri = con.getValueFactory().createURI("http://example.org/test/context");
            
            // i used the filename as the context, the file format is N3
            con.add(SesameTest1.class.getResourceAsStream("/test1.n3"), "http://test.example.org/", RDFFormat.N3, contextUri);
            
            con.commit();
            
            System.out.println("size(context)="+con.size(contextUri));
            System.out.println("size()="+con.size());

            // i used the filename as the context, the file format is N3
            con.add(SesameTest1.class.getResourceAsStream("/test1.n3"), "http://test.example.org/", RDFFormat.N3, contextUri);
            
            con.commit();
            
            System.out.println("size(context)="+con.size(contextUri));
            System.out.println("size()="+con.size());
        }
        catch(final OpenRDFException e)
        {
            throw new RuntimeException(e);
        }
        catch(final IOException e)
        {
            throw new RuntimeException(e);
        }
        finally
        {
            if(con != null)
            {
                try
                {
                    con.close();
                }
                catch(RepositoryException e)
                {
                    System.err.println("Error closing repository connection e="+e.getMessage());
                }
            }
        }
    }
}
