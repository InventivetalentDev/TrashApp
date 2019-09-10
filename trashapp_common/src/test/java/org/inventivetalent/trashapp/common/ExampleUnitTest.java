package org.inventivetalent.trashapp.common;

import org.inventivetalent.trashapp.common.db.Converters;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {

	@Test
	public void addition_isCorrect() {
		assertEquals(4, 2 + 2);
	}

	@Test
	public void convertersListToString() {
		List<String> list = Arrays.asList("lorem", "ipsum", "dolor", "sit", "amet", "consectetur", "adipiscing", "elit");
		String string = Converters.fromList(list);
		assertEquals("lorem,ipsum,dolor,sit,amet,consectetur,adipiscing,elit", string);
	}

	@Test
	public void convertersStringToList() {
		String string = "lorem,ipsum,dolor,sit,amet,consectetur,adipiscing,elit";
		List<String> list = Converters.fromString(string);
		assertArrayEquals(new String[] {
				"lorem",
				"ipsum",
				"dolor",
				"sit",
				"amet",
				"consectetur",
				"adipiscing",
				"elit" }, list.toArray(new String[0]));
	}

	@Test
	public void issueUriTest() {
		System.out.println(Util.createPrefilledIssueUri(null));
	}

	@Test
	public void splitterTestSemicolon() {
		String test = "trash;cigarettes";
		String[] split = test.split(Util.SPLIT_SEMICOLON_OR_COMMA);
		assertArrayEquals(new String[] {
				"trash",
				"cigarettes" }, split);
	}

	@Test
	public void splitterTestComma() {
		String test = "trash,cigarettes";
		String[] split = test.split(Util.SPLIT_SEMICOLON_OR_COMMA);
		assertArrayEquals(new String[] {
				"trash",
				"cigarettes" }, split);
	}

	@Test
	public void splitterTestCommaAndSemicolon() {
		String test = "trash,cigarettes;paper";
		String[] split = test.split(Util.SPLIT_SEMICOLON_OR_COMMA);
		assertArrayEquals(new String[] {
				"trash",
				"cigarettes",
				"paper" }, split);
	}

}