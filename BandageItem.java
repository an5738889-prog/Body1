package com.ben.bodyinjury.item;

import com.ben.bodyinjury.capability.BodyCapability;
import com.ben.bodyinjury.capability.BodyPart;
import com.ben.bodyinjury.capability.InjuryLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class BandageItem extends Item {
    private final int healAmount;

    public BandageItem(Properties props) {
        this(props, 1);
    }

    public BandageItem(Properties props, int healAmount) {
        super(props);
        this.healAmount = healAmount;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            return InteractionResultHolder.success(stack);
        }

        return player.getCapability(BodyCapability.BODY).map(body -> {
            BodyPart target = null;
            int best = 0;
            for (BodyPart part : BodyPart.values()) {
                int lv = body.get(part);
                if (lv >= 1 && lv <= 3 && lv > best) {
                    best = lv;
                    target = part;
                }
            }

            if (target == null) {
                boolean hasCrit = false;
                for (BodyPart part : BodyPart.values()) {
                    if (body.get(part) >= 4) hasCrit = true;
                }
                if (hasCrit) {
                    player.displayClientMessage(Component.literal(
                            "§cThương nguy kịch không băng được. Cần táo vàng / táo vàng phù phép."), true);
                } else {
                    player.displayClientMessage(Component.literal("§7Không có chỗ cần băng."), true);
                }
                return InteractionResultHolder.fail(stack);
            }

            int before = body.get(target);
            for (int i = 0; i < healAmount; i++) {
                body.bandage(target);
            }
            int after = body.get(target);

            InjuryLevel b = InjuryLevel.from(before);
            InjuryLevel a = InjuryLevel.from(after);
            player.displayClientMessage(Component.literal(
                    "§7Băng §f" + target.getDisplay() + "§7: " +
                    b.getColor() + b.getDisplay() + " §7→ " + a.getColor() + a.getDisplay()), true);

            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            player.swing(hand, true);
            return InteractionResultHolder.consume(stack);
        }).orElse(InteractionResultHolder.pass(stack));
    }
}
