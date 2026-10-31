package com.ben.bodyinjury.item;

import com.ben.bodyinjury.BodyInjuryMod;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, BodyInjuryMod.MOD_ID);

    public static final RegistryObject<Item> BANDAGE = ITEMS.register("bandage",
            () -> new BandageItem(new Item.Properties().stacksTo(16)));

    public static final RegistryObject<Item> MEDKIT = ITEMS.register("medkit",
            () -> new BandageItem(new Item.Properties().stacksTo(8), 2)); // heals 2 levels

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}
