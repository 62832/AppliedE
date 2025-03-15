package gripe._90.appliede.me.service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.features.IPlayerRegistry;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;

import gripe._90.appliede.AppliedE;
import gripe._90.appliede.AppliedEConfig;
import gripe._90.appliede.me.key.EMCKey;
import gripe._90.appliede.me.key.EMCKeyType;
import gripe._90.appliede.menu.TransmutationTerminalMenu;

import moze_intel.projecte.api.ItemInfo;
import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import moze_intel.projecte.api.proxy.IEMCProxy;

public final class EMCStorage implements MEStorage {
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
            out.add(EMCKey.of(currentTier), emc.remainder(AppliedE.TIER_LIMIT).longValue());
            emc = emc.divide(AppliedE.TIER_LIMIT);
            currentTier++;
        }

        out.add(EMCKey.of(currentTier), emc.longValue());

        if (highestTier != currentTier) {
            highestTier = currentTier;
            service.updatePatterns();
        }
    }

    @Override
    public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
        if (amount <= 0
                || !(what instanceof EMCKey emc)
                || service.getProviders().isEmpty()) {
            return 0;
        }

        if (mode == Actionable.MODULATE) {
            var providers = new ArrayList<>(service.getProviders());
            Collections.shuffle(providers);

            if (emc.getTier() == 1) {
                var divisor = service.getProviders().size();
                var quotient = amount / divisor;
                var remainder = amount % divisor;

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
        if (amount <= 0 || service.getProviders().isEmpty()) {
            return 0;
        }

        if (what instanceof AEItemKey item && source.player().isPresent()) {
            return extractItem(item, amount, mode, source, false);
        }

        if (!(what instanceof EMCKey emc)) {
            return 0;
        }

        var multiplier = AppliedE.TIER_LIMIT.pow(emc.getTier() - 1);
        var rawEmc = BigInteger.valueOf(amount).multiply(multiplier);
        var extracted = BigInteger.ZERO;

        var providers = getProvidersForExtraction(source);

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

    public long insertItem(
            AEItemKey what,
            long amount,
            Actionable mode,
            IActionSource source,
            boolean mayLearn,
            boolean consumePower,
            Runnable onLearn) {
        if (amount <= 0 || service.getProviders().isEmpty()) {
            return 0;
        }

        if (!mayLearn && !service.getKnownItems().contains(what) || !IEMCProxy.INSTANCE.hasValue(what.toStack())) {
            return 0;
        }

        var player = source.player().orElse(null);
        var machine = source.machine().orElse(null);
        var playerProvider = player != null ? service.getProviderFor(player) : null;
        var machineProvider = machine != null ? service.getProviderFor(machine) : null;

        if (mayLearn) {
            if (player != null && playerProvider == null) {
                return 0;
            }

            if (machine != null && machineProvider == null) {
                return 0;
            }
        }

        if (mode == Actionable.MODULATE) {
            var itemEmc = BigInteger.valueOf(IEMCProxy.INSTANCE.getSellValue(what.toStack()));
            var totalEmc = itemEmc.multiply(BigInteger.valueOf(amount));

            if (consumePower) {
                amount = getAmountAfterPowerExpenditure(totalEmc, itemEmc);
            }

            if (amount == 0) {
                return 0;
            }

            var providers = new ArrayList<>(service.getProviders());
            Collections.shuffle(providers);
            distributeEmc(totalEmc, providers);
            service.syncEmc();

            if (mayLearn) {
                if (player != null && !playerProvider.get().hasKnowledge(what.toStack())) {
                    addKnowledge(what, playerProvider.get(), player);
                    onLearn.run();
                }

                if (machine != null && !machineProvider.get().hasKnowledge(what.toStack())) {
                    var node = Objects.requireNonNull(machine.getActionableNode());
                    var owner = IPlayerRegistry.getConnected(node.getLevel().getServer(), node.getOwningPlayerId());
                    addKnowledge(what, machineProvider.get(), owner);
                    onLearn.run();
                }
            }
        }

        return amount;
    }

    public long insertItem(AEItemKey what, long amount, Actionable mode, IActionSource source, boolean mayLearn) {
        return insertItem(what, amount, mode, source, mayLearn, true, () -> {});
    }

    public long extractItem(AEItemKey what, long amount, Actionable mode, IActionSource source, boolean skipStored) {
        if (source.player().isPresent()
                && !(source.player().get().containerMenu instanceof TransmutationTerminalMenu)) {
            return 0;
        }

        if (amount <= 0 || !service.getKnownItems().contains(what)) {
            return 0;
        }

        var existingStored = service.getGrid().getStorageService().getCachedInventory();

        if (!skipStored && existingStored.get(what) > 0) {
            return 0;
        }

        var itemEmc = BigInteger.valueOf(IEMCProxy.INSTANCE.getValue(what.toStack()));

        if (itemEmc.signum() <= 0) {
            return 0;
        }

        var totalEmc = itemEmc.multiply(BigInteger.valueOf(amount));

        var providers = getProvidersForExtraction(source);
        var availableEmc = totalEmc.min(
                providers.equals(service.getProviders())
                        ? service.getEmc()
                        : providers.getFirst().getEmc());

        amount = availableEmc.divide(itemEmc).longValue();

        if (amount == 0) {
            return 0;
        }

        if (mode == Actionable.MODULATE) {
            amount = getAmountAfterPowerExpenditure(availableEmc, itemEmc);

            if (amount == 0) {
                return 0;
            }

            var withdrawn = BigInteger.ZERO;

            while (!providers.isEmpty() && withdrawn.compareTo(availableEmc) < 0) {
                Collections.shuffle(providers);

                var toWithdraw = availableEmc.subtract(withdrawn);
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

    private void distributeEmc(BigInteger totalEmc, ArrayList<IKnowledgeProvider> providers) {
        var divisor = BigInteger.valueOf(service.getProviders().size());
        var quotient = totalEmc.divide(divisor);
        var remainder = totalEmc.remainder(divisor).longValue();

        for (var p = 0; p < providers.size(); p++) {
            var provider = providers.get(p);
            var added = quotient.add(p < remainder ? BigInteger.ONE : BigInteger.ZERO);
            provider.setEmc(provider.getEmc().add(added));
        }
    }

    private List<IKnowledgeProvider> getProvidersForExtraction(IActionSource source) {
        var providers = new ArrayList<IKnowledgeProvider>();

        if (source.player().isPresent() && AppliedEConfig.CONFIG.terminalExtractFromOwnEmcOnly()) {
            var provider = service.getProviderFor(source.player().get());
            providers.add(provider.get());
        } else {
            providers.addAll(service.getProviders());
        }

        return providers;
    }

    private long getAmountAfterPowerExpenditure(BigInteger maxEmc, BigInteger itemEmc) {
        var energyService = service.getGrid().getEnergyService();
        var multiplier = BigDecimal.valueOf(PowerMultiplier.CONFIG.multiplier)
                .multiply(BigDecimal.valueOf(AppliedEConfig.CONFIG.getTransmutationPowerMultiplier()))
                .divide(BigDecimal.valueOf(EMCKeyType.TYPE.getAmountPerOperation()), 4, RoundingMode.HALF_UP);
        var toExpend = new BigDecimal(maxEmc).multiply(multiplier).min(BigDecimal.valueOf(Double.MAX_VALUE));

        var available = energyService.extractAEPower(toExpend.doubleValue(), Actionable.SIMULATE, PowerMultiplier.ONE);
        var expended = Math.min(available, toExpend.doubleValue());
        var amount = BigDecimal.valueOf(available)
                .min(toExpend)
                .divide(multiplier, RoundingMode.HALF_UP)
                .toBigInteger()
                .divide(itemEmc)
                .longValue();

        if (amount > 0) {
            energyService.extractAEPower(expended, Actionable.MODULATE, PowerMultiplier.ONE);
        }

        return amount;
    }

    private void addKnowledge(AEItemKey what, IKnowledgeProvider provider, Player player) {
        var stack = what.toStack();
        provider.addKnowledge(stack);

        if (player instanceof ServerPlayer serverPlayer) {
            provider.syncKnowledgeChange(serverPlayer, ItemInfo.fromStack(stack), true);
        }
    }

    int getHighestTier() {
        return highestTier;
    }

    @Override
    public Component getDescription() {
        return AppliedE.EMC_MODULE.get().getDescription();
    }
}
