package net.silentchaos512.gear.util;

import net.minecraft.util.ResourceLocation;
import net.silentchaos512.gear.SilentGear;
import net.silentchaos512.gear.api.material.IMaterial;
import net.silentchaos512.gear.api.parts.IGearPart;
import net.silentchaos512.gear.api.traits.ITrait;
import net.silentchaos512.gear.gear.material.MaterialManager;
import net.silentchaos512.gear.parts.PartManager;
import net.silentchaos512.gear.traits.TraitManager;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

public final class DataResource<T> {
    private final ResourceLocation objectId;
    private final Function<ResourceLocation, T> getter;

    private DataResource(ResourceLocation id, Function<ResourceLocation, T> getter) {
        this.objectId = id;
        this.getter = getter;
    }

    public static DataResource<IMaterial> material(String modPath) {
        return material(SilentGear.getId(modPath));
    }

    public static DataResource<IMaterial> material(ResourceLocation id) {
        return new DataResource<>(id, MaterialManager::get);
    }

    public static DataResource<IGearPart> part(String modPath) {
        return part(SilentGear.getId(modPath));
    }

    public static DataResource<IGearPart> part(ResourceLocation id) {
        return new DataResource<>(id, PartManager::get);
    }

    public static DataResource<ITrait> trait(String modPath) {
        return trait(SilentGear.getId(modPath));
    }

    private static DataResource<ITrait> trait(ResourceLocation id) {
        return new DataResource<>(id, TraitManager::get);
    }

    @Nullable
    private T getNullable() {
        return this.getter.apply(this.objectId);
    }

    public T get() {
        T ret = getNullable();
        Objects.requireNonNull(ret, () -> "Data resource not present: " + this.objectId);
        return ret;
    }

    public ResourceLocation getId() {
        return this.objectId;
    }

    public boolean isPresent() {
        return this.getNullable() != null;
    }

    public void ifPresent(Consumer<? super T> consumer) {
        T obj = getNullable();
        if (obj != null) {
            consumer.accept(obj);
        }
    }

    public Stream<T> stream() {
        return isPresent() ? Stream.of(get()) : Stream.of();
    }

    public <U> Optional<U> map(Function<? super T, ? extends U> mapper) {
        T obj = getNullable();
        return obj != null ? Optional.ofNullable(mapper.apply(obj)) : Optional.empty();
    }
}
