package net.cgerwyu.playerdeathbell.network;

import net.cgerwyu.playerdeathbell.PlayerDeathBell;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public record DeathCounterPayload(
        List<Entry> entries,
        Optional<String> newLeaderName
) implements CustomPacketPayload {

    public static final Type<DeathCounterPayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(PlayerDeathBell.MODID, "death_counter")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, DeathCounterPayload> STREAM_CODEC =
            StreamCodec.of(DeathCounterPayload::write, DeathCounterPayload::read);

    public DeathCounterPayload {
        entries = List.copyOf(entries);
    }

    private static void write(RegistryFriendlyByteBuf buffer, DeathCounterPayload payload) {
        buffer.writeVarInt(payload.entries().size());

        for (Entry entry : payload.entries()) {
            buffer.writeUUID(entry.playerId());
            buffer.writeUtf(entry.playerName(), 16);
            buffer.writeVarInt(entry.deaths());
        }

        buffer.writeBoolean(payload.newLeaderName().isPresent());
        payload.newLeaderName().ifPresent(name -> buffer.writeUtf(name, 16));
    }

    private static DeathCounterPayload read(RegistryFriendlyByteBuf buffer) {
        int size = buffer.readVarInt();
        List<Entry> entries = new ArrayList<>(Math.min(size, 64));

        for (int i = 0; i < size; i++) {
            entries.add(new Entry(
                    buffer.readUUID(),
                    buffer.readUtf(16),
                    buffer.readVarInt()
            ));
        }

        Optional<String> newLeaderName = buffer.readBoolean()
                ? Optional.of(buffer.readUtf(16))
                : Optional.empty();

        return new DeathCounterPayload(entries, newLeaderName);
    }

    @Override
    public Type<DeathCounterPayload> type() {
        return TYPE;
    }

    public record Entry(UUID playerId, String playerName, int deaths) {
    }
}
