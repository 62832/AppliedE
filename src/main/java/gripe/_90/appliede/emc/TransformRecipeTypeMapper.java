package gripe._90.appliede.emc;

import java.util.Collection;
import java.util.Collections;

import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;

import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.recipes.AERecipeTypes;
import appeng.recipes.transform.TransformCircumstance;
import appeng.recipes.transform.TransformRecipe;

import gripe._90.appliede.AppliedE;

import moze_intel.projecte.api.mapper.recipe.RecipeTypeMapper;
import moze_intel.projecte.emc.mappers.recipe.BaseRecipeTypeMapper;

@SuppressWarnings("unused")
@RecipeTypeMapper
public class TransformRecipeTypeMapper extends BaseRecipeTypeMapper {
    @Override
    public String getTranslationKey() {
        return "config." + AppliedE.MODID + ".mapper.transform";
    }

    @Override
    public String getName() {
        return "In-World Transformation Mapper";
    }

    @Override
    public String getDescription() {
        return "(AppliedE) Maps Applied Energistics 2 in-world transformation recipes.";
    }

    @Override
    public boolean canHandle(RecipeType<?> recipeType) {
        return AERecipeTypes.TRANSFORM.equals(recipeType);
    }

    @Override
    protected Collection<Ingredient> getIngredients(Recipe<?> recipe) {
        if (!(recipe instanceof TransformRecipe transform)) {
            return Collections.emptyList();
        }

        if (transform.getResultItem().is(AEItems.QUANTUM_ENTANGLED_SINGULARITY.asItem())) {
            return Collections.emptyList();
        }

        var ingredients = super.getIngredients(recipe);

        if (transform.circumstance == TransformCircumstance.EXPLOSION) {
            ingredients.add(Ingredient.of(AEBlocks.TINY_TNT));
        }

        return ingredients;
    }
}
