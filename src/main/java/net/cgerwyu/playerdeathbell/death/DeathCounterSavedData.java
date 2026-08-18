package net.cgerwyu.playerdeathbell.death;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.cgerwyu.playerdeathbell.PlayerDeathBell;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class DeathCounterSavedData extends SavedData {

    private static final Codec<DeathCounterSavedData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    StoredPlayer.CODEC.listOf()
                            .optionalFieldOf("players", List.of())
                            .forGetter(DeathCounterSavedData::storedPlayers)
            ).apply(instance, DeathCounterSavedData::new)
    );

    private static final SavedDataType<DeathCounterSavedData> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath(PlayerDeathBell.MODID, "death_counts"),
            DeathCounterSavedData::new,
            CODEC
    );

    private final Map<UUID, StoredPlayer> players = new LinkedHashMap<>();

    public DeathCounterSavedData() {
    }

    private DeathCounterSavedData(List<StoredPlayer> storedPlayers) {
        for (StoredPlayer player : storedPlayers) {
            players.put(player.playerId(), player);
        }
    }

    public static DeathCounterSavedData get(MinecraftServer server) {
        return server.getDataStorage().computeIfAbsent(TYPE);
    }

    public void rememberPlayer(ServerPlayer player) {
        UUID playerId = player.getUUID();
        String playerName = player.getGameProfile().name();
        StoredPlayer oldValue = players.get(playerId);

        if (oldValue == null || !oldValue.playerName().equals(playerName)) {
            int deaths = oldValue == null ? 0 : oldValue.deaths();
            players.put(playerId, new StoredPlayer(playerId, playerName, deaths));
            setDirty();
        }
    }

    public void incrementDeaths(ServerPlayer player) {
        UUID playerId = player.getUUID();
        StoredPlayer oldValue = players.get(playerId);
        int deaths = oldValue == null ? 1 : oldValue.deaths() + 1;

        players.put(
                playerId,
                new StoredPlayer(playerId, player.getGameProfile().name(), deaths)
        );
        setDirty();
    }

    public int getDeaths(UUID playerId) {
        StoredPlayer player = players.get(playerId);
        return player == null ? 0 : player.deaths();
    }

    private List<StoredPlayer> storedPlayers() {
        return List.copyOf(players.values());
    }

    private record StoredPlayer(UUID playerId, String playerName, int deaths) {

        private static final Codec<StoredPlayer> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        UUIDUtil.CODEC.fieldOf("id").forGetter(StoredPlayer::playerId),
                        Codec.STRING.fieldOf("name").forGetter(StoredPlayer::playerName),
                        Codec.INT.fieldOf("deaths").forGetter(StoredPlayer::deaths)
                ).apply(instance, StoredPlayer::new)
        );
    }
}
