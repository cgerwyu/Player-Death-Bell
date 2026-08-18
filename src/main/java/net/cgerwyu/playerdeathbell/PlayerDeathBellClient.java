package net.cgerwyu.playerdeathbell;

import net.cgerwyu.playerdeathbell.client.ClientDeathCounterState;
import net.cgerwyu.playerdeathbell.client.DeathCounterHud;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = PlayerDeathBell.MODID, dist = Dist.CLIENT)
public final class PlayerDeathBellClient {

    public PlayerDeathBellClient(IEventBus modEventBus) {
        modEventBus.addListener(DeathCounterHud::registerGuiLayers);
        modEventBus.addListener(ClientDeathCounterState::registerPayloadHandler);
        NeoForge.EVENT_BUS.addListener(ClientDeathCounterState::onLoggingOut);
    }
}
