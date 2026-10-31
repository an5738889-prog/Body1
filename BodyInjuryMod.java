package com.ben.bodyinjury;

import com.ben.bodyinjury.capability.BodyCapability;
import com.ben.bodyinjury.item.ModItems;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import com.ben.bodyinjury.event.ModEvents;

@Mod(BodyInjuryMod.MOD_ID)
public class BodyInjuryMod {
    public static final String MOD_ID = "bodyinjury";

    public BodyInjuryMod() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModItems.register(modBus);
        modBus.addListener(this::onRegisterCaps);
        MinecraftForge.EVENT_BUS.addListener(this::onRegisterCommands);
    }

    private void onRegisterCaps(RegisterCapabilitiesEvent event) {
        event.register(BodyCapability.class);
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> d = event.getDispatcher();

        d.register(Commands.literal("body").executes(ctx -> {
            if (!(ctx.getSource().getEntity() instanceof ServerPlayer player)) return 0;
            player.getCapability(BodyCapability.BODY).ifPresent(body ->
                    ModEvents.sendBodyStatus(player, body));
            return 1;
        }));

        d.register(Commands.literal("bodyreset")
                .requires(s -> s.hasPermission(2))
                .executes(ctx -> {
                    if (!(ctx.getSource().getEntity() instanceof ServerPlayer player)) return 0;
                    player.getCapability(BodyCapability.BODY).ifPresent(body -> {
                        body.healAll();
                        player.displayClientMessage(Component.literal("§7Đã reset toàn bộ thương tích."), true);
                    });
                    return 1;
                }));
    }
}
