package net.silentchaos512.gear.item.gear;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.*;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.silentchaos512.gear.SilentGear;
import net.silentchaos512.gear.api.item.ICoreArmor;
import net.silentchaos512.gear.api.parts.IPartDisplay;
import net.silentchaos512.gear.api.parts.PartType;
import net.silentchaos512.gear.api.stats.CommonItemStats;
import net.silentchaos512.gear.client.util.GearClientHelper;
import net.silentchaos512.gear.config.Config;
import net.silentchaos512.gear.parts.PartData;
import net.silentchaos512.gear.parts.PartManager;
import net.silentchaos512.gear.util.GearData;
import net.silentchaos512.gear.util.GearHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class CoreArmor extends ItemArmor implements ICoreArmor {
    // Just copied from ItemArmor, access transformers are too flaky
    private static final UUID[] ARMOR_MODIFIERS = {UUID.fromString("845DB27C-C624-495F-8C9F-6020A9A58B6B"), UUID.fromString("D8499B04-0E66-4726-AB29-64469D734E0D"), UUID.fromString("9F3D476D-C118-4544-8365-64846904B48E"), UUID.fromString("2AD3F246-FEE1-4E67-B886-69FD380BB150")};
    // sum = 1, starts with boots
    private static final float[] ABSORPTION_RATIO_BY_SLOT = {0.175f, 0.3f, 0.4f, 0.125f};
    // Same values as in ItemArmor.
    private static final int[] MAX_DAMAGE_ARRAY = {13, 15, 16, 11};

    private final String itemName;

    public CoreArmor(EntityEquipmentSlot slot, String name) {
        super(ArmorMaterial.DIAMOND, slot, GearHelper.getBuilder(null));
        this.itemName = name;
    }

    @Override
    public String getGearClass() {
        return itemName;
    }

    //region Stats and attributes

    public double getArmorProtection(ItemStack stack) {
        if (GearHelper.isBroken(stack)) return 0;
        return ABSORPTION_RATIO_BY_SLOT[armorType.getIndex()] * GearData.getStat(stack, CommonItemStats.ARMOR);
    }

    public double getArmorToughness(ItemStack stack) {
        if (GearHelper.isBroken(stack)) return 0;
        return GearData.getStat(stack, CommonItemStats.ARMOR_TOUGHNESS) / 4;
    }

    private static double getGenericArmorProtection(ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof CoreArmor)
            return ((CoreArmor) item).getArmorProtection(stack);
        else if (item instanceof ItemArmor)
            return ((ItemArmor) item).getDamageReduceAmount();
        return 0;
    }

    private static int getPlayerTotalArmorValue(EntityLivingBase player) {
        float total = 0;
        for (ItemStack armor : player.getArmorInventoryList()) {
            total += getGenericArmorProtection(armor);
        }
        return Math.round(total);
    }

    @Nonnull
    @Override
    public Multimap<String, AttributeModifier> getAttributeModifiers(EntityEquipmentSlot slot, ItemStack stack) {
        Multimap<String, AttributeModifier> multimap = HashMultimap.create();
        if (slot == this.armorType) {
            UUID uuid = ARMOR_MODIFIERS[slot.getIndex()];
            multimap.put(SharedMonsterAttributes.ARMOR.getName(), new AttributeModifier(
                    uuid, "Armor modifier", getArmorProtection(stack), 0));
            multimap.put(SharedMonsterAttributes.ARMOR_TOUGHNESS.getName(), new AttributeModifier(
                    uuid, "Armor toughness", getArmorToughness(stack), 0));
        }
        return multimap;
    }

    //endregion

    //region Item overrides

    @Override
    public int getMaxDamage(ItemStack stack) {
        int maxDamageFactor = GearData.getStatInt(stack, CommonItemStats.ARMOR_DURABILITY);
        return MAX_DAMAGE_ARRAY[armorType.getIndex()] * maxDamageFactor;
    }

    @Override
    public void setDamage(ItemStack stack, int damage) {
        if (GearHelper.isUnbreakable(stack)) return;
        if (!Config.GENERAL.gearBreaksPermanently.get())
            damage = MathHelper.clamp(damage, 0, getMaxDamage(stack));
        super.setDamage(stack, damage);
    }

    @Override
    public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
        return GearHelper.getIsRepairable(toRepair, repair);
    }

    @Override
    public int getItemEnchantability(ItemStack stack) {
        return GearData.getStatInt(stack, CommonItemStats.ENCHANTABILITY);
    }

    @Override
    public void inventoryTick(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
        GearHelper.inventoryTick(stack, worldIn, entityIn, itemSlot, isSelected);
    }

    public Collection<IRecipe> getExampleRecipes() {
        Collection<IRecipe> list = new ArrayList<>();

        /*
        Ingredient blueprint = Blueprint.getBlueprintIngredientForGear(this);
        if (blueprint != null) {
            for (PartMain part : PartRegistry.getVisibleMains()) {
                ItemStack result = construct(this, part.getCraftingStack());
                NonNullList<Ingredient> ingredients = NonNullList.create();
                ingredients.add(blueprint);
                for (int i = 0; i < getConfig().getHeadCount(); ++i) {
                    ingredients.add(Ingredient.fromStacks(part.getCraftingStack()));
                }
                list.add(new ShapelessRecipes(SilentGear.MOD_ID, result, ingredients));
            }
        } else {
            SilentGear.log.warn("Trying to add {} example recipes, but could not find blueprint item!", itemName);
        }
        */

        return list;
    }

    @Override
    public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items) {
        GearHelper.fillItemGroup(this, group, items);
    }

    //endregion

    //region Client-side methods and rendering horrors

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type) {
        // In order for colors to work, it seems the following must be true:
        // 1. Armor texture must be named using the vanilla convention
        // 2. Return value of this may NOT be cached... wat? Not a big deal I guess.
        // 3. You got lucky. The tiniest change can break everything for no apparent reason.

        int layer = slot == EntityEquipmentSlot.LEGS ? 2 : 1;
        // Overlay - default to a blank texture
        if ("overlay".equals(type))
            return SilentGear.MOD_ID + ":textures/models/armor/all_layer_" + layer + "_overlay.png";

        PartData part = GearData.getPrimaryRenderPartFast(stack);
        if (part == null) part = PartData.ofNullable(PartManager.tryGetFallback(PartType.MAIN));

        // Actual armor texture
        IPartDisplay props = part.getPart().getDisplayProperties(part, stack, 0);
        return props.getTextureDomain() + ":textures/models/armor/"
                + props.getTextureSuffix()
                + "_layer_" + layer
                + (type != null ? "_" + type : "")
                + ".png";
    }

    // FIXME
    /*
    @Override
    public boolean hasOverlay(ItemStack stack) {
        return true;
    }

    @Override
    public boolean hasColor(ItemStack stack) {
        return true;
    }

    @Override
    public int getColor(ItemStack stack) {
        PartData renderPart = GearData.getPrimaryRenderPartFast(stack);
        return renderPart != null ? renderPart.getColor(stack, 0) : 0xFF00FF;
    }

    @Override
    public void removeColor(ItemStack stack) {}

    @Override
    public void setColor(ItemStack stack, int color) {}
    */

    @Override
    public ITextComponent getDisplayName(ItemStack stack) {
        return GearHelper.getDisplayName(stack);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        GearClientHelper.addInformation(stack, worldIn, tooltip, flagIn);
    }

    //endregion
}
