package org.inventivetalent.trashapp.common;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.inventivetalent.trashapp.common.db.Converters;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

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

	@Test
	public void osmBridgeJsonTest() {
		OsmBridgeClient.PendingTrashcan pendingTrashcan = new OsmBridgeClient.PendingTrashcan(20, 30, "idk");
		assertEquals(20, pendingTrashcan.lat, 0.01);
		assertEquals(30, pendingTrashcan.lon, 0.01);
		assertEquals("idk", pendingTrashcan.amenity);

		JsonElement json = new Gson().toJsonTree(pendingTrashcan);
		assertTrue(json.isJsonObject());
		assertTrue(((JsonObject) json).has("lat"));
		assertTrue(((JsonObject) json).has("lon"));
		assertTrue(((JsonObject) json).has("amenity"));
	}

}