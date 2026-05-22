package dev.mageowl.keepsomemoddedinventory.compat;

import dev.mageowl.keepsomemoddedinventory.Config;
import dev.mageowl.keepsomemoddedinventory.KeepSomeModdedInventory;
import io.wispforest.accessories.api.DropRule;
import io.wispforest.accessories.api.events.OnDropCallback;
import io.wispforest.accessories.api.slot.SlotReference;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class AccessoriesCompat implements OnDropCallback {
    @Override
    public @Nullable DropRule onDrop(DropRule dropRule, ItemStack stack, SlotReference reference, DamageSource damageSource) {
        if (Config.modEnabled && KeepSomeModdedInventory.shouldDrop(stack, reference.entity().level())) return dropRule;
        else return DropRule.KEEP;
    }
}
