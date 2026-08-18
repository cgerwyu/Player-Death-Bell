package net.cgerwyu.playerdeathbell.client;

import net.cgerwyu.playerdeathbell.network.DeathCounterPayload;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

public final class ClientDeathCounterState {

    private static List<DeathCounterPayload.Entry> entries = List.of();

    public static void registerPayloadHandler(RegisterClientPayloadHandlersEvent event) {
        event.register(DeathCounterPayload.TYPE, ClientDeathCounterState::handlePayload);
    }

    public static void handlePayload(DeathCounterPayload payload, IPayloadContext context) {
        entries = payload.entries();
        payload.newLeaderName().ifPresent(DeathCounterHud::announceNewLeader);
    }

    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        entries = List.of();
        DeathCounterHud.reset();
    }

    public static List<DeathCounterPayload.Entry> entries() {
        return entries;
    }

    private ClientDeathCounterState() {
    }
}
