package com.ntmcleanroom.compat.tinkers;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hbm.main.MainRegistry;
import com.ntmcleanroom.NTMCleanroomCore;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Configurable mB amounts for the Tinkers' Antique smeltery bridge: how much fluid one ingot
 * melts into, and how much fluid each hbm-shape cast ({@link ShapeCasts}) consumes - so server
 * owners can retune the bridge without recompiling, the same way hbm's own machines expose their
 * recipes as editable JSON under {@code config/hbmRecipes}.
 *
 * hbm's own melting machine (the Crucible, not a "Foundry" - see the plan) doesn't expose a clean
 * single per-material ratio to port (most of our materials melt via its automatic 1:1 smelting at
 * a fixed internal rate, and Steel/Combine Steel are multi-ingredient alloy recipes with no direct
 * per-ingot number), so every material defaults to Tinkers' own standard ratio (144 mB/ingot) here
 * - the real deliverable is editability, not a specific ported number.
 *
 * This isn't hooked into hbm's {@code SerializableRecipe.recipeHandlers} pipeline like
 * {@link com.ntmcleanroom.content.transmutator.SchrabidiumTransmutatorRecipes} is: that pipeline's
 * JSON read/write happens during hbm's own postInit, which runs *after* our init() (where Tinkers'
 * melting/casting registration needs to happen, per the working v1 timing) - so this reads/writes
 * its own file directly and synchronously instead, at the point we actually need the values.
 */
public final class TinkerSmelteryRecipes {

    private static final String FILE_NAME = "hbmTinkerSmeltery.json";
    private static final int DEFAULT_MB_PER_INGOT = 144;

    private static final Map<String, Integer> mbPerIngot = new LinkedHashMap<>();
    private static final Map<String, Integer> mbPerShapeCast = new LinkedHashMap<>();

    private TinkerSmelteryRecipes() {}

    public static int mbPerIngot(String materialId) {
        return mbPerIngot.getOrDefault(materialId, DEFAULT_MB_PER_INGOT);
    }

    public static int mbPerShapeCast(String shapeName) {
        return mbPerShapeCast.getOrDefault(shapeName, DEFAULT_MB_PER_INGOT);
    }

    /** Seeds defaults, then overlays whatever's in config/hbmRecipes/hbmTinkerSmeltery.json (writing it fresh if absent). */
    public static void load() {
        mbPerIngot.clear();
        mbPerShapeCast.clear();

        for (NTMTinkerMaterial ntm : TinkersMaterialRegistry.all()) {
            mbPerIngot.put(ntm.id, DEFAULT_MB_PER_INGOT);
        }
        for (String shape : new String[] {
                "billet", "blade", "blades", "dense_wire", "mechanism", "plate",
                "wire", "shell", "pipe", "barrel_light", "barrel_heavy",
                "receiver_light", "receiver_heavy", "stock", "grip"}) {
            mbPerShapeCast.put(shape, DEFAULT_MB_PER_INGOT);
        }
        // Alloy-only support fluids (see AlloyRecipes) - not bridged materials in their own right,
        // but still worth exposing here so their melt amount is editable alongside everything else.
        for (String alloyIngredient : new String[] {
                "ntmalloy_carbon", "ntmalloy_bismuth", "ntmalloy_arsenic",
                "ntmalloy_tungsten", "ntmalloy_magnetized_tungsten"}) {
            mbPerIngot.put(alloyIngredient, DEFAULT_MB_PER_INGOT);
        }

        File file = new File(MainRegistry.configDir, "hbmRecipes" + File.separator + FILE_NAME);
        try {
            if (file.isFile()) {
                String json = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
                JsonObject root = new JsonParser().parse(json).getAsJsonObject();
                readInto(root, "mbPerIngot", mbPerIngot);
                readInto(root, "mbPerShapeCast", mbPerShapeCast);
            } else {
                write(file);
            }
        } catch (Exception e) {
            NTMCleanroomCore.LOGGER.error("Failed to load {}, using defaults", file, e);
        }
    }

    private static void readInto(JsonObject root, String key, Map<String, Integer> target) {
        if (!root.has(key)) {
            return;
        }
        for (Map.Entry<String, JsonElement> entry : root.getAsJsonObject(key).entrySet()) {
            target.put(entry.getKey(), entry.getValue().getAsInt());
        }
    }

    private static void write(File file) throws IOException {
        file.getParentFile().mkdirs();

        JsonObject root = new JsonObject();
        root.addProperty("comment", "mB of molten metal per ingot melted, and mB consumed per hbm-shape "
                + "cast, for the Tinkers' Antique smeltery bridge. Edit and restart to change.");
        root.add("mbPerIngot", toJson(mbPerIngot));
        root.add("mbPerShapeCast", toJson(mbPerShapeCast));

        try (FileWriter out = new FileWriter(file)) {
            new GsonBuilder().setPrettyPrinting().create().toJson(root, out);
        }
    }

    private static JsonObject toJson(Map<String, Integer> map) {
        JsonObject obj = new JsonObject();
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            obj.addProperty(entry.getKey(), entry.getValue());
        }
        return obj;
    }
}
