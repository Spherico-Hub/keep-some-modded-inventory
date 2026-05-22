package dev.mageowl.keepsomemoddedinventory;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = KeepSomeModdedInventory.ID, dist = Dist.CLIENT)
public class KeepSomeModdedInventoryClient {

    public KeepSomeModdedInventoryClient(ModContainer modContainer) {
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }
}
