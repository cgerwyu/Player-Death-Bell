package net.cgerwyu.playerdeathbell;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    public static final ModConfigSpec.BooleanValue ENABLED;
    public static final ModConfigSpec SPEC;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        ENABLED = builder
                .comment("Play the death bell sound when a player dies")
                .define("enabled", true);

        SPEC = builder.build();
    }

    private Config() {
    }

}
