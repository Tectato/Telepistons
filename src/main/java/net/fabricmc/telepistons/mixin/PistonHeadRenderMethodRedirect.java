package net.fabricmc.telepistons.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.PistonHeadBlock;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.PistonBlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(PistonBlockEntityRenderer.class)
abstract class PistonHeadRenderMethodRedirect {
	@Environment(EnvType.CLIENT)
	@Redirect(method = "render",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/block/entity/PistonBlockEntityRenderer;renderModel(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/world/World;ZI)V"))
	private void renderRedirect(PistonBlockEntityRenderer PBER, BlockPos blockPos, BlockState blockState, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, World world, boolean bl, int i) {
		if(blockState.isOf(Blocks.PISTON_HEAD) && !blockState.get(PistonHeadBlock.SHORT)) {
			this.renderModel(blockPos, blockState.with(PistonHeadBlock.SHORT, true), matrixStack, vertexConsumerProvider, world, bl, i);
		} else {
			this.renderModel(blockPos, blockState, matrixStack, vertexConsumerProvider, world, bl, i);
		}
	}
	
	@Shadow private void renderModel(BlockPos blockPos, BlockState blockState, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, World world, boolean bl, int i) {
		
	}
}
