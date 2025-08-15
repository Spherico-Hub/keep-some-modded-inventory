package dev.mageowl.keepsomemoddedinventory;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.UUID;

public class DeathSavedData extends SavedData {
    private final HashMap<UUID, DeathInventory> deathInventoryMap;

    public DeathSavedData(HashMap<UUID, DeathInventory> deathInventoryMap) {
        this.deathInventoryMap = deathInventoryMap;
    }

    public static DeathSavedData create() {
        return new DeathSavedData(new HashMap<>());
    }

    public static DeathSavedData load(CompoundTag tag, HolderLookup.Provider provider) {
        HashMap<UUID, DeathInventory> data = new HashMap<>();

        for (String key : tag.getAllKeys()) {
            try {
                UUID uuid = UUID.fromString(key);
                DeathInventory inv = DeathInventory.load(tag.getList(key, Tag.TAG_COMPOUND), provider);
                data.put(uuid, inv);
            } catch (ClassCastException _e) {
                KeepSomeModdedInventory.LOGGER.warn("Invalid death inventory.");
            }
        }

        return new DeathSavedData(data);
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider provider) {
        for (UUID key : deathInventoryMap.keySet()) {
            tag.put(key.toString(), deathInventoryMap.get(key).save(new ListTag(41), provider));
        }

        return tag;
    }

    public void put(UUID uuid, DeathInventory inventory) {
        deathInventoryMap.put(uuid, inventory);
        setDirty();
    }
    public DeathInventory get(UUID uuid) {
        return deathInventoryMap.get(uuid);
    }
    public DeathInventory remove(UUID uuid) {
        setDirty();
        return deathInventoryMap.remove(uuid);
    }
}
