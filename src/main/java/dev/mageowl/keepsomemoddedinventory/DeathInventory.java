package dev.mageowl.keepsomemoddedinventory;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.NotNull;

public class DeathInventory implements INBTSerializable<CompoundTag> {
    private final NonNullList<ItemStack> items;

    public static DeathInventory empty() {
        return new DeathInventory(NonNullList.withSize(41, ItemStack.EMPTY));
    }
    public DeathInventory(ServerPlayer player) {
        this(NonNullList.createWithCapacity(41));
        Inventory inventory = player.getInventory();
        items.addAll(inventory.items);
        items.addAll(inventory.armor);
        items.addAll(inventory.offhand);

        KeepSomeModdedInventory.LOGGER.info("Filling inventory with {} item stacks", items.size());
    }
    private DeathInventory(NonNullList<ItemStack> items) {
        this.items = items;
    }

    public void remove(ItemStack item) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i) == item) {
                items.set(i, ItemStack.EMPTY);
                return;
            }
        }
    }

    public void restore(ServerPlayer player) {
        KeepSomeModdedInventory.LOGGER.info("Restoring inventory with {} items", items.size());
        Inventory inventory = player.getInventory();

        for (int i = 0; i < items.size(); i++) {
            if (inventory.getItem(i).isEmpty())
                inventory.setItem(i, items.get(i));
            else {
                KeepSomeModdedInventory.LOGGER.info("Slot {} was non-empty", i);
                int slot = inventory.getFreeSlot();
                if (slot == -1) {
                    player.drop(items.get(i), true, false);
                } else inventory.setItem(slot, items.get(i));
            }
        }
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.@NotNull Provider provider) {
        CompoundTag tag = new CompoundTag();

        ListTag itemsTag = new ListTag(this.items.size());
        for (int i = 0; i < items.size(); i++) {
            ItemStack item = items.get(i);
            if (!item.isEmpty()) {
                CompoundTag compoundTag = new CompoundTag();
                compoundTag.putInt("Slot", i);
                itemsTag.add(item.save(provider, compoundTag));
            }
        }
        tag.put("items", itemsTag);

        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.@NotNull Provider provider, @NotNull CompoundTag tag) {
        ListTag itemsTag = tag.getList("items", ListTag.TAG_COMPOUND);
        for (int i = 0; i < itemsTag.size(); i++) {
            CompoundTag compoundTag = itemsTag.getCompound(i);
            int j = compoundTag.getByte("Slot");
            items.set(j, ItemStack.parse(provider, compoundTag).orElseGet(() -> {
                KeepSomeModdedInventory.LOGGER.warn("Invalid item in death inventory.");
                return ItemStack.EMPTY;
            }));
        }
    }
}
