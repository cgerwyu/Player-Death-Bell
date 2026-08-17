package net.cgerwyu.playerdeathbell.ModSounds;

import net.cgerwyu.playerdeathbell.PlayerDeathBell;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModSounds {

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(
                    Registries.SOUND_EVENT,
                    PlayerDeathBell.MODID
            );

    public static final DeferredHolder<SoundEvent, SoundEvent> PLAYER_DEATH_BELL =
            SOUND_EVENTS.register(
                    "death_bell",
                    SoundEvent::createVariableRangeEvent
            );

    public static void register(IEventBus modEventBus) {
        SOUND_EVENTS.register(modEventBus);
    }

    private ModSounds() {}

}
