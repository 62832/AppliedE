package gripe._90.appliede.me.service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.features.IPlayerRegistry;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;

import gripe._90.appliede.AppliedE;
import gripe._90.appliede.AppliedEConfig;
import gripe._90.appliede.me.key.EMCKey;
import gripe._90.appliede.me.key.EMCKeyType;
import gripe._90.appliede.me.misc.TransmutationCapable;

import moze_intel.projecte.api.ItemInfo;
import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import moze_intel.projecte.api.proxy.IEMCProxy;

record EMCStorage(KnowledgeService service, IEnergySource energy) implements MEStorage {
    @Override
    public void getAvailableStacks(KeyCounter out) {
        var emc = service.getEmc();

        for (var tier = 1; emc.signum() == 1; tier++) {
            out.add(EMCKey.of(tier), emc.remainder(AppliedE.TIER_LIMIT).longValue());
            emc = emc.divide(AppliedE.TIER_LIMIT);
        }
    }

    @Override
    public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
        if (amount <= 0) {
            return 0;
        }

        if (what instanceof AEItemKey item) {
            return insertItem(item, amount, mode, source);
        }

        if (!(what instanceof EMCKey emc)) {
            return 0;
        }

        if (mode == Actionable.MODULATE) {
            var providers = service.getAllProviders();
            Collections.shuffle(providers);

            if (emc.getTier() == 1) {
                var quotient = amount / providers.size();
                var remainder = amount % providers.size();

                for (var p = 0; p < providers.size(); p++) {
                    var provider = providers.get(p);
                    provider.setEmc(provider.getEmc().add(BigInteger.valueOf(quotient + (p < remainder ? 1 : 0))));
                }
            } else {
                var toInsert = BigInteger.valueOf(amount).multiply(AppliedE.TIER_LIMIT.pow(emc.getTier() - 1));
                distributeEmc(toInsert, providers);
            }

            service.syncEmc();
        }

