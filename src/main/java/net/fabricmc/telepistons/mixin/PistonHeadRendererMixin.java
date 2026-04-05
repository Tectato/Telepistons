package net.fabricmc.telepistons.mixin;

import net.fabricmc.telepistons.access.PistonRenderStateAccess;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.block.MovingBlockRenderState;
import net.minecraft.client.renderer.blockentity.state.PistonHeadRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.telepistons.Telepistons;
import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;
import net.minecraft.client.renderer.blockentity.PistonHeadRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

@Mixin(PistonHeadRenderer.class)
public class PistonHeadRendererMixin {
    @Environment(EnvType.CLIENT)

    @Inject(
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V"),
            method = "Lnet/minecraft/client/renderer/blockentity/PistonHeadRenderer;submit(Lnet/minecraft/client/renderer/blockentity/state/PistonHeadRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V"
    )
    public void submitInjection(final PistonHeadRenderState state, final PoseStack poseStack, final SubmitNodeCollector submitNodeCollector, final CameraRenderState camera, CallbackInfo ci){
        PistonRenderStateAccess pistonRenderStateAccess = (PistonRenderStateAccess)state;
        MovingBlockRenderState arm = pistonRenderStateAccess.getArm();
        if (arm == null) return;
        PistonMovingBlockEntity pistonBlockEntity = pistonRenderStateAccess.getPistonBlockEntity();
        Level world = pistonBlockEntity.getLevel();
        if (world == null) return;

        Direction dir = pistonBlockEntity.getMovementDirection();
        float dist = 1 - (Math.abs(state.xOffset)
                + Math.abs(state.yOffset)
                + Math.abs(state.zOffset));
        poseStack.pushPose();

        float extendRate = -0.5F;
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

            poseStack.translate(-state.xOffset, -state.yOffset, -state.zOffset);
            poseStack.translate(.5f, .5f, .5f);

            if (extending) {
                squishFactors.lerp(new Vector3f(1f, 1f, 1f), dist);

                poseStack.translate(.25f * dx, .25f * dy, .25f * dz);
                poseStack.translate(-dx, -dy, -dz);

                poseStack.scale(
                        squishFactors.x(),
                        squishFactors.y(),
                        squishFactors.z());

                poseStack.translate(-.5f - .25f * dx, -.5f - .25f * dy, -.5f - .25f * dz);
                poseStack.translate(.5f * dx, .5f * dy, .5f * dz);

                poseStack.translate(.5f * dx, .5f * dy, .5f * dz);
            } else {
                Vector3f squish = new Vector3f(1f, 1f, 1f);
                squish.lerp(squishFactors, dist);

                poseStack.translate(-.25f * dx, -.25f * dy, -.25f * dz);

                poseStack.scale(
                        squish.x(),
                        squish.y(),
                        squish.z());

                poseStack.translate(-.5f - .25f * dx, -.5f - .25f * dy, -.5f - .25f * dz);
                poseStack.translate(-.5f * dx, -.5f * dy, -.5f * dz);
            }
        } else {
            poseStack.translate(extendRate * (double) state.xOffset, extendRate * (double) state.yOffset, extendRate * (double) state.zOffset);

            if (!pistonBlockEntity.isExtending()) {
                poseStack.translate(-.5f * dir.getStepX(), -.5f * dir.getStepY(), -.5f * dir.getStepZ());
            }
        }

        submitNodeCollector.submitMovingBlock(poseStack, arm);

        poseStack.popPose();
    }

    @Inject(at = @At("RETURN"), method = "extractRenderState(Lnet/minecraft/world/level/block/piston/PistonMovingBlockEntity;Lnet/minecraft/client/renderer/blockentity/state/PistonHeadRenderState;FLnet/minecraft/world/phys/Vec3;Lnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V")
    private void onUpdateRenderState(PistonMovingBlockEntity pistonBlockEntity, PistonHeadRenderState pistonBlockEntityRenderState, float f, Vec3 vec3d, ModelFeatureRenderer.CrumblingOverlay crumblingOverlayCommand, CallbackInfo ci) {
        ((PistonRenderStateAccess)pistonBlockEntityRenderState).setPistonBlockEntity(pistonBlockEntity);
    }

}
