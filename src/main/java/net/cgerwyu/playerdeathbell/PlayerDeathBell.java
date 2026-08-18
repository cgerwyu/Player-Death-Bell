package net.cgerwyu.playerdeathbell;

import net.cgerwyu.playerdeathbell.ModSounds.ModSounds;
import net.cgerwyu.playerdeathbell.command.PlayerDeathBellCommand;
import net.cgerwyu.playerdeathbell.death.DeathCounterService;
import net.cgerwyu.playerdeathbell.network.ModNetworking;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@Mod(PlayerDeathBell.MODID)
public class PlayerDeathBell {

    public static final String MODID = "playerdeathbell";

    public PlayerDeathBell(
            IEventBus modEventBus,
            ModContainer modContainer
    ) {
        ModSounds.register(modEventBus);
        ModNetworking.register(modEventBus);

        modContainer.registerConfig(
                ModConfig.Type.SERVER,
                Config.SPEC
        );

        NeoForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer deadPlayer) {
            DeathCounterService.recordDeath(deadPlayer);

            if (Config.ENABLED.get()) {
                playDeathSoundForEveryone(deadPlayer);
            }
        }
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            DeathCounterService.playerJoined(player);
        }
    }

    @SubscribeEvent
    public void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            DeathCounterService.playerLeft(player);
        }
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        PlayerDeathBellCommand.register(event.getDispatcher());
    }

    private void playDeathSoundForEveryone(ServerPlayer deadPlayer) {
        long seed = deadPlayer.getRandom().nextLong();

        for (ServerPlayer listener :
                deadPlayer.level().getServer().getPlayerList().getPlayers()) {

            listener.connection.send(
                    new ClientboundSoundPacket(
                            ModSounds.PLAYER_DEATH_BELL,
                            SoundSource.PLAYERS,
                            listener.getX(),
                            listener.getY(),
                            listener.getZ(),
                            1.0F,
                            1.0F,
                            seed
                    )
            );
        }
    }

}