        return amount;
    }

    @Override
    public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
        if (amount <= 0) {
            return 0;
        }

        if (what instanceof AEItemKey item) {
            return source.machine().isPresent() && source.machine().get() instanceof TransmutationCapable
                    ? extractItem(item, amount, mode, source)
                    : 0;
        }

        if (!(what instanceof EMCKey emc)) {
            return 0;
        }

        var providers = extractionProviders(source);

        if (providers.isEmpty()) {
            return 0;
        }

        var multiplier = AppliedE.TIER_LIMIT.pow(emc.getTier() - 1);
        var rawEmc = BigInteger.valueOf(amount).multiply(multiplier);
        var extracted = BigInteger.ZERO;

        while (!providers.isEmpty() && extracted.compareTo(rawEmc) < 0) {
            Collections.shuffle(providers);

            var toExtract = rawEmc.subtract(extracted);
            var divisor = BigInteger.valueOf(providers.size());
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

                    extracted = extracted.add(currentEmc);
                    // provider exhausted, remove from current list to re-extract deficit from remaining providers
                    providers.remove(provider);
                } else {
                    if (mode == Actionable.MODULATE) {
                        provider.setEmc(currentEmc.subtract(toExtractFrom));
                    }

                    extracted = extracted.add(toExtractFrom);
                }
            }
        }

        if (mode == Actionable.MODULATE) {
            service.syncEmc();
        }

        return extracted.divide(multiplier).longValue();
    }

    private long insertItem(AEItemKey what, long amount, Actionable mode, IActionSource source) {
        if (!IEMCProxy.INSTANCE.hasValue(what.toStack())) {
            return 0;
        }

        if (source.machine().isEmpty() || !(source.machine().get() instanceof TransmutationCapable tc)) {
            return 0;
        }

        var needsLearning = tc.mayLearn() && !service.getKnownItems().contains(what);

        IKnowledgeProvider learningProvider = null;
        Player learningPlayer = null;

        if (needsLearning) {
            if (source.player().isPresent()) {
                learningPlayer = source.player().get();
                learningProvider = service.getProviderFor(learningPlayer.getUUID());
            } else {
                var node = tc.getActionableNode();

                if (node == null) {
                    return 0;
                }

                learningProvider = service.getProviderFor(node.getOwningPlayerProfileId());
                learningPlayer = IPlayerRegistry.getConnected(node.getLevel().getServer(), node.getOwningPlayerId());
            }

            if (learningProvider == null) {
                return 0;
            }
        }

        if (mode == Actionable.MODULATE) {
            var itemEmc = BigInteger.valueOf(IEMCProxy.INSTANCE.getSellValue(what.toStack()));
            var totalEmc = itemEmc.multiply(BigInteger.valueOf(amount));

            if (tc.consumePowerOnInsert()) {
                amount = getAmountAfterPowerExpenditure(totalEmc, itemEmc, energy);
            }

            if (amount == 0) {
                return 0;
            }

            var providers = service.getAllProviders();
            Collections.shuffle(providers);
            distributeEmc(totalEmc, providers);
            service.syncEmc();

            if (needsLearning) {
                addKnowledge(what, learningProvider, learningPlayer);
                tc.onLearn();
            }
        }

        return amount;
    }

    private long extractItem(AEItemKey what, long amount, Actionable mode, IActionSource source) {
        var providers = extractionProviders(source);

        if (providers.isEmpty()) {
            return 0;
        }

        if (amount <= 0 || !service.getKnownItems().contains(what)) {
            return 0;
        }

        var itemEmc = BigInteger.valueOf(IEMCProxy.INSTANCE.getValue(what.toStack()));

        if (itemEmc.signum() <= 0) {
            return 0;
        }

        var totalEmc = itemEmc.multiply(BigInteger.valueOf(amount));
        var available = totalEmc.min(
                providers.stream().map(IKnowledgeProvider::getEmc).reduce(BigInteger.ZERO, BigInteger::add));

        amount = available.divide(itemEmc).longValue();

        if (amount == 0) {
            return 0;
        }

        if (mode == Actionable.MODULATE) {
            amount = getAmountAfterPowerExpenditure(available, itemEmc, energy);

            if (amount == 0) {
                return 0;
            }

            var withdrawn = BigInteger.ZERO;

            while (!providers.isEmpty() && withdrawn.compareTo(available) < 0) {
                Collections.shuffle(providers);

                var toWithdraw = available.subtract(withdrawn);
                var divisor = BigInteger.valueOf(providers.size());
                var quotient = toWithdraw.divide(divisor);
                var remainder = toWithdraw.remainder(divisor).longValue();

                for (var p = 0; p < providers.size(); p++) {
                    var provider = providers.get(p);

                    var currentEmc = provider.getEmc();
                    var toWithdrawFrom = quotient.add(p < remainder ? BigInteger.ONE : BigInteger.ZERO);

                    if (currentEmc.compareTo(toWithdrawFrom) <= 0) {
                        provider.setEmc(BigInteger.ZERO);
                        withdrawn = withdrawn.add(currentEmc);
                        // provider exhausted, remove from current list to re-extract deficit from remaining providers
                        providers.remove(provider);
                    } else {
                        provider.setEmc(currentEmc.subtract(toWithdrawFrom));
                        withdrawn = withdrawn.add(toWithdrawFrom);
                    }
                }
            }

            service.syncEmc();
        }

        return amount;
    }

    private static void distributeEmc(BigInteger totalEmc, List<IKnowledgeProvider> providers) {
        var divisor = BigInteger.valueOf(providers.size());
        var quotient = totalEmc.divide(divisor);
        var remainder = totalEmc.remainder(divisor).longValue();

        for (var p = 0; p < providers.size(); p++) {
            var provider = providers.get(p);
            var added = quotient.add(p < remainder ? BigInteger.ONE : BigInteger.ZERO);
            provider.setEmc(provider.getEmc().add(added));
        }
    }

    private List<IKnowledgeProvider> extractionProviders(IActionSource source) {
        if (source.player().isPresent() && AppliedEConfig.CONFIG.terminalExtractFromOwnEmcOnly()) {
            var provider = service.getProviderFor(source.player().get().getUUID());
            return provider != null ? List.of(provider) : List.of();
        } else {
            return service.getAllProviders();
        }
    }

    private static long getAmountAfterPowerExpenditure(BigInteger maxEmc, BigInteger itemEmc, IEnergySource energy) {
        var multiplier = BigDecimal.valueOf(PowerMultiplier.CONFIG.multiplier)
                .multiply(BigDecimal.valueOf(AppliedEConfig.CONFIG.getTransmutationPowerMultiplier()))
                .divide(BigDecimal.valueOf(EMCKeyType.TYPE.getAmountPerOperation()), 4, RoundingMode.HALF_UP);
        var toExpend = new BigDecimal(maxEmc).multiply(multiplier).min(BigDecimal.valueOf(Double.MAX_VALUE));

        var available = energy.extractAEPower(toExpend.doubleValue(), Actionable.SIMULATE, PowerMultiplier.ONE);
        var expended = Math.min(available, toExpend.doubleValue());
        var amount = BigDecimal.valueOf(available)
                .min(toExpend)
                .divide(multiplier, RoundingMode.HALF_UP)
                .toBigInteger()
                .divide(itemEmc)
                .longValue();

        if (amount > 0) {
            energy.extractAEPower(expended, Actionable.MODULATE, PowerMultiplier.ONE);
        }

        return amount;
    }

    private static void addKnowledge(AEItemKey what, IKnowledgeProvider provider, Player player) {
        var stack = what.toStack();
        provider.addKnowledge(stack);

        if (player instanceof ServerPlayer serverPlayer) {
            provider.syncKnowledgeChange(serverPlayer, ItemInfo.fromStack(stack), true);
        }
    }

    @Override
    public boolean isPreferredStorageFor(AEKey what, IActionSource source) {
        return source.machine().isPresent() && source.machine().get() instanceof TransmutationCapable;
    }

    @Override
    public Component getDescription() {
        return AppliedE.EMC_MODULE.get().getDescription();
    }
}
