package mdn.jh.automation.webserver;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.json.JSONArray;
import org.json.JSONObject;

/** Persistent shared page definitions for the automation canvas. */
public final class AutomationPageStore {
	public static final String DEFAULT_PAGE_ID = "automation-page-1";
	private static final Path FILE = Path.of("automation-pages.json");
	private static final int MAX_PAGES = 50;

	private AutomationPageStore() { }

	public static synchronized JSONArray load() throws Exception {
		if (!Files.exists(FILE)) return defaults();
		return validate(new JSONObject(Files.readString(FILE, StandardCharsets.UTF_8)).optJSONArray("pages"));
	}

	public static synchronized JSONArray save(JSONArray pages) throws Exception {
		JSONArray validated = validate(pages);
		Path temporary = Path.of(FILE + ".tmp");
		Files.writeString(temporary, new JSONObject().put("pages", validated).toString(2), StandardCharsets.UTF_8);
		try { Files.move(temporary, FILE, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE); }
		catch (IOException unsupported) { Files.move(temporary, FILE, StandardCopyOption.REPLACE_EXISTING); }
		return validated;
	}

	public static boolean contains(JSONArray pages, String id) {
		for (int i = 0; i < pages.length(); i++) if (pages.optJSONObject(i) != null && id.equals(pages.optJSONObject(i).optString("id"))) return true;
		return false;
	}

	private static JSONArray validate(JSONArray source) throws org.json.JSONException {
		if (source == null || source.length() == 0) return defaults();
		if (source.length() > MAX_PAGES) throw new IllegalArgumentException("Too many automation pages");
		JSONArray result = new JSONArray();
		for (int i = 0; i < source.length(); i++) {
			JSONObject page = source.getJSONObject(i);
			String id = page.optString("id", "").trim(), name = page.optString("name", "").trim();
			if (id.isEmpty() || id.length() > 80 || contains(result, id)) throw new IllegalArgumentException("Invalid or duplicate page ID");
			if (name.isEmpty()) name = "Page " + (i + 1);
			result.put(new JSONObject().put("id", id).put("name", name.substring(0, Math.min(100, name.length()))));
		}
		return result;
	}

	private static JSONArray defaults() throws org.json.JSONException {
		return new JSONArray().put(new JSONObject().put("id", DEFAULT_PAGE_ID).put("name", "Automation"));
	}
}
