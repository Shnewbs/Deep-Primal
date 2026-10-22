package tfcmt.tfcitembarrelsandmore.client.model;

import java.util.List;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tfcmt.tfcitembarrelsandmore.client.BraceClientCache;

public class TFCBarrelBracedModel
implements BakedModel {
    public static final ModelProperty<BlockPos> BRACE_POS = new ModelProperty();
    private final BakedModel tfcModel;
    @Nullable
    private final BakedModel bracedModel;

    public TFCBarrelBracedModel(BakedModel tfcModel, @Nullable BakedModel bracedModel) {
        this.tfcModel = tfcModel;
        this.bracedModel = bracedModel;
    }

    @NotNull
    public ModelData getModelData(@NotNull BlockAndTintGetter level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull ModelData modelData) {
        return modelData.derive().with(BRACE_POS, pos.immutable()).build();
    }

    @NotNull
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand, @NotNull ModelData data, @Nullable RenderType renderType) {
        if (!this.resolveBraced(data)) {
            return this.tfcModel.getQuads(state, side, rand, data, renderType);
        }
        return this.bracedModel.getQuads(state, side, rand, data, renderType);
    }

    @NotNull
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand) {
        return this.tfcModel.getQuads(state, side, rand);
    }

    private boolean resolveBraced(@NotNull ModelData data) {
        if (this.bracedModel == null) {
            return false;
        }
        BlockPos pos = (BlockPos)data.get(BRACE_POS);
        if (pos == null) {
            return false;
        }
        return BraceClientCache.get(pos) != null;
    }

    public boolean useAmbientOcclusion() {
        return this.tfcModel.useAmbientOcclusion();
    }

    

    @Override public net.neoforged.neoforge.common.util.TriState useAmbientOcclusion(BlockState state, ModelData data, RenderType renderType) {
        return this.tfcModel.useAmbientOcclusion(state, data, renderType);
    }

    public boolean isGui3d() {
        return this.tfcModel.isGui3d();
    }

    public boolean usesBlockLight() {
        return this.tfcModel.usesBlockLight();
    }

    public boolean isCustomRenderer() {
        return false;
    }

    public TextureAtlasSprite getParticleIcon() {
        return this.tfcModel.getParticleIcon();
    }

    public TextureAtlasSprite getParticleIcon(ModelData data) {
        return this.tfcModel.getParticleIcon(data);
    }

    public ItemOverrides getOverrides() {
        return this.tfcModel.getOverrides();
    }

    public ItemTransforms getTransforms() {
        return this.tfcModel.getTransforms();
    }
}

