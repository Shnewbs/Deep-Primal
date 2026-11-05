package tfcmt.tfcitembarrelsandmore.common.blockentities.mast;

import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import tfcmt.tfcitembarrelsandmore.common.blockentities.mast.RigSlot;

public record RigLink(UUID targetUuid, RigSlot targetSlot) {
    private static final String TAG_UUID = "TargetUuid";
    private static final String TAG_SLOT = "TargetSlot";

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID(TAG_UUID, this.targetUuid);
        tag.putString(TAG_SLOT, this.targetSlot.name());
        return tag;
    }

    @Nullable
    public static RigLink fromTag(CompoundTag tag) {
        if (!tag.hasUUID(TAG_UUID)) {
            return null;
        }
        try {
            RigSlot slot = RigSlot.valueOf(tag.getString(TAG_SLOT));
            return new RigLink(tag.getUUID(TAG_UUID), slot);
        }
        catch (IllegalArgumentException e) {
            return null;
        }
    }
}

