package dev.mageowl.keepsomemoddedinventory;

import com.mojang.logging.LogUtils;
import dev.mageowl.keepsomemoddedinventory.compat.AccessoriesCompat;
import io.wispforest.accessories.api.AccessoriesAPI;
import io.wispforest.accessories.api.events.OnDropCallback;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.slf4j.Logger;

import java.util.function.Supplier;


// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(KeepSomeModdedInventory.ID)
public class KeepSomeModdedInventory {
    // Define mod id in a common place for everything to reference
    public static final String ID = "keepsomemoddedinventory";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final TagKey<Item> TAG_ALWAYS_DROP = ItemTags.create(ResourceLocation.fromNamespaceAndPath(ID, "always_drop"));
    public static final TagKey<Item> TAG_NEVER_DROP = ItemTags.create(ResourceLocation.fromNamespaceAndPath(ID, "never_drop"));

    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, ID);

    public static final Supplier<AttachmentType<DeathInventory>> DEATH_INVENTORY = ATTACHMENT_TYPES.register(
            "death_inventory", () -> AttachmentType.serializable(DeathInventory::empty).copyOnDeath().build()
    );

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public KeepSomeModdedInventory(ModContainer modContainer, IEventBus eventBus) {
        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (KeepSomeModdedInventory) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(DeathEvents.class);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.SERVER, Config.SPEC);

        ATTACHMENT_TYPES.register(eventBus);

        if (ModList.get().isLoaded("accessories")) {
            OnDropCallback.EVENT.register(new AccessoriesCompat());
        }
    }

    public static boolean shouldDrop(ItemStack item, Level level) {
        if (item.is(KeepSomeModdedInventory.TAG_ALWAYS_DROP)) return true;
        if (item.is(KeepSomeModdedInventory.TAG_NEVER_DROP)) return false;

        if (Config.keepEnchantable && item.isEnchantable()) return false;
        if (Config.keepDamageable && item.has(DataComponents.MAX_DAMAGE)) return false;
        if (Config.keepEquippable && isEquippable(item, level)) return false;

        return true;
    }

    private static boolean isEquippable(ItemStack item, Level level) {
        EquipmentSlot slot = item.getEquipmentSlot();
        if (slot != null && slot.isArmor()) return true;
        if (ModList.get().isLoaded("accessories") && AccessoriesAPI.isValidAccessory(item, level)) return true;

        return false;
    }
}
