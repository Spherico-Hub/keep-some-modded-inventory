package dev.mageowl.keepsomemoddedinventory;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Neo's config APIs
@EventBusSubscriber(modid = KeepSomeModdedInventory.ID)
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.BooleanValue MOD_ENABLED = BUILDER
            .comment("Whether the mod is enabled.")
            .define("modEnabled", true);
    private static final ModConfigSpec.BooleanValue KEEP_ENCHANTABLE = BUILDER
            .comment("Whether enchantable items should be kept. Items in datapack tags override this.")
            .define("keepEnchantable", true);
    private static final ModConfigSpec.BooleanValue KEEP_DAMAGEABLE = BUILDER
            .comment("Whether items with a max_damage component should be kept. Items in datapack tags override this.")
            .define("keepDamageable", true);
    private static final ModConfigSpec.BooleanValue KEEP_EQUIPPABLE = BUILDER
            .comment("Whether items that are able to be equipped should be dropped. This includes Accessories if installed.")
            .define("keepEquippable", true);
    private static final ModConfigSpec.DoubleValue ITEM_VELOCITY_MULTIPLIER = BUILDER
            .comment("How much items should be thrown when you die. 1 is the vanilla default.")
            .defineInRange("itemVelocityMultiplier", 0.1, 0, 100.0);
    private static final ModConfigSpec.DoubleValue EXPERIENCE_DROPPED = BUILDER
            .comment("Percent of experience should be dropped when you die. Set to 0 to use Minecraft's calculations.")
            .defineInRange("experienceDropped", 1.0, 0.0, 1.0);

    static final ModConfigSpec SPEC = BUILDER.build();

    public static boolean modEnabled;
    public static boolean keepEnchantable;
    public static boolean keepDamageable;
    public static boolean keepEquippable;
    public static double itemVelocityMultiplier;
    public static double experienceDropped;

    private static boolean validateItemName(final Object obj) {
        return obj instanceof String itemName && BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse(itemName));
    }

    @SubscribeEvent
    static void onLoad(ModConfigEvent event) {
        if (event instanceof ModConfigEvent.Unloading) return;
        modEnabled = MOD_ENABLED.get();
        keepEnchantable = KEEP_ENCHANTABLE.get();
        keepDamageable = KEEP_DAMAGEABLE.get();
        keepEquippable = KEEP_EQUIPPABLE.get();
        itemVelocityMultiplier = ITEM_VELOCITY_MULTIPLIER.get();
        experienceDropped = EXPERIENCE_DROPPED.get();
    }
}
