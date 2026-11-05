package tfcmt.tfcitembarrelsandmore.common.blockentities.mast;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nullable;
import tfcmt.tfcitembarrelsandmore.common.blockentities.mast.RigLinkable;

public final class RigLinkRegistry {
    private static final Map<UUID, WeakReference<RigLinkable>> REGISTRY = new ConcurrentHashMap<UUID, WeakReference<RigLinkable>>();
    private static final Map<UUID, WeakReference<RigLinkable>> CLIENT_REGISTRY = new ConcurrentHashMap<UUID, WeakReference<RigLinkable>>();

    private RigLinkRegistry() {
    }

    public static void register(UUID uuid, RigLinkable linkable) {
        REGISTRY.put(uuid, new WeakReference<RigLinkable>(linkable));
    }

    @Nullable
    public static RigLinkable find(UUID uuid) {
        return RigLinkRegistry.find(REGISTRY, uuid);
    }

    public static Collection<RigLinkable> all() {
        return RigLinkRegistry.all(REGISTRY);
    }

    public static void registerClient(UUID uuid, RigLinkable linkable) {
        CLIENT_REGISTRY.put(uuid, new WeakReference<RigLinkable>(linkable));
    }

    @Nullable
    public static RigLinkable findClient(UUID uuid) {
        return RigLinkRegistry.find(CLIENT_REGISTRY, uuid);
    }

    public static Collection<RigLinkable> allClient() {
        return RigLinkRegistry.all(CLIENT_REGISTRY);
    }

    @Nullable
    private static RigLinkable find(Map<UUID, WeakReference<RigLinkable>> registry, UUID uuid) {
        WeakReference<RigLinkable> ref = registry.get(uuid);
        if (ref == null) {
            return null;
        }
        RigLinkable linkable = (RigLinkable)ref.get();
        if (linkable == null) {
            registry.remove(uuid);
        }
        return linkable;
    }

    private static Collection<RigLinkable> all(Map<UUID, WeakReference<RigLinkable>> registry) {
        ArrayList<RigLinkable> result = new ArrayList<RigLinkable>(registry.size());
        Iterator<Map.Entry<UUID, WeakReference<RigLinkable>>> it = registry.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, WeakReference<RigLinkable>> entry = it.next();
            RigLinkable linkable = (RigLinkable)entry.getValue().get();
            if (linkable == null) {
                it.remove();
                continue;
            }
            result.add(linkable);
        }
        return result;
    }
}

