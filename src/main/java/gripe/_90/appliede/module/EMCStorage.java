package gripe._90.appliede.module;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;

import net.minecraft.network.chat.Component;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;

import gripe._90.appliede.AppliedE;
import gripe._90.appliede.key.EMCKey;

public record EMCStorage(KnowledgeService service) implements MEStorage {
    @Override
    public void getAvailableStacks(KeyCounter out) {
        var emc = service.getKnowledge().getEmc();
        var currentTier = 1;

        while (emc.divide(AppliedE.TIER_LIMIT).signum() == 1) {
            out.add(new EMCKey(currentTier), emc.remainder(AppliedE.TIER_LIMIT).longValue());
            emc = emc.divide(AppliedE.TIER_LIMIT);
            currentTier++;
        }

        out.add(new EMCKey(currentTier), emc.min(AppliedE.TIER_LIMIT).longValue());
    }

    @Override
    public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
        if (amount == 0 || !(what instanceof EMCKey emc)) {
            return 0;
        }

        if (mode == Actionable.MODULATE) {
            var bigAmount = BigInteger.valueOf(amount);
            var multiplier = AppliedE.TIER_LIMIT.pow(emc.getTier() - 1);

            var knowledge = service.getKnowledge();
            var toInsert = bigAmount.multiply(multiplier);

            var providers = new ArrayList<>(knowledge.getProviders());
            Collections.shuffle(providers);

            var divisor = BigInteger.valueOf(knowledge.getProviders().size());
            var quotient = toInsert.divide(divisor);
            var remainder = toInsert.remainder(divisor).longValue();

            for (var p = 0; p < providers.size(); p++) {
                var provider = providers.get(p).get();
                provider.setEmc(provider.getEmc().add(quotient.add(p < remainder ? BigInteger.ONE : BigInteger.ZERO)));
            }

            service.syncEmc();
        }

        return amount;
    }

    @Override
    public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
        if (amount == 0 || !(what instanceof EMCKey emc)) {
            return 0;
        }

        var knowledge = service.getKnowledge();
        var extracted = 0L;
        var multiplier = AppliedE.TIER_LIMIT.pow(emc.getTier() - 1);

        var providers = new ArrayList<>(knowledge.getProviders());

        while (!providers.isEmpty() && extracted < amount) {
            Collections.shuffle(providers);

            var toExtract = BigInteger.valueOf(amount - extracted).multiply(multiplier);
            var divisor = BigInteger.valueOf(knowledge.getProviders().size());
            var quotient = toExtract.divide(divisor);
            var remainder = toExtract.remainder(divisor).longValue();

            for (var p = 0; p < providers.size(); p++) {
                var providerSupplier = providers.get(p);
                var provider = providerSupplier.get();

                var currentEmc = provider.getEmc();
                var toExtractFrom = quotient.add(p < remainder ? BigInteger.ONE : BigInteger.ZERO);

                if (currentEmc.compareTo(toExtractFrom) <= 0) {
                    if (mode == Actionable.MODULATE) {
                        provider.setEmc(BigInteger.ZERO);
                    }

                    extracted += currentEmc.divide(multiplier).longValue();
                    // provider exhausted, remove from providers and re-extract deficit from remaining providers
                    providers.remove(providerSupplier);
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

    @Override
    public Component getDescription() {
        return AppliedE.EMC_MODULE.get().getDescription();
    }
}
