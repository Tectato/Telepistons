package net.fabricmc.telepistons.mixin;

import net.fabricmc.telepistons.access.PistonRenderStateAccess;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.block.MovingBlockRenderState;
import net.minecraft.client.renderer.blockentity.state.PistonHeadRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.core.Holder;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.telepistons.Telepistons;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.blockentity.PistonHeadRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

@Mixin(PistonHeadRenderer.class)
public class PistonHeadRendererMixin {
    @Environment(EnvType.CLIENT)
    @Inject(at = @At("HEAD"),
            method = "submit(Lnet/minecraft/client/renderer/blockentity/state/PistonHeadRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V")
    private void render(PistonHeadRenderState pistonBlockEntityRenderState, PoseStack matrixStack, SubmitNodeCollector orderedRenderCommandQueue, CameraRenderState cameraRenderState, CallbackInfo ci) {
        PistonRenderStateAccess pistonRenderStateAccess = (PistonRenderStateAccess)pistonBlockEntityRenderState;
        PistonMovingBlockEntity pistonBlockEntity = pistonRenderStateAccess.getPistonBlockEntity();
        float fValue = pistonRenderStateAccess.getFValue();

        // pistonBlockEntityRenderState.offsetX/Y/Z aren't interpolated, so this fValue is supposed to bring some interpolation in.
        float offsetX = pistonBlockEntity.getXOff(fValue);
        float offsetY = pistonBlockEntity.getYOff(fValue);
        float offsetZ = pistonBlockEntity.getZOff(fValue);

        if (pistonBlockEntity.isSourcePiston()) {
            Level world = pistonBlockEntity.getLevel();
            if (world != null) {
                Direction dir = pistonBlockEntity.getMovementDirection();
                float dist = 1 - (Math.abs(offsetX)
                        + Math.abs(offsetY)
                        + Math.abs(offsetZ));
                ModelBlockRenderer.enableCaching();
                matrixStack.pushPose();

                float extendRate = 0.5F;
                if (Telepistons.squishArm) {
                    boolean extending = pistonBlockEntity.isExtending();
                    float dx = dir.getStepX();
                    float dy = dir.getStepY();
                    float dz = dir.getStepZ();

                    Vec3 squishFactorsSrc =
                            (dx != 0f) ? Telepistons.squishFactorsX
                                    : (dy != 0f) ? Telepistons.squishFactorsY
                                    : Telepistons.squishFactorsZ;

                    Vector3f squishFactors = new Vector3f((float) squishFactorsSrc.x(), (float) squishFactorsSrc.y(), (float) squishFactorsSrc.z());

                    matrixStack.translate(.5f, .5f, .5f);

                    if (extending) {
                        squishFactors.lerp(new Vector3f(1f, 1f, 1f), dist);

                        matrixStack.translate(.25f * dx, .25f * dy, .25f * dz);
                        matrixStack.translate(-dx, -dy, -dz);

                        matrixStack.scale(
                                squishFactors.x(),
                                squishFactors.y(),
                                squishFactors.z());

                        matrixStack.translate(-.5f - .25f * dx, -.5f - .25f * dy, -.5f - .25f * dz);
                        matrixStack.translate(.5f * dx, .5f * dy, .5f * dz);

                        matrixStack.translate(.5f * dx, .5f * dy, .5f * dz);
                    } else {
                        Vector3f squish = new Vector3f(1f, 1f, 1f);
                        squish.lerp(squishFactors, dist);

                        matrixStack.translate(-.25f * dx, -.25f * dy, -.25f * dz);

                        matrixStack.scale(
                                squish.x(),
                                squish.y(),
                                squish.z());

                        matrixStack.translate(-.5f - .25f * dx, -.5f - .25f * dy, -.5f - .25f * dz);
                        matrixStack.translate(-.5f * dx, -.5f * dy, -.5f * dz);
                    }
                } else {
                    matrixStack.translate(extendRate * (double) offsetX, extendRate * (double) offsetY, extendRate * (double) offsetZ);

                    if (!pistonBlockEntity.isExtending()) {
                        matrixStack.translate(-.5f * dir.getStepX(), -.5f * dir.getStepY(), -.5f * dir.getStepZ());
                    }
                }

                matrixStack.translate(.5f, .5f, .5f);
                matrixStack.mulPose(Telepistons.getRotationQuaternion(pistonBlockEntity.isExtending() ? dir : dir.getOpposite()));
                matrixStack.translate(-.5f, -.5f, -.5f);

                BlockState state = pistonBlockEntityRenderState.blockState;
                RenderType renderLayer = ItemBlockRenderTypes.getMovingBlockRenderType(state);

                orderedRenderCommandQueue.submitBlockModel(matrixStack, renderLayer, Telepistons.pistonArmBakedModel,
                        1, 1, 1, pistonBlockEntityRenderState.lightCoords, OverlayTexture.NO_OVERLAY, 0);

                matrixStack.popPose();
                ModelBlockRenderer.clearCache();
            }
        }
    }

    @Inject(at = @At("RETURN"), method = "extractRenderState(Lnet/minecraft/world/level/block/piston/PistonMovingBlockEntity;Lnet/minecraft/client/renderer/blockentity/state/PistonHeadRenderState;FLnet/minecraft/world/phys/Vec3;Lnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V")
    private void onUpdateRenderState(PistonMovingBlockEntity pistonBlockEntity, PistonHeadRenderState pistonBlockEntityRenderState, float f, Vec3 vec3d, ModelFeatureRenderer.CrumblingOverlay crumblingOverlayCommand, CallbackInfo ci) {
        ((PistonRenderStateAccess)pistonBlockEntityRenderState).setPistonBlockEntity(pistonBlockEntity);
        ((PistonRenderStateAccess)pistonBlockEntityRenderState).setFValue(f);
        ((PistonRenderStateAccess)pistonBlockEntityRenderState).setCrumblingOverlayCommand(crumblingOverlayCommand);
    }
}
