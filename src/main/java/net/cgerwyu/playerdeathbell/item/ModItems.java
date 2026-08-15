package net.cgerwyu.playerdeathbell.item;

import net.cgerwyu.playerdeathbell.PlayerDeathBell;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(PlayerDeathBell.MODID);

    public static final DeferredItem<Item> AZURITE = ITEMS.registerSimpleItem("azurite");

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }

}
