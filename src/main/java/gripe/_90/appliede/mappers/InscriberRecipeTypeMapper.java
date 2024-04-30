package gripe._90.appliede.mappers;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;

import appeng.recipes.handlers.InscriberProcessType;
import appeng.recipes.handlers.InscriberRecipe;

import gripe._90.appliede.AppliedE;

import moze_intel.projecte.api.mapper.recipe.RecipeTypeMapper;
import moze_intel.projecte.emc.mappers.recipe.BaseRecipeTypeMapper;

@SuppressWarnings("unused")
@RecipeTypeMapper
public class InscriberRecipeTypeMapper extends BaseRecipeTypeMapper {
    @Override
    public String getName() {
        return "AE2Inscriber";
    }

    @Override
    public String getDescription() {
        return "Maps Applied Energistics 2 inscriber recipes.";
    }

    @Override
    public boolean isAvailable() {
        return AppliedE.useCustomMapper();
    }

    @Override
    public boolean canHandle(RecipeType<?> recipeType) {
        return InscriberRecipe.TYPE.equals(recipeType);
    }

    @Override
    protected Collection<Ingredient> getIngredients(Recipe<?> recipe) {
        if (!(recipe instanceof InscriberRecipe inscriber)) {
            return Collections.emptyList();
        }

        return inscriber.getProcessType() == InscriberProcessType.INSCRIBE
                ? Collections.singletonList(inscriber.getMiddleInput())
                : List.of(inscriber.getMiddleInput(), inscriber.getTopOptional(), inscriber.getBottomOptional());
    }
}
