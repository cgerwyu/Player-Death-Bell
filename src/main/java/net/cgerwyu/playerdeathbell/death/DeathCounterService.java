package net.cgerwyu.playerdeathbell.death;

import net.cgerwyu.playerdeathbell.network.DeathCounterPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class DeathCounterService {

    public static void recordDeath(ServerPlayer player) {
        MinecraftServer server = player.level().getServer();
        DeathCounterSavedData data = DeathCounterSavedData.get(server);
        UUID previousLeader = findLeader(server, data, null);

        data.incrementDeaths(player);

        UUID newLeader = findLeader(server, data, null);
        Optional<String> announcement = previousLeader != null
                && newLeader != null
                && !previousLeader.equals(newLeader)
                ? Optional.of(player.getGameProfile().name())
                : Optional.empty();

        sync(server, null, announcement);
    }

    public static void playerJoined(ServerPlayer player) {
        MinecraftServer server = player.level().getServer();
        DeathCounterSavedData.get(server).rememberPlayer(player);
        sync(server, null, Optional.empty());
    }

    public static void playerLeft(ServerPlayer player) {
        sync(player.level().getServer(), player.getUUID(), Optional.empty());
    }

    private static void sync(
            MinecraftServer server,
            UUID excludedPlayerId,
            Optional<String> leaderAnnouncement
    ) {
        DeathCounterSavedData data = DeathCounterSavedData.get(server);

        DeathCounterPayload payload = new DeathCounterPayload(
                createRanking(server, data, excludedPlayerId),
                leaderAnnouncement
        );

        for (ServerPlayer listener : server.getPlayerList().getPlayers()) {
            if (!listener.getUUID().equals(excludedPlayerId)) {
                PacketDistributor.sendToPlayer(listener, payload);
            }
        }
    }

    private static UUID findLeader(
            MinecraftServer server,
            DeathCounterSavedData data,
            UUID excludedPlayerId
    ) {
        List<DeathCounterPayload.Entry> ranking = createRanking(server, data, excludedPlayerId);
        return ranking.isEmpty() ? null : ranking.getFirst().playerId();
    }

    private static List<DeathCounterPayload.Entry> createRanking(
            MinecraftServer server,
            DeathCounterSavedData data,
            UUID excludedPlayerId
    ) {
        return server.getPlayerList().getPlayers().stream()
                .filter(player -> !player.getUUID().equals(excludedPlayerId))
                .map(player -> new DeathCounterPayload.Entry(
                        player.getUUID(),
                        player.getGameProfile().name(),
                        data.getDeaths(player.getUUID())
                ))
                .sorted(
                        Comparator.comparingInt(DeathCounterPayload.Entry::deaths)
                                .reversed()
                                .thenComparing(
                                        DeathCounterPayload.Entry::playerName,
                                        String.CASE_INSENSITIVE_ORDER
                                )
                )
                .toList();
    }

    private DeathCounterService() {
    }
}
