package gripe._90.appliede.client;

import net.neoforged.fml.config.IConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec;

public class AppliedEClientConfig {
    public static final AppliedEClientConfig CONFIG;
    public static final IConfigSpec SPEC;

    static {
        var configured = new ModConfigSpec.Builder().configure(AppliedEClientConfig::new);
        CONFIG = configured.getKey();
        SPEC = configured.getValue();
    }

    private final ModConfigSpec.IntValue emcTierColours;

    private AppliedEClientConfig(ModConfigSpec.Builder builder) {
        emcTierColours = builder.comment(
                        "How many different colours should be used to represent higher tiers of EMC in storage")
                .defineInRange("emcTierColours", 10, 1, Integer.MAX_VALUE);
    }

    public int getEmcTierColours() {
        return emcTierColours.getAsInt();
    }
}
