package gripe._90.appliede.me.service;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;

import com.google.common.primitives.Ints;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import appeng.core.stats.AeStats;

import gripe._90.appliede.AppliedE;
import gripe._90.appliede.me.key.EMCKey;

import moze_intel.projecte.api.ItemInfo;
import moze_intel.projecte.api.proxy.IEMCProxy;

public class EMCStorage implements MEStorage {
    private final KnowledgeService service;
    private int highestTier = 1;

    EMCStorage(KnowledgeService service) {
        this.service = service;
    }

    @Override
    public void getAvailableStacks(KeyCounter out) {
        var emc = service.getEmc();
        var currentTier = 1;

        while (emc.divide(AppliedE.TIER_LIMIT).signum() == 1) {
            out.add(EMCKey.tier(currentTier), emc.remainder(AppliedE.TIER_LIMIT).longValue());
            emc = emc.divide(AppliedE.TIER_LIMIT);
            currentTier++;
        }

        out.add(EMCKey.tier(currentTier), emc.longValue());

        if (highestTier != currentTier) {
            highestTier = currentTier;
            service.updatePatterns();
        }
    }

    @Override
    public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
        if (amount == 0 || !(what instanceof EMCKey emc)) {
            return 0;
        }

        if (mode == Actionable.MODULATE) {
            var providers = new ArrayList<>(service.getProviders());
            Collections.shuffle(providers);

            var toInsert = BigInteger.valueOf(amount).multiply(AppliedE.TIER_LIMIT.pow(emc.getTier() - 1));
            var divisor = BigInteger.valueOf(service.getProviders().size());
            var quotient = toInsert.divide(divisor);
            var remainder = toInsert.remainder(divisor).longValue();

            for (var p = 0; p < providers.size(); p++) {
                var provider = providers.get(p);
                provider.setEmc(provider.getEmc().add(quotient.add(p < remainder ? BigInteger.ONE : BigInteger.ZERO)));
            }

            service.syncEmc();
        }

