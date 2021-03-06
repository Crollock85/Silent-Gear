package net.silentchaos512.gear.traits.conditions;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.item.ItemStack;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.silentchaos512.gear.SilentGear;
import net.silentchaos512.gear.api.parts.PartDataList;
import net.silentchaos512.gear.api.parts.PartType;
import net.silentchaos512.gear.api.traits.ITrait;
import net.silentchaos512.gear.api.traits.ITraitCondition;
import net.silentchaos512.gear.api.traits.ITraitConditionSerializer;
import net.silentchaos512.gear.gear.material.MaterialInstance;
import net.silentchaos512.gear.traits.TraitSerializers;

import java.util.ArrayList;
import java.util.List;

public class AndTraitCondition implements ITraitCondition {
    public static final Serializer SERIALIZER = new Serializer();
    private static final ResourceLocation NAME = SilentGear.getId("and");

    private final ITraitCondition[] children;

    public AndTraitCondition(ITraitCondition... values) {
        //noinspection ConstantConditions
        if (values == null || values.length == 0) {
            throw new IllegalArgumentException("Values must not be empty");
        }

        for (ITraitCondition child : values) {
            if (child == null) {
                throw new IllegalArgumentException("Value must not be null");
            }
        }

        this.children = values.clone();
    }

    @Override
    public ResourceLocation getId() {
        return NAME;
    }

    @Override
    public boolean matches(ItemStack gear, PartDataList parts, ITrait trait) {
        for (ITraitCondition child : this.children) {
            if (!child.matches(gear, parts, trait)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean matches(ItemStack gear, PartType partType, List<MaterialInstance> materials, ITrait trait) {
        for (ITraitCondition child : this.children) {
            if (!child.matches(gear, partType, materials, trait)) {
                return false;
            }
        }
        return true;
    }

    public static class Serializer implements ITraitConditionSerializer<AndTraitCondition> {

        @Override
        public ResourceLocation getId() {
            return AndTraitCondition.NAME;
        }

        @Override
        public AndTraitCondition deserialize(JsonObject json) {
            List<ITraitCondition> children = new ArrayList<>();
            for (JsonElement j : JSONUtils.getJsonArray(json, "values")) {
                if (!j.isJsonObject()) {
                    throw new JsonSyntaxException("And condition values must be array of objects");
                }
                children.add(TraitSerializers.deserializeCondition(j.getAsJsonObject()));
            }
            return new AndTraitCondition(children.toArray(new ITraitCondition[0]));
        }

        @Override
        public void serialize(AndTraitCondition value, JsonObject json) {
            JsonArray values = new JsonArray();
            for (ITraitCondition c : value.children) {
                values.add(TraitSerializers.serializeCondition(c));
            }
            json.add("values", values);
        }
    }
}
