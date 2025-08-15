package dev.mageowl.keepsomemoddedinventory;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class DeathInventory {
    private final NonNullList<ItemStack> items;

    public DeathInventory(Player player) {
        Inventory inventory = player.getInventory();
        this.items = NonNullList.createWithCapacity(41);
        items.addAll(inventory.items);
        items.addAll(inventory.armor);
        items.addAll(inventory.offhand);
    }
    private DeathInventory(NonNullList<ItemStack> items) {
        this.items = items;
    }

    public static DeathInventory load(ListTag tag, HolderLookup.@NotNull Provider provider) {
        NonNullList<ItemStack> items = NonNullList.withSize(41, ItemStack.EMPTY);

        for (int i = 0; i < tag.size(); i++) {
            CompoundTag compoundTag = tag.getCompound(i);
            int j = compoundTag.getByte("Slot");
            items.set(j, ItemStack.parse(provider, compoundTag).orElseGet(() -> {
                KeepSomeModdedInventory.LOGGER.warn("Invalid item in death inventory.");
                return ItemStack.EMPTY;
            }));
        }

        return new DeathInventory(items);
    }
    public ListTag save(ListTag tag, HolderLookup.@NotNull Provider provider) {
        for (int i = 0; i < items.size(); i++) {
            ItemStack item = items.get(i);
            if (!item.isEmpty()) {
                CompoundTag compoundTag = new CompoundTag();
                compoundTag.putInt("Slot", i);
                tag.add(item.save(provider, compoundTag));
            }
        }
        return tag;
    }

    public void remove(ItemStack item) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i) == item) {
                items.set(i, ItemStack.EMPTY);
                return;
            }
        }
    }

    public void restore(Player player) {
        Inventory inventory = player.getInventory();

        for (int i = 0; i < items.size(); i++) {
            if (inventory.getItem(i).isEmpty())
                inventory.setItem(i, items.get(i));
            else {
                int slot = inventory.getFreeSlot();
                if (!items.get(slot).isEmpty() || slot == -1) {
                    player.drop(items.get(i), true, false);
                } else inventory.setItem(slot, items.get(i));
            }
        }
    }
}
