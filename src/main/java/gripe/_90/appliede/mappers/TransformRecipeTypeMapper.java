package gripe._90.appliede.mappers;

import java.util.Collection;
import java.util.Collections;

import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;

import appeng.core.definitions.AEBlocks;
import appeng.recipes.transform.TransformCircumstance;
import appeng.recipes.transform.TransformRecipe;

import gripe._90.appliede.AppliedE;

import moze_intel.projecte.api.mapper.recipe.RecipeTypeMapper;
import moze_intel.projecte.emc.mappers.recipe.BaseRecipeTypeMapper;

@SuppressWarnings("unused")
@RecipeTypeMapper
public class TransformRecipeTypeMapper extends BaseRecipeTypeMapper {
    @Override
    public String getName() {
        return "AE2Transform";
    }

    @Override
    public String getDescription() {
        return "Maps Applied Energistics 2 in-world transformation recipes.";
    }

    @Override
    public boolean isAvailable() {
        return AppliedE.useCustomMapper();
    }

    @Override
    public boolean canHandle(RecipeType<?> recipeType) {
        return TransformRecipe.TYPE.equals(recipeType);
    }

    @Override
    protected Collection<Ingredient> getIngredients(Recipe<?> recipe) {
        if (!(recipe instanceof TransformRecipe transform)) {
            return Collections.emptyList();
        }

        var ingredients = super.getIngredients(recipe);

        if (transform.circumstance == TransformCircumstance.EXPLOSION) {
            ingredients.add(Ingredient.of(AEBlocks.TINY_TNT));
        }

        return ingredients;
    }
}
