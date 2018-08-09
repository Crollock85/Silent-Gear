package net.silentchaos512.gear;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.silentchaos512.gear.api.parts.ItemPart;
import net.silentchaos512.gear.api.parts.PartRegistry;
import net.silentchaos512.gear.api.stats.CommonItemStats;
import net.silentchaos512.gear.client.gui.GuiHandlerSilentGear;
import net.silentchaos512.gear.config.Config;
import net.silentchaos512.gear.event.*;
import net.silentchaos512.gear.init.ModBlocks;
import net.silentchaos512.gear.init.ModItems;
import net.silentchaos512.gear.init.ModMaterials;
import net.silentchaos512.gear.init.ModRecipes;
import net.silentchaos512.gear.util.GearData;
import net.silentchaos512.gear.util.GearHelper;
import net.silentchaos512.gear.world.ModWorldGenerator;
import net.silentchaos512.lib.registry.SRegistry;

public class CommonProxy {

    public void preInit(SRegistry registry, FMLPreInitializationEvent event) {
        CommonItemStats.init();

        // Registration Handlers
        registry.addRegistrationHandler(ModBlocks::registerAll, Block.class);
        registry.addRegistrationHandler(ModItems::registerAll, Item.class);
        registry.addRegistrationHandler(ModRecipes::registerAll, IRecipe.class);

        // Phased Initializers
        registry.addPhasedInitializer(Config.INSTANCE);
        registry.addPhasedInitializer(ModMaterials.INSTANCE);
        registry.addPhasedInitializer(VanillaGearHandler.INSTANCE);

        Config.INSTANCE.onPreInit(event);

        NetworkRegistry.INSTANCE.registerGuiHandler(SilentGear.instance, new GuiHandlerSilentGear());

        GameRegistry.registerWorldGenerator(new ModWorldGenerator(), 0);

        // Event Handlers
        MinecraftForge.EVENT_BUS.register(DropsHandler.INSTANCE);
        MinecraftForge.EVENT_BUS.register(GearData.EventHandler.INSTANCE);
        MinecraftForge.EVENT_BUS.register(GearHelper.EventHandler.INSTANCE);
        MinecraftForge.EVENT_BUS.register(RepairHandler.INSTANCE);
        MinecraftForge.EVENT_BUS.register(ToolBlockPlaceHandler.INSTANCE);
        MinecraftForge.EVENT_BUS.register(WorldHandler.INSTANCE);

        registry.preInit(event);
    }

    public void init(SRegistry registry, FMLInitializationEvent event) {
        registry.init(event);
    }

    public void postInit(SRegistry registry, FMLPostInitializationEvent event) {
        registry.postInit(event);

        // Log issues with registered parts
        PartRegistry.getValues().forEach(ItemPart::postInitChecks);
    }
}
