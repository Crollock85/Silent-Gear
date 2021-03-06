package net.silentchaos512.gear.config;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.silentchaos512.gear.SilentGear;
import net.silentchaos512.gear.api.stats.ItemStat;
import net.silentchaos512.gear.api.stats.ItemStats;
import net.silentchaos512.gear.init.NerfedGear;
import net.silentchaos512.gear.item.blueprint.BlueprintType;
import net.silentchaos512.gear.util.IAOETool;

import java.util.*;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid = SilentGear.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class Config {
    public static final class Common {
        static final ForgeConfigSpec spec;
        // Blueprints
        public static final ForgeConfigSpec.EnumValue<BlueprintType> blueprintTypes;
        public static final ForgeConfigSpec.BooleanValue spawnWithStarterBlueprints;
        // Nerfed gear
        public static final ForgeConfigSpec.BooleanValue nerfedItemsEnabled;
        public static final ForgeConfigSpec.DoubleValue nerfedItemDurabilityMulti;
        public static final ForgeConfigSpec.DoubleValue nerfedItemHarvestSpeedMulti;
        static final ForgeConfigSpec.ConfigValue<List<? extends String>> nerfedItems;
        // Sinew
        public static final ForgeConfigSpec.DoubleValue sinewDropRate;
        static final ForgeConfigSpec.ConfigValue<List<? extends String>> sinewAnimals;
        // Gear
        public static final ForgeConfigSpec.EnumValue<IAOETool.MatchMode> matchModeStandard;
        public static final ForgeConfigSpec.EnumValue<IAOETool.MatchMode> matchModeOres;
        public static final ForgeConfigSpec.BooleanValue gearBreaksPermanently;
        public static final ForgeConfigSpec.IntValue prospectorHammerRange;
        public static final ForgeConfigSpec.DoubleValue repairFactorAnvil;
        public static final ForgeConfigSpec.DoubleValue repairFactorQuick;
        public static final ForgeConfigSpec.IntValue repairKitCrudeCapacity;
        public static final ForgeConfigSpec.IntValue repairKitSturdyCapacity;
        public static final ForgeConfigSpec.IntValue repairKitCrimsonCapacity;
        public static final ForgeConfigSpec.IntValue repairKitAzureCapacity;
        public static final ForgeConfigSpec.DoubleValue repairKitCrudeEfficiency;
        public static final ForgeConfigSpec.DoubleValue repairKitSturdyEfficiency;
        public static final ForgeConfigSpec.DoubleValue repairKitCrimsonEfficiency;
        public static final ForgeConfigSpec.DoubleValue repairKitAzureEfficiency;
        public static final ForgeConfigSpec.BooleanValue upgradesInAnvilOnly;
        private static final Map<ItemStat, ForgeConfigSpec.DoubleValue> statMultipliers = new HashMap<>();
        // Salvager
        public static final ForgeConfigSpec.DoubleValue salvagerMinLossRate;
        public static final ForgeConfigSpec.DoubleValue salvagerMaxLossRate;
        // Compatibility
        public static final ForgeConfigSpec.BooleanValue mineAndSlashSupport;
        // Debug
        public static final ForgeConfigSpec.BooleanValue extraPartAndTraitLogging;

        static {
            ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

            {
                builder.push("item");

                {
                    builder.comment("Blueprint and template settings");
                    builder.push("blueprint");
                    blueprintTypes = builder
                            .comment("Allowed blueprint types. Valid values are: BOTH, BLUEPRINT, and TEMPLATE")
                            .defineEnum("typesAllowed", BlueprintType.BOTH);
                    spawnWithStarterBlueprints = builder
                            .comment("When joining a new world, should players be given a blueprint package?",
                                    "The blueprint package gives some blueprints when used (right-click).",
                                    "To change what is given, override the starter_blueprints loot table.")
                            .define("spawnWithStarterBlueprints", true);
                    builder.pop();
                }
                {
                    builder.comment("Repair kit configs.");
                    builder.push("repairKits");

                    {
                        builder.comment("Capacity is the number of materials that can be stored (all types combined)",
                                "Setting to zero would make the repair kit unusable.");
                        builder.push("capacity");
                        repairKitCrudeCapacity = builder.defineInRange("crude", 16, 0, Integer.MAX_VALUE);
                        repairKitSturdyCapacity = builder.defineInRange("sturdy", 32, 0, Integer.MAX_VALUE);
                        repairKitCrimsonCapacity = builder.defineInRange("crimson", 48, 0, Integer.MAX_VALUE);
                        repairKitAzureCapacity = builder.defineInRange("azure", 64, 0, Integer.MAX_VALUE);
                        builder.pop();
                    }
                    {
                        builder.comment("Efficiency is the percentage of the repair value used. Higher values mean less materials used.",
                                "Setting to zero would make the repair kit unusable.");
                        builder.push("efficiency");
                        repairKitCrudeEfficiency = builder.defineInRange("crude", 0.35f, 0f, 10f);
                        repairKitSturdyEfficiency = builder.defineInRange("sturdy", 0.4f, 0f, 10f);
                        repairKitCrimsonEfficiency = builder.defineInRange("crimson", 0.45f, 0f, 10f);
                        repairKitAzureEfficiency = builder.defineInRange("azure", 0.5f, 0f, 10f);
                        builder.pop();
                    }

                    builder.pop();
                }

                builder.pop();
            }
            {
                builder.comment("Settings for nerfed items.",
                        "You can give items reduced durability to encourage use of Silent Gear tools.",
                        "Changes require a restart!");
                builder.push("nerfedItems");
                nerfedItemsEnabled = builder
                        .comment("Enable this feature. If false, the other settings in this category are ignored.")
                        .define("enabled", false);
                nerfedItemDurabilityMulti = builder
                        .comment("Multiplies max durability by this value. If the result would be zero, a value of 1 is assigned.")
                        .defineInRange("durabilityMultiplier", 0.05, 0, 1);
                nerfedItemHarvestSpeedMulti = builder
                        .comment("Multiplies harvest speed by this value.")
                        .defineInRange("harvestSpeedMultiplier", 0.5, 0, 1);
                nerfedItems = builder
                        .comment("These items will have reduced durability")
                        .defineList("items", NerfedGear.DEFAULT_ITEMS, Config::isResourceLocation);
                builder.pop();
            }
            {
                builder.comment("Settings for sinew drops");
                builder.push("sinew");

                sinewDropRate = builder
                        .comment("Drop rate of sinew (chance out of 1)")
                        .defineInRange("dropRate", 0.2, 0, 1);
                sinewAnimals = builder
                        .comment("These entities can drop sinew when killed.")
                        .defineList("dropsFrom",
                                ImmutableList.of(
                                        "minecraft:cow",
                                        "minecraft:pig",
                                        "minecraft:sheep"
                                ),
                                Config::isResourceLocation);
                builder.pop();
            }
            {
                builder.comment("Settings for gear (tools, weapons, and armor)");
                builder.push("gear");

                gearBreaksPermanently = builder
                        .comment("If true, gear breaks permanently, like vanilla tools and armor")
                        .define("breaksPermanently", false);

                {
                    builder.push("prospector_hammer");
                    prospectorHammerRange = builder
                            .comment("The range in blocks the prospector hammer will search for blocks of interest")
                            .defineInRange("range", 16, 0, 64);
                    builder.pop();
                }
                {
                    builder.comment("Settings for AOE tools (hammer, excavator)",
                            "Match modes determine what blocks are considered similar enough to be mined together.",
                            "LOOSE: Break anything (you probably do not want this)",
                            "MODERATE: Break anything with the same harvest level",
                            "STRICT: Break only the exact same block");
                    builder.push("aoeTool");
                    matchModeStandard = builder
                            .comment("Match mode for most blocks")
                            .defineEnum("matchMode.standard", IAOETool.MatchMode.MODERATE);
                    matchModeOres = builder
                            .comment("Match mode for ore blocks (anything in the forge:ores block tag)")
                            .defineEnum("matchMode.ores", IAOETool.MatchMode.STRICT);
                    builder.pop();
                }
                {
                    builder.push("repairs");
                    repairFactorAnvil = builder
                            .comment("Effectiveness of gear repairs done in an anvil. Set to 0 to disable anvil repairs.")
                            .defineInRange("anvilEffectiveness", 0.5, 0, 1);
                    repairFactorQuick = builder
                            .comment("DEPRECATED! Effectiveness of quick gear repairs (crafting grid). Set to 0 to disable quick repairs.")
                            .defineInRange("quickEffectiveness", 0.35, 0, 1);

                    builder.pop();
                }
                {
                    builder.push("upgrades");
                    upgradesInAnvilOnly = builder
                            .comment("If true, upgrade parts may only be applied in an anvil.")
                            .define("applyInAnvilOnly", false);
                    builder.pop();
                }
                {
                    builder.comment("Multipliers for stats on all gear. This allows the stats on all items to be increased or decreased",
                            "without overriding every single file.");
                    builder.push("statMultipliers");

                    // FIXME: Does not work, called too early
                    ItemStats.REGISTRY.get().getValues().forEach(stat -> {
                        ResourceLocation name = Objects.requireNonNull(stat.getRegistryName());
                        String key = name.getNamespace() + "." + name.getPath();
                        ForgeConfigSpec.DoubleValue config = builder
                                .defineInRange(key, 1, 0, Double.MAX_VALUE);
                        statMultipliers.put(stat, config);
                    });
                    builder.pop();
                }
                builder.pop();
            }

            {
                builder.comment("Settings for the salvager");
                builder.push("salvager");
                salvagerMinLossRate = builder
                        .comment("Minimum rate of part loss when salvaging items. 0 = no loss, 1 = complete loss.",
                                "Rate depends on remaining durability.")
                        .defineInRange("partLossRate.min", 0.0, 0, 1);
                salvagerMaxLossRate = builder
                        .comment("Maximum rate of part loss when salvaging items. 0 = no loss, 1 = complete loss.",
                                "Rate depends on remaining durability.")
                        .defineInRange("partLossRate.max", 0.5, 0, 1);
                builder.pop();
            }

            mineAndSlashSupport = builder
                    .comment("Enable compatibility with the Mine and Slash mod, if installed")
                    .define("compat.mineAndSlash.enabled", true);

            extraPartAndTraitLogging = builder
                    .comment("Log additional information related to loading and synchronizing gear parts and traits.",
                            "This might help track down more obscure issues.")
                    .define("debug.logging.extraPartAndTraitInfo", false);

            spec = builder.build();
        }

        private Common() {}

        public static float getStatWithMultiplier(ItemStat stat, float value) {
            if (statMultipliers.containsKey(stat))
                return statMultipliers.get(stat).get().floatValue() * value;
            return value;
        }

        @SuppressWarnings("TypeMayBeWeakened")
        public static boolean isNerfedItem(Item item) {
            return nerfedItemsEnabled.get() && isThingInList(item, nerfedItems);
        }

        public static boolean isSinewAnimal(LivingEntity entity) {
            return isThingInList(entity.getType(), sinewAnimals);
        }

        private static boolean isThingInList(IForgeRegistryEntry<?> thing, ForgeConfigSpec.ConfigValue<List<? extends String>> list) {
            ResourceLocation name = thing.getRegistryName();
            for (String str : list.get()) {
                ResourceLocation fromList = ResourceLocation.tryCreate(str);
                if (fromList != null && fromList.equals(name)) {
                    return true;
                }
            }
            return false;
        }
    }

    private static boolean isResourceLocation(Object o) {
        return o instanceof String && ResourceLocation.tryCreate((String) o) != null;
    }

    public static final class Client {
        static final ForgeConfigSpec spec;

        public static final ForgeConfigSpec.BooleanValue allowEnchantedEffect;
        public static final ForgeConfigSpec.BooleanValue useLiteModels;
        public static final ForgeConfigSpec.BooleanValue disableNewMaterialTooltips;

        static {
            ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

            allowEnchantedEffect = builder
                    .comment("Allow gear items to have the 'enchanted glow' effect. Set to 'false' to disable the effect.",
                            "The way vanilla handles the effect is bugged, and it is recommended to disable this until custom models are possible again.")
                    .define("gear.allowEnchantedEffect", false);
            useLiteModels = builder
                    .comment("Use 'lite' gear models. These should be easier on some systems, but do not allow unique textures for different materials.",
                            "Currently, this option has no effect, as the normal model system is not working yet (lite models are used)")
                    .define("gear.useLiteModels", false);
            disableNewMaterialTooltips = builder
                    .comment("Disable item tooltips related to the new material system. Will be removed when fully implemented.")
                    .define("item.gear.materials.disableNewTooltips", false);

            spec = builder.build();
        }

        private Client() {}
    }

    private Config() {}

    public static void init() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Common.spec);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Client.spec);
    }

    public static void sync() {
    }

    @SubscribeEvent
    public static void sync(ModConfig.Loading event) {
        sync();
    }

    @SubscribeEvent
    public static void sync(ModConfig.Reloading event) {
        sync();
    }

    // TODO: Old stuff below, needs to be fixed

    private static String[] getDefaultNerfedGear() {
        Set<String> toolTypes = ImmutableSet.of("pickaxe", "shovel", "axe", "sword");
        Set<String> toolMaterials = ImmutableSet.of("wooden", "stone", "iron", "golden", "diamond");
        List<String> items = toolTypes.stream()
                .flatMap(type -> toolMaterials.stream()
                        .map(material -> "minecraft:" + material + "_" + type))
                .collect(Collectors.toList());

        Set<String> armorTypes = ImmutableSet.of("helmet", "chestplate", "leggings", "boots");
        Set<String> armorMaterials = ImmutableSet.of("leather", "chainmail", "iron", "diamond", "golden");
        items.addAll(armorTypes.stream()
                .flatMap(type -> armorMaterials.stream()
                        .map(material -> "minecraft:" + material + "_" + type))
                .collect(Collectors.toList()));

        return items.toArray(new String[0]);
    }
}
