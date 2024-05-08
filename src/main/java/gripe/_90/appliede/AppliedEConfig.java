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

    public AppliedEConfig(ForgeConfigSpec.Builder builder) {
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
}
