package com.ntmcleanroom.content.transmutator;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.RecipesCommon.OreDictStack;
import com.hbm.inventory.recipes.loader.SerializableRecipe;
import com.hbm.items.ModItems;
import net.minecraft.item.ItemStack;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.hbm.inventory.OreDictManager.U;

/**
 * Restored from NTM-CE's {@code NuclearTransmutationRecipes}: input item -> output item, gated by an
 * energy cost. Hooked into hbm's own {@link SerializableRecipe} pipeline (see {@link TransmutatorModule})
 * so it gets its own JSON file under config/hbmRecipes, same as every other hbm machine.
 */
public class SchrabidiumTransmutatorRecipes extends SerializableRecipe {

    public static final SchrabidiumTransmutatorRecipes INSTANCE = new SchrabidiumTransmutatorRecipes();

    public static List<TransmutationRecipe> recipes = new ArrayList<>();

    @Override
    public void registerDefaults() {
        recipes.add(new TransmutationRecipe(new OreDictStack(U.crystal()), new ItemStack(ModItems.crystal_schraranium, 1), 5_000_000L));
        recipes.add(new TransmutationRecipe(new OreDictStack(U.ingot()), new ItemStack(ModItems.ingot_schraranium, 1), 5_000_000L));
        recipes.add(new TransmutationRecipe(new OreDictStack(U.block()), new ItemStack(ModBlocks.block_schraranium, 1), 50_000_000L));
    }

    @Override
    public String getFileName() {
        return "hbmSchrabidiumTransmutator.json";
    }

    @Override
    public String getComment() {
        return "Recipes for the Schrabidium Transmutation Device: input item (matched by item or ore dict) -> output item, gated by an energy (HE) cost.";
    }

    @Override
    public Object getRecipeObject() {
        return recipes;
    }

    @Override
    public void deleteRecipes() {
        recipes.clear();
    }

    @Override
    public void readRecipe(JsonElement recipe) {
        JsonObject obj = (JsonObject) recipe;

        AStack input = readAStack(obj.get("input").getAsJsonArray());
        ItemStack output = readItemStack(obj.get("output").getAsJsonArray());
        long energy = obj.get("energy").getAsLong();

        recipes.add(new TransmutationRecipe(input, output, energy));
    }

    @Override
    public void writeRecipe(Object obj, JsonWriter writer) throws IOException {
        TransmutationRecipe recipe = (TransmutationRecipe) obj;

        writer.name("input");
        writeAStack(recipe.input, writer);

        writer.name("output");
        writeItemStack(recipe.output, writer);

        writer.name("energy").value(recipe.energy);
    }

    public static ItemStack getOutput(ItemStack stack) {
        TransmutationRecipe recipe = findRecipe(stack);
        return recipe == null ? null : recipe.output;
    }

    public static long getEnergy(ItemStack stack) {
        TransmutationRecipe recipe = findRecipe(stack);
        return recipe == null ? -1 : recipe.energy;
    }

    private static TransmutationRecipe findRecipe(ItemStack stack) {
        if (stack == null || stack.isEmpty())
            return null;

        for (TransmutationRecipe recipe : recipes) {
            if (recipe.input.matchesRecipe(stack, true))
                return recipe;
        }
        return null;
    }

    public static class TransmutationRecipe {
        public final AStack input;
        public final ItemStack output;
        public final long energy;

        public TransmutationRecipe(AStack input, ItemStack output, long energy) {
            this.input = input;
            this.output = output;
            this.energy = energy;
        }
    }
}
