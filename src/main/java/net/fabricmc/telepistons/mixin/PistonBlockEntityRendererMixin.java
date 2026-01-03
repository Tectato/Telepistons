package net.fabricmc.telepistons.mixin;

import net.fabricmc.telepistons.access.PistonRenderStateAccess;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.block.MovingBlockRenderState;
import net.minecraft.client.render.block.entity.state.PistonBlockEntityRenderState;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.biome.Biome;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.telepistons.Telepistons;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.PistonBlockEntity;
import net.minecraft.client.render.block.BlockModelRenderer;
import net.minecraft.client.render.block.entity.PistonBlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

@Mixin(PistonBlockEntityRenderer.class)
public class PistonBlockEntityRendererMixin {
    @Environment(EnvType.CLIENT)
    @Inject(at = @At("HEAD"),
            method = "render(Lnet/minecraft/client/render/block/entity/state/PistonBlockEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;Lnet/minecraft/client/render/state/CameraRenderState;)V")
    private void render(PistonBlockEntityRenderState pistonBlockEntityRenderState, MatrixStack matrixStack, OrderedRenderCommandQueue orderedRenderCommandQueue, CameraRenderState cameraRenderState, CallbackInfo ci) {
        PistonRenderStateAccess pistonRenderStateAccess = (PistonRenderStateAccess)pistonBlockEntityRenderState;
        PistonBlockEntity pistonBlockEntity = pistonRenderStateAccess.getPistonBlockEntity();
        float fValue = pistonRenderStateAccess.getFValue();

        // pistonBlockEntityRenderState.offsetX/Y/Z aren't interpolated, so this fValue is supposed to bring some interpolation in.
        float offsetX = pistonBlockEntity.getRenderOffsetX(fValue);
        float offsetY = pistonBlockEntity.getRenderOffsetY(fValue);
        float offsetZ = pistonBlockEntity.getRenderOffsetZ(fValue);

        if (pistonBlockEntity.isSource()) {
            World world = pistonBlockEntity.getWorld();
            if (world != null) {
                Direction dir = pistonBlockEntity.getMovementDirection();
                float dist = 1 - (Math.abs(offsetX)
                        + Math.abs(offsetY)
                        + Math.abs(offsetZ));
                BlockModelRenderer.enableBrightnessCache();
                matrixStack.push();

                float extendRate = 0.5F;
                if (Telepistons.squishArm) {
                    boolean extending = pistonBlockEntity.isExtending();
                    float dx = dir.getOffsetX();
                    float dy = dir.getOffsetY();
                    float dz = dir.getOffsetZ();

                    Vec3d squishFactorsSrc =
                            (dx != 0f) ? Telepistons.squishFactorsX
                                    : (dy != 0f) ? Telepistons.squishFactorsY
                                    : Telepistons.squishFactorsZ;

                    Vector3f squishFactors = new Vector3f((float) squishFactorsSrc.getX(), (float) squishFactorsSrc.getY(), (float) squishFactorsSrc.getZ());

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
                        matrixStack.translate(-.5f * dir.getOffsetX(), -.5f * dir.getOffsetY(), -.5f * dir.getOffsetZ());
                    }
                }

                matrixStack.translate(.5f, .5f, .5f);
                matrixStack.multiply(Telepistons.getRotationQuaternion(pistonBlockEntity.isExtending() ? dir : dir.getOpposite()));
                matrixStack.translate(-.5f, -.5f, -.5f);

                BlockState state = pistonBlockEntityRenderState.blockState;
                RenderLayer renderLayer = RenderLayers.getMovingBlockLayer(state);

                orderedRenderCommandQueue.submitBlockStateModel(matrixStack, renderLayer, Telepistons.pistonArmBakedModel,
                        1, 1, 1, pistonBlockEntityRenderState.lightmapCoordinates, OverlayTexture.DEFAULT_UV, 0);

                matrixStack.pop();
                BlockModelRenderer.disableBrightnessCache();
            }
        }
    }

    @Inject(at = @At("RETURN"), method = "updateRenderState(Lnet/minecraft/block/entity/PistonBlockEntity;Lnet/minecraft/client/render/block/entity/state/PistonBlockEntityRenderState;FLnet/minecraft/util/math/Vec3d;Lnet/minecraft/client/render/command/ModelCommandRenderer$CrumblingOverlayCommand;)V")
    private void onUpdateRenderState(PistonBlockEntity pistonBlockEntity, PistonBlockEntityRenderState pistonBlockEntityRenderState, float f, Vec3d vec3d, ModelCommandRenderer.CrumblingOverlayCommand crumblingOverlayCommand, CallbackInfo ci) {
        ((PistonRenderStateAccess)pistonBlockEntityRenderState).setPistonBlockEntity(pistonBlockEntity);
        ((PistonRenderStateAccess)pistonBlockEntityRenderState).setFValue(f);
        ((PistonRenderStateAccess)pistonBlockEntityRenderState).setCrumblingOverlayCommand(crumblingOverlayCommand);
    }
}
