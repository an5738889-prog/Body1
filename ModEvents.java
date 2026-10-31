package com.ben.bodyinjury.event;

import com.ben.bodyinjury.BodyInjuryMod;
import com.ben.bodyinjury.capability.BodyCapability;
import com.ben.bodyinjury.capability.BodyPart;
import com.ben.bodyinjury.capability.InjuryLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Random;

@Mod.EventBusSubscriber(modid = BodyInjuryMod.MOD_ID)
public class ModEvents {
    private static final Random RANDOM = new Random();
    private static final ResourceLocation BODY_CAP =
            new ResourceLocation(BodyInjuryMod.MOD_ID, "body");

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            if (!event.getObject().getCapability(BodyCapability.BODY).isPresent()) {
                event.addCapability(BODY_CAP, new BodyCapability.Provider());
            }
        }
    }

    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event) {
        event.getOriginal().reviveCaps();
        event.getOriginal().getCapability(BodyCapability.BODY).ifPresent(oldCap -> {
            event.getEntity().getCapability(BodyCapability.BODY).ifPresent(newCap -> {
                for (BodyPart part : BodyPart.values()) {
                    newCap.set(part, oldCap.get(part));
                }
            });
        });
        event.getOriginal().invalidateCaps();
    }

    @SubscribeEvent
    public static void onHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        float amount = event.getAmount();
        if (amount < 1.5f) return;

        int add = 0;
        if (amount >= 12f) add = 4;
        else if (amount >= 8f) add = 3;
        else if (amount >= 5f) add = 2;
        else if (amount >= 2f) add = 1;
        if (add <= 0) return;

        BodyPart part = BodyPart.values()[RANDOM.nextInt(BodyPart.values().length)];
        String msg = event.getSource().getMsgId();
        if (msg != null && (msg.contains("fall") || msg.contains("explosion"))) {
            if (RANDOM.nextFloat() < 0.4f) {
                part = RANDOM.nextBoolean() ? BodyPart.LEFT_LEG : BodyPart.RIGHT_LEG;
            }
            if (amount >= 14f && RANDOM.nextFloat() < 0.35f) add = 4;
        }

        final int addFinal = add;
        final BodyPart partFinal = part;
        player.getCapability(BodyCapability.BODY).ifPresent(body -> {
            body.add(partFinal, addFinal);
            InjuryLevel lv = body.getLevel(partFinal);
            player.displayClientMessage(Component.literal(
                    lv.getColor() + partFinal.getDisplay() + " §7→ " + lv.getColor() + lv.getDisplay()), true);
        });
    }

    @SubscribeEvent
    public static void onFood(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ItemStack stack = event.getItem();
        if (!stack.is(Items.GOLDEN_APPLE) && !stack.is(Items.ENCHANTED_GOLDEN_APPLE)) return;

        player.getCapability(BodyCapability.BODY).ifPresent(body -> {
            body.healCritical();
            if (stack.is(Items.ENCHANTED_GOLDEN_APPLE)) {
                for (BodyPart part : BodyPart.values()) {
                    if (body.get(part) > 0) body.set(part, Math.max(0, body.get(part) - 1));
                }
            }
            player.displayClientMessage(Component.literal(
                    "§6[Y tế] §aĐã điều trị thương nặng bằng táo vàng."), true);
            sendBodyStatus(player, body);
        });
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player)) return;
        if (player.tickCount % 40 != 0) return;

        player.getCapability(BodyCapability.BODY).ifPresent(body -> {
            int legs = Math.max(body.get(BodyPart.LEFT_LEG), body.get(BodyPart.RIGHT_LEG));
            int arms = Math.max(body.get(BodyPart.LEFT_ARM), body.get(BodyPart.RIGHT_ARM));
            int head = body.get(BodyPart.HEAD);
            int chest = body.get(BodyPart.CHEST);

            if (legs >= 3)
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 50, legs >= 4 ? 2 : 1, false, false));
            if (arms >= 3)
                player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 50, arms >= 4 ? 1 : 0, false, false));
            if (head >= 3)
                player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 60, 0, false, false));
            if (chest >= 3)
                player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 50, chest >= 4 ? 1 : 0, false, false));
            if (head >= 4 || chest >= 4)
                player.addEffect(new MobEffectInstance(MobEffects.WITHER, 40, 0, false, false));
        });
    }

    public static void sendBodyStatus(Player player, BodyCapability body) {
        player.sendSystemMessage(Component.literal("§8§l—— Tình trạng cơ thể ——"));
        for (BodyPart part : BodyPart.values()) {
            InjuryLevel lv = body.getLevel(part);
            player.sendSystemMessage(Component.literal(
                    lv.getColor() + part.getDisplay() + ": " + lv.getDisplay()));
        }
    }
}
