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
    private final ForgeConfigSpec.BooleanValue terminalExtractFromOwnEmcOnly;

    private AppliedEConfig(ForgeConfigSpec.Builder builder) {
        moduleEnergyUsage = builder.comment("The amount of AE energy per tick used by the ME Transmutation Module.")
                .defineInRange("moduleEnergyUsage", 25.0, 0, Double.MAX_VALUE);
        terminalExtractFromOwnEmcOnly = builder.comment(
                        "When extracting items from a Transmutation Terminal via EMC, deduct EMC only",
                        "from the player using the terminal rather than all players with modules attached.",
                        "This option does not cover re-insertion and conversion back into EMC.")
                .define("terminalExtractFromOwnEmcOnly", false);
    }

    public double getModuleEnergyUsage() {
        return moduleEnergyUsage.get();
    }

    public boolean terminalExtractFromOwnEmcOnly() {
        return terminalExtractFromOwnEmcOnly.get();
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
