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
				if (!"image".equals(type) && !"frame".equals(type) && !"label".equals(type) && !"value".equals(type)
						&& !"graph".equals(type) && !"pushbutton".equals(type) && !"switch".equals(type)
						&& !"fixedInput".equals(type) && !"selector".equals(type) && !"bar".equals(type) && !"gauge".equals(type) && !"cover".equals(type) && !"timer".equals(type)) continue;
				JSONObject item = new JSONObject();
				item.put("id", text(sourceItem, "id", 80, "item-" + pageIndex + "-" + itemIndex));
				item.put("type", type);
				item.put("x", bounded(sourceItem.optInt("x", 20), 0, 10000));
				item.put("y", bounded(sourceItem.optInt("y", 20), 0, 10000));
				boolean graph = "graph".equals(type), timer = "timer".equals(type), meter = "bar".equals(type) || "gauge".equals(type);
				item.put("width", bounded(sourceItem.optInt("width", graph ? 500 : timer ? 620 : meter ? 320 : 220), graph ? 200 : timer ? 280 : 100, 2000));
				item.put("height", bounded(sourceItem.optInt("height", graph ? 280 : timer ? 220 : meter ? ("gauge".equals(type) ? 240 : 140) : "label".equals(type) || "value".equals(type) ? 70 : 65), graph ? 150 : timer ? 120 : meter ? 100 : 45, 2000));
				int defaultFontSize = "label".equals(type) ? 18 : "value".equals(type) ? 24 : 14;
				item.put("fontSize", bounded(sourceItem.optInt("fontSize", defaultFontSize), 8, 200));
				if ("label".equals(type) || "value".equals(type)) {
					String horizontal = sourceItem.optString("horizontalAlign", "center");
					String vertical = sourceItem.optString("verticalAlign", "middle");
					item.put("horizontalAlign", "left".equals(horizontal) || "right".equals(horizontal) ? horizontal : "center");
					item.put("verticalAlign", "top".equals(vertical) || "bottom".equals(vertical) ? vertical : "middle");
				}
				if ("image".equals(type)) {
                    String id=sourceItem.optString("imageId","");
                    if(!DashboardImageStore.validId(id))throw new IllegalArgumentException("Invalid dashboard image ID");
                    item.put("imageId",id).put("alt",text(sourceItem,"alt",200,"Bild")).put("fit","cover".equals(sourceItem.optString("fit"))?"cover":"contain");
                }
                else if ("frame".equals(type)) {
                    String style=sourceItem.optString("lineStyle","solid");
                    if(!java.util.Set.of("solid","dashed","dotted","double").contains(style))style="solid";
                    item.put("lineStyle",style).put("lineWidth",bounded(sourceItem.optInt("lineWidth",2),1,30)).put("lineColor",colorOrDefault(sourceItem.optString("lineColor",""),0));
                    item.put("height",Math.max(item.getInt("height"),2*item.getInt("lineWidth")));
                }
                else if ("label".equals(type)) item.put("text", text(sourceItem, "text", 500, "Label"));
				else if ("value".equals(type) || meter) {
                    if (meter) {
                        item.put("name", text(sourceItem, "name", 120, ""));
                        double minimum = finiteNumber(sourceItem, "minimum", 0), maximum = finiteNumber(sourceItem, "maximum", 100);
                        if (!(maximum > minimum) || !Double.isFinite(maximum - minimum)) throw new IllegalArgumentException("Dashboard maximum must be greater than minimum and the range must be finite");
                        item.put("minimum", minimum).put("maximum", maximum);
                        item.put("color", colorOrDefault(sourceItem.optString("color", ""), 0));
                        if ("bar".equals(type)) item.put("orientation", "vertical".equals(sourceItem.optString("orientation", "horizontal")) ? "vertical" : "horizontal");
                        if ("gauge".equals(type)) {
                            item.put("showTicks", sourceItem.optBoolean("showTicks", true));
                            item.put("divisions", bounded(sourceItem.optInt("divisions", 10), 1, 100));
                        }
                    }
					item.put("componentId", sourceItem.optInt("componentId", -1));
					item.put("scale", finiteNumber(sourceItem, "scale", 1.0));
					item.put("unit", text(sourceItem, "unit", 40, ""));
					String placement = sourceItem.optString("unitPlacement", "suffix");
					item.put("unitPlacement", "prefix".equals(placement) ? "prefix" : "suffix");
					item.put("trueColor", color(sourceItem.optString("trueColor", "")));
					item.put("falseColor", color(sourceItem.optString("falseColor", "")));
					item.put("booleanTrueText", text(sourceItem, "booleanTrueText", 120, ""));
					item.put("booleanFalseText", text(sourceItem, "booleanFalseText", 120, ""));
					JSONArray ranges = new JSONArray(), sourceRanges = sourceItem.optJSONArray("colorRanges");
					if (sourceRanges != null) for (int rangeIndex = 0; rangeIndex < Math.min(sourceRanges.length(), 50); rangeIndex++) {
						JSONObject sourceRange = sourceRanges.getJSONObject(rangeIndex);
						double minimum = finiteNumber(sourceRange, "minimum", 0), maximum = finiteNumber(sourceRange, "maximum", 0);
						if (maximum < minimum) continue;
						ranges.put(new JSONObject().put("minimum", minimum).put("maximum", maximum)
								.put("color", color(sourceRange.optString("color", ""))));
					}
					item.put("colorRanges", ranges);
				}
				else if (graph) {
					JSONArray sourceSeries = sourceItem.optJSONArray("series"), series = new JSONArray();
					if (sourceSeries != null) for (int seriesIndex = 0; seriesIndex < Math.min(sourceSeries.length(), 20); seriesIndex++) {
						JSONObject sourceEntry = sourceSeries.optJSONObject(seriesIndex);
						if (sourceEntry == null) continue;
						series.put(new JSONObject().put("componentId", sourceEntry.optInt("componentId", -1))
								.put("color", colorOrDefault(sourceEntry.optString("color", ""), seriesIndex)));
					}
					if (series.length() == 0) series.put(new JSONObject().put("componentId", sourceItem.optInt("componentId", -1))
							.put("color", "#00897b"));
					item.put("series", series);
					item.put("duration", bounded(sourceItem.optInt("duration", 300), 10, 86400));
					item.put("autoscale", sourceItem.optBoolean("autoscale", true));
					double minimum = finiteNumber(sourceItem, "yMin", 0.0);
					double maximum = finiteNumber(sourceItem, "yMax", 100.0);
					if (maximum <= minimum) { minimum = 0.0; maximum = 100.0; }
					item.put("yMin", minimum);
					item.put("yMax", maximum);
				}
				else {
					item.put("componentId", sourceItem.optInt("componentId", -1));
					if ("fixedInput".equals(type)) {
                        boolean slider = "slider".equals(sourceItem.optString("inputMode"));
                        item.put("inputMode", slider ? "slider" : "keypad");
                        if (slider) {
                            if (!sourceItem.has("minimum") || !sourceItem.has("maximum")) throw new IllegalArgumentException("Slider requires minimum and maximum");
                            double minimum = sourceItem.getDouble("minimum"), maximum = sourceItem.getDouble("maximum");
                            if (!Double.isFinite(minimum) || !Double.isFinite(maximum) || !(maximum > minimum) || !Double.isFinite(maximum - minimum)) throw new IllegalArgumentException("Slider maximum must exceed minimum");
                            item.put("minimum", minimum).put("maximum", maximum);
                        }
                    }
					if ("selector".equals(type)) item.put("displayMode", "radio".equals(sourceItem.optString("displayMode")) ? "radio" : "combo");
					if ("pushbutton".equals(type)) item.put("name", text(sourceItem, "name", 120, ""));
				}
				if (timer) {
                    JSONArray units = new JSONArray(), selected = sourceItem.optJSONArray("timerUnits");
                    for (String unit : new String[]{"h", "min", "s", "ms"}) {
                        boolean include = selected == null;
                        if (selected != null) for (int index = 0; index < selected.length(); index++)
                            if (unit.equals(selected.optString(index))) include = true;
                        if (include) units.put(unit);
                    }
                    if (units.length() == 0) throw new IllegalArgumentException("Select at least one timer unit");
                    item.put("timerUnits", units);
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

	private static String color(String value) {
		return value != null && value.matches("#[0-9a-fA-F]{6}") ? value.toLowerCase() : "";
	}

	private static String colorOrDefault(String value, int index) {
		String validated = color(value);
		if (!validated.isEmpty()) return validated;
		String[] palette = { "#00897b", "#1976d2", "#d81b60", "#ef6c00", "#7b1fa2", "#3949ab", "#43a047", "#6d4c41" };
		return palette[index % palette.length];
	}
}
