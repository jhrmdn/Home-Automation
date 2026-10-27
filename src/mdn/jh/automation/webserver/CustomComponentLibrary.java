package mdn.jh.automation.webserver;

import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.json.*;
import mdn.jh.automation.io.logic.components.custom.CustomComponent;

/** Reusable definitions; running instances embed their own independent copy. */
public final class CustomComponentLibrary {
    private final Path file;
    public CustomComponentLibrary(Path file) { this.file = file; }
    private JSONArray load() throws Exception {
        if (!Files.exists(file)) return new JSONArray();
        return new JSONObject(Files.readString(file, StandardCharsets.UTF_8)).getJSONArray("templates");
    }
    private void write(JSONArray entries) throws Exception {
        Path temporary = file.resolveSibling(file.getFileName() + ".tmp");
        Files.writeString(temporary, new JSONObject().put("templates", entries).toString(2), StandardCharsets.UTF_8);
        try { Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE); }
        catch (AtomicMoveNotSupportedException e) { Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING); }
    }
    public synchronized JSONArray catalog() throws Exception {
        JSONArray catalog = new JSONArray(), entries = load();
        for (int i = 0; i < entries.length(); i++) {
            JSONObject entry = entries.getJSONObject(i), graph = entry.getJSONObject("graph");
            catalog.put(new JSONObject().put("id", entry.getString("id")).put("name", graph.getString("name"))
                .put("inputs", graph.getJSONArray("inputs")).put("outputs", graph.getJSONArray("outputs"))
                .put("componentCount", graph.getJSONArray("nodes").length()));
        }
        return catalog;
    }
    public synchronized String save(JSONObject graph) throws Exception {
        String name = graph.getString("name").trim();
        if (name.isEmpty() || name.length() > 100) throw new IllegalArgumentException("Name must be 1–100 characters");
        graph.put("name", name);
        if (graph.toString().length() > 2_000_000) throw new IllegalArgumentException("Custom component definition is too large");
        JSONArray entries = load();
        if (entries.length() >= 100) throw new IllegalArgumentException("At most 100 custom components can be saved");
        for (int i = 0; i < entries.length(); i++) if (name.equalsIgnoreCase(entries.getJSONObject(i).getJSONObject("graph").getString("name")))
            throw new IllegalArgumentException("A custom component with this name already exists");
        CustomComponent validation = new CustomComponent();
        try { validation.initialize(graph, true); } finally { validation.shutdownRuntime(); }
        String id = UUID.randomUUID().toString();
        entries.put(new JSONObject().put("id", id).put("graph", graph)); write(entries); return id;
    }
    public synchronized CustomComponent instantiate(String id) throws Exception {
        JSONArray entries = load();
        for (int i = 0; i < entries.length(); i++) {
            JSONObject entry = entries.getJSONObject(i);
            if (!id.equals(entry.getString("id"))) continue;
            CustomComponent component = new CustomComponent();
            try { component.initialize(entry.getJSONObject("graph"), true); component.setName(component.getTemplateName()); return component; }
            catch (Exception error) { component.shutdownRuntime(); throw error; }
        }
        throw new IllegalArgumentException("Custom component not found");
    }
}
