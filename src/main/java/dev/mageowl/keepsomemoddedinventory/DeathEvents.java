package dev.mageowl.keepsomemoddedinventory;


import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

import java.util.Collection;
import java.util.Objects;

public class DeathEvents {
    private DeathSavedData savedData;

    @SubscribeEvent
    public void onEntityDeath(LivingDeathEvent event) {
        if (!Config.modEnabled) return;

        if (event.getEntity() instanceof ServerPlayer player) {
            savedData.put(player.getUUID(), new DeathInventory(player));
        }
    }

    @SubscribeEvent
    public void onEntityDropItems(LivingDropsEvent event) {
        if (!Config.modEnabled) return;

        if (event.getEntity() instanceof ServerPlayer player) {
            DeathInventory inventory = savedData.get(player.getUUID());
            if (inventory == null) return;

            Collection<ItemEntity> drops = event.getDrops();
            drops.removeIf((drop) -> {
                ItemStack item = drop.getItem();

                if (shouldDrop(item)) {
                    inventory.remove(item);
                    drop.setDeltaMovement(
                            drop.getDeltaMovement()
                                    .multiply(
                                            Config.itemVelocityMultiplier,
                                            1,
                                            Config.itemVelocityMultiplier)
                    );
                    drop.setUnlimitedLifetime();
                    return false;
                } else return true;
            });
        }
    }

    @SubscribeEvent
    public void onEntityDropExperience(LivingExperienceDropEvent event) {
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
    public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!Config.modEnabled) return;

        if (event.getEntity() instanceof ServerPlayer player) {
            if (Config.experienceDropped != 0) {
                player.setExperienceLevels(0);
                player.setExperiencePoints(0);
            }

            DeathInventory inventory = savedData.remove(player.getUUID());
            if (inventory == null) return;

            inventory.restore(player);
        }
    }

    private boolean shouldDrop(ItemStack item) {

        if (item.is(KeepSomeModdedInventory.TAG_ALWAYS_DROP)) return true;
        if (item.is(KeepSomeModdedInventory.TAG_NEVER_DROP)) return false;

        if (item.isEnchantable()) return false;
        if (item.has(DataComponents.MAX_DAMAGE)) return false;

        return true;
    }

    @SubscribeEvent
    public void onJoinWorld(ServerStartingEvent event) {
        ServerLevel level = Objects.requireNonNull(event.getServer()
                .getLevel(Level.OVERWORLD));
        DimensionDataStorage dataStorage = level.getDataStorage();
        savedData = dataStorage.computeIfAbsent(new SavedData.Factory<>(DeathSavedData::create, DeathSavedData::load), KeepSomeModdedInventory.MODID);
    }
}
