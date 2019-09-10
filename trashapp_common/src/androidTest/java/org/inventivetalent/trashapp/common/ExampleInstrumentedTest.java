package org.inventivetalent.trashapp.common;

import android.content.Context;
import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
	@Test
	public void useAppContext() {
		// Context of the app under test.
		Context appContext = InstrumentationRegistry.getTargetContext();

		assertEquals("org.inventivetalent.trashapp.common.test", appContext.getPackageName());
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
