package dev.mageowl.keepsomemoddedinventory;


import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.Collection;

public class DeathEvents {
    @SubscribeEvent
    public static void onEntityDeath(LivingDeathEvent event) {
        if (!Config.modEnabled) return;

        if (event.getEntity() instanceof ServerPlayer player) {
            player.setData(KeepSomeModdedInventory.DEATH_INVENTORY, new DeathInventory(player));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onEntityDropItems(LivingDropsEvent event) {
        if (!Config.modEnabled) return;

        if (event.getEntity() instanceof ServerPlayer player) {
            DeathInventory inventory = player.getData(KeepSomeModdedInventory.DEATH_INVENTORY);

            Collection<ItemEntity> drops = event.getDrops();
            drops.removeIf((drop) -> {
                ItemStack item = drop.getItem();

                if (KeepSomeModdedInventory.shouldDrop(item, event.getEntity().level())) {
                    inventory.remove(item);
                    drop.setDeltaMovement(
                            drop.getDeltaMovement().multiply(
                                    Config.itemVelocityMultiplier,
                                    1,
                                    Config.itemVelocityMultiplier
                            )
                    );
                    drop.setUnlimitedLifetime();
                    return false;
                } else return true;
            });
        }
    }

    @SubscribeEvent
    public static void onEntityDropExperience(LivingExperienceDropEvent event) {
        if (!Config.modEnabled) return;
        if (Config.experienceDropped == 0) return;

        if (event.getEntity() instanceof ServerPlayer player) {
            int xp = (int) (player.getXpNeededForNextLevel() * player.experienceProgress);

            // From https://minecraft.wiki/Experience#Leveling_up
            int lvlSquared = player.experienceLevel * player.experienceLevel;
            if (player.experienceLevel < 17) {
                xp += lvlSquared + 6 * player.experienceLevel;
            } else if (player.experienceLevel < 32) {
                xp += (int) (2.5 * lvlSquared - 40.5 * player.experienceLevel + 360);
            } else {
                xp += (int) (4.5 * lvlSquared - 162.5 * player.experienceLevel + 2220);
            }

            xp = (int) (xp * Config.experienceDropped);
            KeepSomeModdedInventory.LOGGER.info("Dropping {} experience", xp);

            event.setDroppedExperience(xp);
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!Config.modEnabled) return;

        if (event.getEntity() instanceof ServerPlayer player) {
            if (Config.experienceDropped != 0) {
                player.setExperienceLevels(0);
                player.setExperiencePoints(0);
            }

            DeathInventory inventory = player.removeData(KeepSomeModdedInventory.DEATH_INVENTORY);
            if (inventory == null) return;

            inventory.restore(player);
        }
    }
}
