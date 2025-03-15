package gripe._90.appliede.emc;

import net.minecraft.world.item.crafting.RecipeType;

import appeng.recipes.AERecipeTypes;

import gripe._90.appliede.AppliedE;

import moze_intel.projecte.api.mapper.recipe.RecipeTypeMapper;
import moze_intel.projecte.emc.mappers.recipe.BaseRecipeTypeMapper;

@SuppressWarnings("unused")
@RecipeTypeMapper
public class ChargerRecipeTypeMapper extends BaseRecipeTypeMapper {
    @Override
    public String getTranslationKey() {
        return "config." + AppliedE.MODID + ".mapper.charger";
    }

    @Override
    public String getName() {
        return "Charging Mapper";
    }

    @Override
    public String getDescription() {
        return "(AppliedE) Maps Applied Energistics 2 charger recipes.";
    }

    @Override
    public boolean canHandle(RecipeType<?> recipeType) {
        return AERecipeTypes.CHARGER.equals(recipeType);
    }
}
