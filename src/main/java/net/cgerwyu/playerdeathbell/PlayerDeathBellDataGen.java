package net.cgerwyu.playerdeathbell;

import net.cgerwyu.playerdeathbell.datagen.ModModelProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(modid = PlayerDeathBell.MODID)
public class PlayerDeathBellDataGen {

    @SubscribeEvent
    public static void gatherClientData(GatherDataEvent.Client event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packoutput = generator.getPackOutput();

        generator.addProvider(true, new ModModelProvider(packoutput));
    }

}
