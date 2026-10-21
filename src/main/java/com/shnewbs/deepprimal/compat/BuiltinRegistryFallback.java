package com.shnewbs.deepprimal.compat;

import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;

/** Keeps dynamic registry snapshots usable for script data generation after a failed world load. */
public final class BuiltinRegistryFallback {
  public static RegistryAccess.Frozen complete(RegistryAccess.Frozen source) {
    if (source.lookup(Registries.ITEM).isPresent()) return source;
    var builtin = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
    return new RegistryAccess.Frozen() {
      @Override
      public <E> Optional<Registry<E>> lookup(ResourceKey<? extends Registry<? extends E>> key) {
        return source.lookup(key).or(() -> builtin.lookup(key));
      }

      @Override
      public RegistryAccess.Frozen freeze() {
        return this;
      }

      @Override
      public Stream<RegistryAccess.RegistryEntry<?>> registries() {
        return Stream.concat(
            source.registries(),
            builtin.registries().filter(entry -> source.lookup(entry.key()).isEmpty()));
      }
    };
  }

  private BuiltinRegistryFallback() {}
}
