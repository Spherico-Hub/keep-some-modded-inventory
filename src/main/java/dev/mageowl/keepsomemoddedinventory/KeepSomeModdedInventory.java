package dev.mageowl.keepsomemoddedinventory;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(KeepSomeModdedInventory.MODID)
public class KeepSomeModdedInventory {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "keepsomemoddedinventory";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final TagKey<Item> TAG_ALWAYS_DROP = ItemTags.create(ResourceLocation.fromNamespaceAndPath(MODID, "always_drop"));
    public static final TagKey<Item> TAG_NEVER_DROP = ItemTags.create(ResourceLocation.fromNamespaceAndPath(MODID, "never_drop"));

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public KeepSomeModdedInventory(ModContainer modContainer) {
        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (KeepSomeModdedInventory) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(new DeathEvents());

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.SERVER, Config.SPEC);
    }
}
