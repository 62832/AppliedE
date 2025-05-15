package gripe._90.appliede;

import net.neoforged.fml.config.IConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec;

public class AppliedEConfig {
    public static final AppliedEConfig CONFIG;
    public static final IConfigSpec SPEC;

    static {
        var configured = new ModConfigSpec.Builder().configure(AppliedEConfig::new);
        CONFIG = configured.getKey();
        SPEC = configured.getValue();
    }

    private final ModConfigSpec.DoubleValue moduleEnergyUsage;
    private final ModConfigSpec.DoubleValue transmutationPowerMultiplier;
    private final ModConfigSpec.IntValue emcPerByte;
    private final ModConfigSpec.BooleanValue terminalExtractFromOwnEmcOnly;
    private final ModConfigSpec.IntValue syncThrottleInterval;

    private AppliedEConfig(ModConfigSpec.Builder builder) {
        moduleEnergyUsage = builder.comment("The amount of AE energy per tick used by the ME Transmutation Module.")
                .defineInRange("moduleEnergyUsage", 25.0, 0, Double.MAX_VALUE);
        transmutationPowerMultiplier = builder.comment("The amount of AE energy used to transmute items, per 2000 EMC.")
                .defineInRange("transmutationPowerMultiplier", 1.0, 0, Double.MAX_VALUE);
        emcPerByte = builder.comment(
                        "The number of EMC units (of any tier) per byte as used in AE2 auto-crafting.",
                        "It is not recommended to set this very low, as this will require unreasonably large",
                        "amounts of crafting storage for some jobs.")
                .defineInRange("emcPerByte", 1000000, 1, Integer.MAX_VALUE);
        terminalExtractFromOwnEmcOnly = builder.comment(
                        "When extracting items from a Transmutation Terminal via EMC, deduct EMC only",
                        "from the player using the terminal rather than all players with modules attached.",
                        "This option does not cover re-insertion and conversion back into EMC.")
                .define("terminalExtractFromOwnEmcOnly", false);
        syncThrottleInterval = builder.comment(
                        "How many ticks to wait before the next player EMC sync when manipulating stored EMC.",
                        "It is not recommended to set this very low, as this may cause the server to hang when",
                        "carrying out a lot of repeated EMC insertion/extraction operations too quickly.")
                .defineInRange("syncThrottleInterval", 20, 1, 200);
    }

    public double getModuleEnergyUsage() {
        return moduleEnergyUsage.get();
    }

    public double getTransmutationPowerMultiplier() {
        return transmutationPowerMultiplier.get();
    }

    public int getEmcPerByte() {
        return emcPerByte.get();
    }

    public boolean terminalExtractFromOwnEmcOnly() {
        return terminalExtractFromOwnEmcOnly.get();
    }

    public int getSyncThrottleInterval() {
        return syncThrottleInterval.get();
    }
}
