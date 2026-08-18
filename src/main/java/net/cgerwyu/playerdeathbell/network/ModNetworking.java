package net.cgerwyu.playerdeathbell.network;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public final class ModNetworking {

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(ModNetworking::registerPayloadHandlers);
    }

    private static void registerPayloadHandlers(RegisterPayloadHandlersEvent event) {
        event.registrar("1")
                .playToClient(DeathCounterPayload.TYPE, DeathCounterPayload.STREAM_CODEC);
    }

    private ModNetworking() {
    }
}
