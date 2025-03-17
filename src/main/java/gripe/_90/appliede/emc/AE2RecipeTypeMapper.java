package gripe._90.appliede.emc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;

import appeng.core.definitions.AEBlocks;
import appeng.recipes.AERecipeTypes;
import appeng.recipes.handlers.InscriberProcessType;
import appeng.recipes.handlers.InscriberRecipe;
import appeng.recipes.transform.TransformCircumstance;
import appeng.recipes.transform.TransformRecipe;

import gripe._90.appliede.AppliedE;

import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.mapper.recipe.INSSFakeGroupManager;
import moze_intel.projecte.api.mapper.recipe.IRecipeTypeMapper;
import moze_intel.projecte.api.mapper.recipe.RecipeTypeMapper;
import moze_intel.projecte.api.nss.NSSItem;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;

@SuppressWarnings("unused")
@RecipeTypeMapper
public class AE2RecipeTypeMapper implements IRecipeTypeMapper {
    @Override
    public boolean canHandle(RecipeType<?> recipeType) {
        return recipeType == AERecipeTypes.CHARGER
                || recipeType == AERecipeTypes.INSCRIBER
                || recipeType == AERecipeTypes.TRANSFORM;
    }

    @Override
    public boolean handleRecipe(
            IMappingCollector<NormalizedSimpleStack, Long> collector,
            RecipeHolder<?> holder,
            RegistryAccess access,
            INSSFakeGroupManager fakeGroupManager) {
        var recipe = holder.value();
        var output = recipe.getResultItem(access);

        if (output.isEmpty()) {
            return false;
        }

        var ingredients = getIngredients(recipe);

        if (ingredients.isEmpty()) {
            return true;
        }

        var ingredientMap = new Object2IntOpenHashMap<NormalizedSimpleStack>();

        for (var ingredient : ingredients) {
            if (!ingredient.hasNoItems()) {
                var items = ingredient.getItems();

                if (items.length == 1) {
                    ingredientMap.mergeInt(NSSItem.createItem(items[0]), 1, Integer::sum);
                } else {
                    var rawNSSMatches = new Object2IntOpenHashMap<NormalizedSimpleStack>(items.length);

                    for (var item : items) {
                        rawNSSMatches.put(NSSItem.createItem(item), 1);
                    }

                    var fakeGroup = fakeGroupManager.getOrCreateFakeGroup(rawNSSMatches, true, true);
                    var dummy = fakeGroup.dummy();
                    ingredientMap.mergeInt(dummy, 1, Integer::sum);

                    if (fakeGroup.created()) {
                        for (var item : items) {
                            var groupIngredientMap = new Object2IntArrayMap<NormalizedSimpleStack>(1);
                            groupIngredientMap.put(NSSItem.createItem(item), 1);
                            collector.addConversion(1, dummy, groupIngredientMap);
                        }
                    }
                }
            }
        }

        collector.addConversion(output.getCount(), NSSItem.createItem(output), ingredientMap);
        return true;
    }

    private static Collection<Ingredient> getIngredients(Recipe<?> recipe) {
        if (recipe instanceof InscriberRecipe inscribe && inscribe.getProcessType() == InscriberProcessType.INSCRIBE) {
            var output = inscribe.getResultItem();
            // prevent recursive recipes (i.e. those for extra presses) from yielding an EMC value for their result
            return inscribe.getTopOptional().test(output)
                            || inscribe.getBottomOptional().test(output)
                    ? Collections.emptyList()
                    : Collections.singletonList(inscribe.getMiddleInput());
        }

        var ingredients = new ArrayList<>(recipe.getIngredients());

        if (recipe instanceof TransformRecipe transform && transform.circumstance == TransformCircumstance.EXPLOSION) {
            ingredients.add(Ingredient.of(AEBlocks.TINY_TNT));
        }

        return ingredients;
    }

    @Override
    public String getName() {
        return "AE2 Recipe Mapper";
    }

    @Override
    public String getTranslationKey() {
        return "config." + AppliedE.MODID + ".mapper.recipe";
    }

    @Override
    public String getDescription() {
        return "(AppliedE) Maps recipes from Applied Energistics 2 such as for In-world Transformation, the Charger and the Inscriber.";
    }
}