        return amount;
    }

    @Override
    public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
        if (amount == 0) {
            return 0;
        }

        if (what instanceof AEItemKey item && source.player().isPresent()) {
            return extractItem(item, amount, mode, source, false);
        }

        if (!(what instanceof EMCKey emc)) {
            return 0;
        }

        var extracted = 0L;
        var multiplier = AppliedE.TIER_LIMIT.pow(emc.getTier() - 1);

        var providers = new ArrayList<>(service.getProviders());

        while (!providers.isEmpty() && extracted < amount) {
            Collections.shuffle(providers);

            var toExtract = BigInteger.valueOf(amount - extracted).multiply(multiplier);
            var divisor = BigInteger.valueOf(service.getProviders().size());
            var quotient = toExtract.divide(divisor);
            var remainder = toExtract.remainder(divisor).longValue();

            for (var p = 0; p < providers.size(); p++) {
                var provider = providers.get(p);

                var currentEmc = provider.getEmc();
                var toExtractFrom = quotient.add(p < remainder ? BigInteger.ONE : BigInteger.ZERO);

                if (currentEmc.compareTo(toExtractFrom) <= 0) {
                    if (mode == Actionable.MODULATE) {
                        provider.setEmc(BigInteger.ZERO);
                    }

                    extracted += currentEmc.divide(multiplier).longValue();
                    // provider exhausted, remove from current list to re-extract deficit from remaining providers
                    providers.remove(provider);
                } else {
                    if (mode == Actionable.MODULATE) {
                        provider.setEmc(currentEmc.subtract(toExtractFrom));
                    }

                    extracted += toExtractFrom.divide(multiplier).longValue();
                }
            }
        }

        if (mode == Actionable.MODULATE) {
            service.syncEmc();
        }

        return extracted;
    }

    public long insertItem(AEItemKey what, long amount, Actionable mode, IActionSource source, boolean mayLearn) {
        if (service.getProviders().isEmpty()) {
            return 0;
        }

        if (!mayLearn && !service.knowsItem(what)) {
            return 0;
        }

        var playerSource = source.player();

        if (mayLearn
                && playerSource.isPresent()
                && !service.isTrackingPlayer(playerSource.get().getUUID())) {
            return 0;
        }

        var grid = service.getGrid();

        if (grid == null) {
            return 0;
        }

        var itemEmc = BigInteger.valueOf(IEMCProxy.INSTANCE.getSellValue(what.toStack()));
        var totalEmc = itemEmc.multiply(BigInteger.valueOf(amount));
        var totalInserted = 0L;

        while (totalEmc.compareTo(BigInteger.ZERO) > 0) {
            var toDeposit = AppliedE.clampedLong(totalEmc);
            var canDeposit = toDeposit;

            if (mode == Actionable.MODULATE) {
                canDeposit = getAmountAfterPowerExpenditure(canDeposit, grid.getEnergyService());
                insert(EMCKey.BASE, canDeposit, Actionable.MODULATE, source);
            }

            var inserted = BigInteger.valueOf(canDeposit).divide(itemEmc).longValue();
            totalInserted += inserted;
            source.player().ifPresent(player -> {
                if (mode == Actionable.MODULATE) {
                    AeStats.ItemsInserted.addToPlayer(player, Ints.saturatedCast(inserted));
                }
            });

            var wouldHaveDeposited = BigInteger.valueOf(toDeposit);
            totalEmc = totalEmc.subtract(wouldHaveDeposited).add(wouldHaveDeposited.remainder(itemEmc));
        }

        if (mode == Actionable.MODULATE && mayLearn) {
            service.getProviders().forEach(provider -> {
                var stack = what.toStack();
                provider.addKnowledge(stack);
                playerSource.ifPresent(player -> {
                    if (player instanceof ServerPlayer serverPlayer) {
                        provider.syncKnowledgeChange(serverPlayer, ItemInfo.fromStack(stack), true);
                    }
                });
            });
        }

        return totalInserted;
    }

    public long extractItem(AEItemKey what, long amount, Actionable mode, IActionSource source, boolean skipStored) {
        if (!service.knowsItem(what)) {
            return 0;
        }

        var grid = service.getGrid();

        if (grid == null) {
            return 0;
        }

        if (!skipStored && grid.getStorageService().getCachedInventory().get(what) > 0) {
            return 0;
        }

        var itemEmc = BigInteger.valueOf(IEMCProxy.INSTANCE.getValue(what.toStack()));
        var totalEmc = itemEmc.multiply(BigInteger.valueOf(amount));
        var totalExtracted = 0L;

        while (totalEmc.compareTo(BigInteger.ZERO) > 0) {
            var toWithdraw = AppliedE.clampedLong(totalEmc);
            var canWithdraw = extract(EMCKey.BASE, toWithdraw, Actionable.SIMULATE, source);

            if (mode == Actionable.MODULATE) {
                canWithdraw = getAmountAfterPowerExpenditure(canWithdraw, grid.getEnergyService());
                extract(EMCKey.BASE, canWithdraw, Actionable.MODULATE, source);
            }

            var extracted = BigInteger.valueOf(canWithdraw).divide(itemEmc).longValue();
            totalExtracted += extracted;
            source.player().ifPresent(player -> {
                if (mode == Actionable.MODULATE) {
                    AeStats.ItemsExtracted.addToPlayer(player, Ints.saturatedCast(extracted));
                }
            });

            var wouldHaveWithdrawn = BigInteger.valueOf(toWithdraw);
            totalEmc = totalEmc.subtract(wouldHaveWithdrawn).add(wouldHaveWithdrawn.remainder(itemEmc));
        }

        return totalExtracted;
    }

    public long learnNewItem(AEItemKey what, ServerPlayer player) {
        return !service.knowsItem(what)
                ? insertItem(what, 1, Actionable.MODULATE, IActionSource.ofPlayer(player), true)
                : 0;
    }

    private long getAmountAfterPowerExpenditure(long maxAmount, IEnergySource source) {
        var requiredPower = PowerMultiplier.CONFIG.multiply(maxAmount);
        var availablePower = source.extractAEPower(requiredPower, Actionable.SIMULATE, PowerMultiplier.CONFIG);
        var powerToExpend = Math.min(requiredPower, availablePower);
        source.extractAEPower(powerToExpend, Actionable.MODULATE, PowerMultiplier.CONFIG);
        return (long) PowerMultiplier.CONFIG.divide(powerToExpend);
    }

    public int getHighestTier() {
        return highestTier;
    }

    @Override
    public Component getDescription() {
        return AppliedE.EMC_MODULE.get().getDescription();
    }
}
