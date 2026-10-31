package com.ben.bodyinjury.capability;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;



import java.util.EnumMap;
import java.util.Map;

public class BodyCapability {
    public static final Capability<BodyCapability> BODY =
            CapabilityManager.get(new CapabilityToken<>() {});

    private final Map<BodyPart, Integer> injuries = new EnumMap<>(BodyPart.class);

    public BodyCapability() {
        for (BodyPart part : BodyPart.values()) {
            injuries.put(part, 0);
        }
    }

    public int get(BodyPart part) {
        return injuries.getOrDefault(part, 0);
    }

    public InjuryLevel getLevel(BodyPart part) {
        return InjuryLevel.from(get(part));
    }

    public void set(BodyPart part, int level) {
        injuries.put(part, Math.max(0, Math.min(4, level)));
    }

    public void add(BodyPart part, int amount) {
        set(part, get(part) + amount);
    }

    /** Bandage: heal 1 level if 1-3. Returns true if used. */
    public boolean bandage(BodyPart part) {
        int lv = get(part);
        if (lv >= 1 && lv <= 3) {
            set(part, lv - 1);
            return true;
        }
        return false;
    }

    /** Auto bandage worst treatable part (red > orange > yellow). */
    public BodyPart bandageWorst() {
        BodyPart best = null;
        int bestLv = 0;
        for (BodyPart part : BodyPart.values()) {
            int lv = get(part);
            if (lv >= 1 && lv <= 3 && lv > bestLv) {
                bestLv = lv;
                best = part;
            }
        }
        if (best != null) bandage(best);
        return best;
    }

    public void healCritical() {
        for (BodyPart part : BodyPart.values()) {
            if (get(part) >= 4) set(part, 0);
            else if (get(part) > 0) set(part, Math.max(0, get(part) - 2));
        }
    }

    public void healAll() {
        for (BodyPart part : BodyPart.values()) set(part, 0);
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        for (BodyPart part : BodyPart.values()) {
            tag.putInt(part.getId(), get(part));
        }
        return tag;
    }

    public void load(CompoundTag tag) {
        for (BodyPart part : BodyPart.values()) {
            if (tag.contains(part.getId())) {
                set(part, tag.getInt(part.getId()));
            }
        }
    }

    public static class Provider implements ICapabilitySerializable<Tag> {
        private final BodyCapability data = new BodyCapability();
        private final LazyOptional<BodyCapability> optional = LazyOptional.of(() -> data);

        @Override
        public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
            return BODY.orEmpty(cap, optional);
        }

        @Override
        public Tag serializeNBT() {
            return data.save();
        }

        @Override
        public void deserializeNBT(Tag nbt) {
            if (nbt instanceof CompoundTag compound) {
                data.load(compound);
            }
        }
    }
}
