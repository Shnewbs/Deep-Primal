package tfcmt.tfcitembarrelsandmore.common.blockentities.mast;

import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import tfcmt.tfcitembarrelsandmore.common.blockentities.mast.RigLink;
import tfcmt.tfcitembarrelsandmore.common.blockentities.mast.RigLinkRegistry;
import tfcmt.tfcitembarrelsandmore.common.blockentities.mast.RigSlot;

public interface RigLinkable {
    public static final String TAG_RIG_UUID = "RigUUID";
    public static final String TAG_RIG_LINKS = "RigLinks";
    public static final String TAG_LOCAL_SLOT = "Slot";

    public UUID getRigUUID();

    public Set<RigLink> getLinks(RigSlot var1);

    public void addLink(RigSlot var1, RigLink var2);

    public void removeLink(RigSlot var1, RigLink var2);

    public boolean allowsMultipleLinks(RigSlot var1);

    @Nullable
    default public RigLink getLink(RigSlot slot) {
        return this.getLinks(slot).stream().findFirst().orElse(null);
    }

    @Nullable
    default public RigLinkable resolveLink(RigSlot slot) {
        RigLink link = this.getLink(slot);
        return link != null ? RigLinkRegistry.find(link.targetUuid()) : null;
    }

    public static UUID loadRigUUID(CompoundTag tag) {
        if (tag.hasUUID(TAG_RIG_UUID)) {
            return tag.getUUID(TAG_RIG_UUID);
        }
        return UUID.randomUUID();
    }

    public static void saveRigData(CompoundTag tag, UUID rigUUID, Map<RigSlot, Set<RigLink>> links) {
        tag.putUUID(TAG_RIG_UUID, rigUUID);
        ListTag list = new ListTag();
        for (Map.Entry<RigSlot, Set<RigLink>> entry : links.entrySet()) {
            for (RigLink link : entry.getValue()) {
                CompoundTag entryTag = link.toTag();
                entryTag.putString(TAG_LOCAL_SLOT, entry.getKey().name());
                list.add(entryTag);
            }
        }
        tag.put(TAG_RIG_LINKS, (Tag)list);
    }

    public static Map<RigSlot, Set<RigLink>> loadRigLinks(CompoundTag tag) {
        EnumMap<RigSlot, Set<RigLink>> links = new EnumMap<RigSlot, Set<RigLink>>(RigSlot.class);
        if (!tag.contains(TAG_RIG_LINKS, 9)) {
            return links;
        }
        ListTag list = tag.getList(TAG_RIG_LINKS, 10);
        for (int i = 0; i < list.size(); ++i) {
            RigSlot localSlot;
            CompoundTag entryTag = list.getCompound(i);
            try {
                localSlot = RigSlot.valueOf(entryTag.getString(TAG_LOCAL_SLOT));
            }
            catch (IllegalArgumentException e) {
                continue;
            }
            RigLink link = RigLink.fromTag(entryTag);
            if (link == null) continue;
            links.computeIfAbsent(localSlot, s -> new LinkedHashSet()).add(link);
        }
        return links;
    }
}

