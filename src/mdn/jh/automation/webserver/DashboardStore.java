package mdn.jh.automation.webserver;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/** Stores and validates the shared web dashboard layout. */
public final class DashboardStore {
	private static final Path FILE = Path.of("dashboard-layout.json");
	private static final int MAX_PAGES = 50;
	private static final int MAX_ITEMS = 500;

	private DashboardStore() { }

	public static synchronized JSONObject load() throws IOException, JSONException {
		if (!Files.exists(FILE)) return emptyLayout();
		return validate(new JSONObject(Files.readString(FILE, StandardCharsets.UTF_8)));
	}

	public static synchronized JSONObject save(JSONObject layout) throws IOException, JSONException {
		JSONObject validated = validate(layout);
		Path temporary = Path.of(FILE.toString() + ".tmp");
		Files.writeString(temporary, validated.toString(2), StandardCharsets.UTF_8);
		try {
			Files.move(temporary, FILE, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
		} catch (IOException unsupportedAtomicMove) {
			Files.move(temporary, FILE, StandardCopyOption.REPLACE_EXISTING);
		}
		return validated;
	}

	private static JSONObject emptyLayout() throws JSONException {
		JSONObject layout = new JSONObject();
		JSONArray pages = new JSONArray();
		JSONObject page = new JSONObject();
		page.put("id", "page-1");
		page.put("name", "Overview");
		page.put("items", new JSONArray());
		pages.put(page);
		layout.put("pages", pages);
		return layout;
	}

	private static JSONObject validate(JSONObject input) throws JSONException {
		JSONArray sourcePages = input.optJSONArray("pages");
		if (sourcePages == null || sourcePages.length() == 0) return emptyLayout();
		if (sourcePages.length() > MAX_PAGES) throw new IllegalArgumentException("Too many dashboard pages");
		JSONObject result = new JSONObject();
		JSONArray pages = new JSONArray();
		int itemCount = 0;
		for (int pageIndex = 0; pageIndex < sourcePages.length(); pageIndex++) {
			JSONObject sourcePage = sourcePages.getJSONObject(pageIndex);
			JSONObject page = new JSONObject();
			page.put("id", text(sourcePage, "id", 80, "page-" + (pageIndex + 1)));
			page.put("name", text(sourcePage, "name", 100, "Page " + (pageIndex + 1)));
			JSONArray sourceItems = sourcePage.optJSONArray("items");
			JSONArray items = new JSONArray();
			if (sourceItems != null) for (int itemIndex = 0; itemIndex < sourceItems.length(); itemIndex++) {
				if (++itemCount > MAX_ITEMS) throw new IllegalArgumentException("Too many dashboard items");
				JSONObject sourceItem = sourceItems.getJSONObject(itemIndex);
				String type = sourceItem.optString("type", "");
				if (!"label".equals(type) && !"value".equals(type)
						&& !"pushbutton".equals(type) && !"switch".equals(type)) continue;
				JSONObject item = new JSONObject();
				item.put("id", text(sourceItem, "id", 80, "item-" + pageIndex + "-" + itemIndex));
				item.put("type", type);
				item.put("x", bounded(sourceItem.optInt("x", 20), 0, 10000));
				item.put("y", bounded(sourceItem.optInt("y", 20), 0, 10000));
				item.put("width", bounded(sourceItem.optInt("width", 220), 100, 2000));
				item.put("height", bounded(sourceItem.optInt("height", "label".equals(type) || "value".equals(type) ? 70 : 65), 45, 2000));
				if ("label".equals(type)) item.put("text", text(sourceItem, "text", 500, "Label"));
				else if ("value".equals(type)) {
					item.put("componentId", sourceItem.optInt("componentId", -1));
					item.put("scale", finiteNumber(sourceItem, "scale", 1.0));
					item.put("unit", text(sourceItem, "unit", 40, ""));
					String placement = sourceItem.optString("unitPlacement", "suffix");
					item.put("unitPlacement", "prefix".equals(placement) ? "prefix" : "suffix");
				}
				else {
					item.put("componentId", sourceItem.optInt("componentId", -1));
					if ("pushbutton".equals(type)) item.put("name", text(sourceItem, "name", 120, ""));
				}
				items.put(item);
			}
			page.put("items", items);
			pages.put(page);
		}
		result.put("pages", pages);
		return result;
	}

	private static String text(JSONObject object, String key, int limit, String fallback) {
		String value = object.optString(key, fallback).trim();
		if (value.isEmpty()) value = fallback;
		return value.length() > limit ? value.substring(0, limit) : value;
	}

	private static int bounded(int value, int minimum, int maximum) {
		return Math.max(minimum, Math.min(maximum, value));
	}

	private static double finiteNumber(JSONObject object, String key, double fallback) {
		try {
			double value = object.optDouble(key, fallback);
			return Double.isFinite(value) ? value : fallback;
		} catch (Exception ignored) {
			return fallback;
		}
	}
}
