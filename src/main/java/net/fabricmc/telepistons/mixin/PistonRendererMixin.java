package net.fabricmc.telepistons.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.util.math.random.Random;
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
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockModelRenderer;
import net.minecraft.client.render.block.entity.PistonBlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

@Mixin(PistonBlockEntityRenderer.class)
public class PistonRendererMixin {
    @Environment(EnvType.CLIENT)
    @Inject(at = @At("HEAD"), method = "render(Lnet/minecraft/block/entity/PistonBlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;II)V")
    private void render(PistonBlockEntity pistonBlockEntity, float f, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, int j, CallbackInfo info) {
        if (pistonBlockEntity.isSource()) {
            World world = pistonBlockEntity.getWorld();
            if (world != null) {
                BlockPos blockPos = pistonBlockEntity.getPos();
                Direction dir = pistonBlockEntity.getMovementDirection();
                float dist = 1 - (Math.abs(pistonBlockEntity.getRenderOffsetX(f))
                        + Math.abs(pistonBlockEntity.getRenderOffsetY(f))
                        + Math.abs(pistonBlockEntity.getRenderOffsetZ(f)));
                BlockModelRenderer.enableBrightnessCache();
                matrixStack.push();

                float extendRate = 0.5F;
                if (Telepistons.squishArm) {
                    boolean extending = pistonBlockEntity.isExtending();
                    float dx = dir.getOffsetX();
                    float dy = dir.getOffsetY();
                    float dz = dir.getOffsetZ();

                    Vector3f squishFactorsSrc =
                            (dx != 0f) ? Telepistons.squishFactorsX
                                    : (dy != 0f) ? Telepistons.squishFactorsY
                                    : Telepistons.squishFactorsZ;

                    Vector3f squishFactors = new Vector3f(squishFactorsSrc.x(), squishFactorsSrc.y(), squishFactorsSrc.z());

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
                    matrixStack.translate(extendRate * (double) pistonBlockEntity.getRenderOffsetX(f), extendRate * (double) pistonBlockEntity.getRenderOffsetY(f), extendRate * (double) pistonBlockEntity.getRenderOffsetZ(f));

                    if (!pistonBlockEntity.isExtending()) {
                        matrixStack.translate(-.5f * dir.getOffsetX(), -.5f * dir.getOffsetY(), -.5f * dir.getOffsetZ());
                    }
                }

                matrixStack.translate(.5f, .5f, .5f);
                matrixStack.multiply(Telepistons.getRotationQuaternion(pistonBlockEntity.isExtending() ? dir : dir.getOpposite()));
                matrixStack.translate(-.5f, -.5f, -.5f);

                BlockState state = pistonBlockEntity.getCachedState();
                MinecraftClient.getInstance().getBlockRenderManager().getModelRenderer().render(world, Telepistons.pistonArmBakedModel, state, blockPos, matrixStack, vertexConsumerProvider.getBuffer(RenderLayers.getMovingBlockLayer(state)), false, Random.create(), 1l, 0);

                matrixStack.pop();
                BlockModelRenderer.disableBrightnessCache();
            }
        }
    }

    @Shadow
    private void renderModel(BlockPos blockPos, BlockState blockState, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, World world, boolean bl, int i) {

    }
}
