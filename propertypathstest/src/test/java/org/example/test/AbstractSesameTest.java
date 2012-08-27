package org.example.test;

import org.junit.After;
import org.junit.Before;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */

/**
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 */
public class AbstractSesameTest
{
    protected static Logger log = LoggerFactory.getLogger(AbstractSesameTest.class);
    
    private Repository testRepository;
    
    private ValueFactory testValueFactory;
    
    private RepositoryConnection testRepositoryConnection;
    
    /**
     * @return the testRepository
     */
    public Repository getTestRepository()
    {
        return this.testRepository;
    }
    
    /**
     * @return the testRepositoryConnection
     */
    public RepositoryConnection getTestRepositoryConnection()
    {
        return this.testRepositoryConnection;
    }
    
    /**
     * @return the testValueFactory
     */
    public ValueFactory getTestValueFactory()
    {
        return this.testValueFactory;
    }
    
    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception
    {
        this.testRepository = new SailRepository(new MemoryStore());
        this.testRepository.initialize();
        
        this.testValueFactory = this.testRepository.getValueFactory();
        
        this.testRepositoryConnection = this.testRepository.getConnection();
        this.testRepositoryConnection.setAutoCommit(false);
    }
    
    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception
    {
        if(this.testRepositoryConnection != null)
        {
            try
            {
                this.testRepositoryConnection.close();
            }
            catch(final RepositoryException e)
            {
                AbstractSesameTest.log.error("Test repository connection could not be closed", e);
            }
        }
        
        this.testRepositoryConnection = null;
        
        this.testValueFactory = null;
        
        if(this.testRepository != null)
        {
            try
            {
                this.testRepository.shutDown();
            }
            catch(final RepositoryException e)
            {
                AbstractSesameTest.log.error("Test repository could not be shutdown", e);
            }
        }
        
        this.testRepository = null;
    }
    
}
