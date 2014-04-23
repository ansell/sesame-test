/**
 * 
 */
package org.example.sesame.java8;

import static org.junit.Assert.*;

import java.util.Set;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.Literal;
import org.openrdf.model.Model;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.util.Literals;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.Rio;

/**
 * @author ans025
 *
 */
public class SesameJava8Test {

	private Model model;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		model = Rio.parse(this.getClass().getResourceAsStream("/test.nt"), "",
				RDFFormat.NTRIPLES);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		model.clear();
		model = null;
	}

	@Test
	public final void testLanguageLiterals() {
		Set<String> languageTags = model
				.stream()
				.filter((Statement st) -> (st.getObject() instanceof Literal && Literals
						.isLanguageLiteral((Literal) st.getObject())))
				.map(st -> ((Literal) st.getObject()).getLanguage())
				.collect(Collectors.toSet());

		System.out.println(String.format("Languages in test set: %s",
				String.join(", ", languageTags)));

		assertEquals(2, languageTags.size());
	}

}
