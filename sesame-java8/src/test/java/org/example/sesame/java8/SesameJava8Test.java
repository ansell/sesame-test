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
import org.openrdf.model.BNode;
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
	public final void testURISubjects() {
		Set<URI> uriSubjects = model.stream()
				.filter((Statement st) -> st.getSubject() instanceof URI)
				.map(st -> (URI) st.getSubject()).collect(Collectors.toSet());

		assertEquals(27, uriSubjects.size());
	}

	@Test
	public final void testBlankNodeSubjects() {
		Set<BNode> bNodeSubjects = model.stream()
				.filter((Statement st) -> st.getSubject() instanceof BNode)
				.map(st -> (BNode) st.getSubject()).collect(Collectors.toSet());

		assertEquals(1, bNodeSubjects.size());
	}

	@Test
	public final void testPredicates() {
		Set<URI> predicates = model.stream().map(st -> st.getPredicate())
				.collect(Collectors.toSet());

		assertEquals(1, predicates.size());
	}

	@Test
	public final void testBlankNodeObjects() {
		Set<BNode> bNodeObjects = model.stream()
				.filter((Statement st) -> st.getObject() instanceof BNode)
				.map(st -> (BNode) st.getObject()).collect(Collectors.toSet());

		assertEquals(1, bNodeObjects.size());
	}

	@Test
	public final void testNonLanguageLiterals() {
		Set<Literal> nonLanguageLiterals = model
				.stream()
				.filter((Statement st) -> (st.getObject() instanceof Literal && !Literals
						.isLanguageLiteral((Literal) st.getObject())))
				.map(st -> (Literal) st.getObject())
				.collect(Collectors.toSet());

		assertEquals(19, nonLanguageLiterals.size());
	}

	@Test
	public final void testNonLanguageLiteralDatatypes() {
		Set<URI> nonLanguageLiteralDatatypes = model
				.stream()
				.filter((Statement st) -> (st.getObject() instanceof Literal && !Literals
						.isLanguageLiteral((Literal) st.getObject())))
				.map(st -> ((Literal) st.getObject()).getDatatype())
				.collect(Collectors.toSet());

		assertEquals(3, nonLanguageLiteralDatatypes.size());
	}

	@Test
	public final void testLanguageLiterals() {
		Set<Literal> languageLiterals = model
				.stream()
				.filter((Statement st) -> (st.getObject() instanceof Literal && Literals
						.isLanguageLiteral((Literal) st.getObject())))
				.map(st -> (Literal) st.getObject())
				.collect(Collectors.toSet());

		assertEquals(2, languageLiterals.size());
	}

	@Test
	public final void testLanguageTags() {
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
