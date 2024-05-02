package gripe._90.appliede.service;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;

import net.minecraft.network.chat.Component;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;

import gripe._90.appliede.AppliedE;
import gripe._90.appliede.key.EMCKey;

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
            return extractItem(item, amount, mode, source);
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

    public long insertItem(AEItemKey what, long amount, Actionable mode, IActionSource source) {
        if (!service.knowsItem(what)) {
            return 0;
        }

        var grid = service.getGrid();

        if (grid == null) {
            return 0;
        }

        var energy = grid.getEnergyService();
        var itemEmc = BigInteger.valueOf(IEMCProxy.INSTANCE.getSellValue(what.toStack()));
        var totalEmc = itemEmc.multiply(BigInteger.valueOf(amount));
        var insertedItems = 0L;

        while (totalEmc.compareTo(BigInteger.ZERO) > 0) {
            var toDeposit = AppliedE.clampedLong(totalEmc);
            var canDeposit = toDeposit;

            if (mode == Actionable.MODULATE) {
                var requiredPower = PowerMultiplier.CONFIG.multiply(toDeposit);
                var availablePower = energy.extractAEPower(requiredPower, Actionable.SIMULATE, PowerMultiplier.CONFIG);
                var powerToExpend = Math.min(requiredPower, availablePower);
                energy.extractAEPower(powerToExpend, Actionable.MODULATE, PowerMultiplier.CONFIG);

                canDeposit = (long) PowerMultiplier.CONFIG.divide(powerToExpend);
                insert(EMCKey.BASE, canDeposit, Actionable.MODULATE, source);
            }

            insertedItems += BigInteger.valueOf(canDeposit).divide(itemEmc).longValue();
            var wouldHaveDeposited = BigInteger.valueOf(toDeposit);
            totalEmc = totalEmc.subtract(wouldHaveDeposited).add(wouldHaveDeposited.remainder(itemEmc));
        }

        return insertedItems;
    }

    public long extractItem(AEItemKey what, long amount, Actionable mode, IActionSource source) {
        if (!service.knowsItem(what)) {
            return 0;
        }

        var grid = service.getGrid();

        if (grid == null || grid.getStorageService().getCachedInventory().get(what) > 0) {
            return 0;
        }

        var energy = grid.getEnergyService();
        var itemEmc = BigInteger.valueOf(IEMCProxy.INSTANCE.getValue(what.toStack()));
        var totalEmc = itemEmc.multiply(BigInteger.valueOf(amount));
        var acquiredItems = 0L;

        while (totalEmc.compareTo(BigInteger.ZERO) > 0) {
            var toWithdraw = AppliedE.clampedLong(totalEmc);
            var canWithdraw = extract(EMCKey.BASE, toWithdraw, Actionable.SIMULATE, source);

            if (mode == Actionable.MODULATE) {
                var requiredPower = source.player().isPresent() ? 0 : PowerMultiplier.CONFIG.multiply(canWithdraw);
                var availablePower = energy.extractAEPower(requiredPower, Actionable.SIMULATE, PowerMultiplier.CONFIG);
                var powerToExpend = Math.min(requiredPower, availablePower);
                energy.extractAEPower(powerToExpend, Actionable.MODULATE, PowerMultiplier.CONFIG);

                canWithdraw = requiredPower > 0 ? (long) PowerMultiplier.CONFIG.divide(powerToExpend) : canWithdraw;
                extract(EMCKey.BASE, canWithdraw, Actionable.MODULATE, source);
            }

            acquiredItems += BigInteger.valueOf(canWithdraw).divide(itemEmc).longValue();
            var wouldHaveWithdrawn = BigInteger.valueOf(toWithdraw);
            totalEmc = totalEmc.subtract(wouldHaveWithdrawn).add(wouldHaveWithdrawn.remainder(itemEmc));
        }

        return acquiredItems;
    }

    public int getHighestTier() {
        return highestTier;
    }

    @Override
    public Component getDescription() {
        return AppliedE.EMC_MODULE.get().getDescription();
    }
}
