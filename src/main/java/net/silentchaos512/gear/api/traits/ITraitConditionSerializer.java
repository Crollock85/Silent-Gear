package net.silentchaos512.gear.api.traits;

import com.google.gson.JsonObject;
import net.minecraft.util.ResourceLocation;

/**
 * TODO: Update Javadoc!
 *
 * Conditions for traits applied to parts. These affect when a trait given to a part will actually
 * apply to a gear item, such as required a specific gear type or a minimum number of parts. Heavily
 * inspired by Forge's recipe conditions (what was IConditionSerializer).
 */
public interface ITraitConditionSerializer<T extends ITraitCondition> {
    ResourceLocation getId();

    T deserialize(JsonObject json);

    void serialize(T value, JsonObject json);

    default JsonObject serialize(T value) {
        JsonObject json = new JsonObject();
        json.addProperty("type", value.getId().toString());
        this.serialize(value, json);
        return json;
    }
}
