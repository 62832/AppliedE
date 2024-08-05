package gripe._90.appliede;

import net.minecraftforge.common.ForgeConfigSpec;

public class AppliedEConfig {
    public static final AppliedEConfig CONFIG;
    public static final ForgeConfigSpec SPEC;

    static {
        var configured = new ForgeConfigSpec.Builder().configure(AppliedEConfig::new);
        CONFIG = configured.getKey();
        SPEC = configured.getValue();
    }

    private final ForgeConfigSpec.DoubleValue moduleEnergyUsage;
    private final ForgeConfigSpec.DoubleValue transmutationPowerMultiplier;
    private final ForgeConfigSpec.IntValue emcPerByte;
    private final ForgeConfigSpec.BooleanValue terminalExtractFromOwnEmcOnly;
    private final ForgeConfigSpec.IntValue syncThrottleInterval;

    private AppliedEConfig(ForgeConfigSpec.Builder builder) {
        moduleEnergyUsage = builder.comment("The amount of AE energy per tick used by the ME Transmutation Module.")
                .defineInRange("moduleEnergyUsage", 25.0, 0, Double.MAX_VALUE);
        transmutationPowerMultiplier = builder.comment(
                        "The amount of AE energy used to transmute 1 EMC through the ME Transmutation Module.")
                .defineInRange("transmutationPowerMultiplier", 1.0, 0, Double.MAX_VALUE);
        emcPerByte = builder.comment(
                        "The number of EMC units (of any tier) per byte as used in AE2 auto-crafting.",
                        "It is not recommended to set this very low as this will require unreasonably large",
                        "amounts of crafting storage for some jobs.")
                .defineInRange("emcPerByte", 1000000, 1, Integer.MAX_VALUE);
        terminalExtractFromOwnEmcOnly = builder.comment(
                        "When extracting items from a Transmutation Terminal via EMC, deduct EMC only",
                        "from the player using the terminal rather than all players with modules attached.",
                        "This option does not cover re-insertion and conversion back into EMC.")
                .define("terminalExtractFromOwnEmcOnly", false);
        syncThrottleInterval = builder.comment(
                        "How many ticks to wait before the next player EMC sync when manipulating stored EMC.")
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

    public static class Client {
        public static final Client CONFIG;
        public static final ForgeConfigSpec SPEC;

        static {
            var configured = new ForgeConfigSpec.Builder().configure(Client::new);
            CONFIG = configured.getKey();
            SPEC = configured.getValue();
        }

        private final ForgeConfigSpec.IntValue emcTierColours;

        private Client(ForgeConfigSpec.Builder builder) {
            emcTierColours = builder.comment(
                            "How many different colours should be used to represent higher tiers of EMC in storage")
                    .defineInRange("emcTierColours", 10, 1, Integer.MAX_VALUE);
        }

        public ForgeConfigSpec.IntValue getEmcTierColours() {
            return emcTierColours;
        }
    }
}
