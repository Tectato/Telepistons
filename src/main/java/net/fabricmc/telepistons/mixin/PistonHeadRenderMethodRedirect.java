package net.fabricmc.telepistons.mixin;

import net.minecraft.client.renderer.block.MovingBlockRenderState;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.piston.PistonHeadBlock;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.PistonHeadRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

@Mixin(PistonHeadRenderer.class)
abstract class PistonHeadRenderMethodRedirect {
	@Environment(EnvType.CLIENT)

	@Redirect(
			method = "extractRenderState(Lnet/minecraft/world/level/block/piston/PistonMovingBlockEntity;Lnet/minecraft/client/renderer/blockentity/state/PistonHeadRenderState;FLnet/minecraft/world/phys/Vec3;Lnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/blockentity/PistonHeadRenderer;createMovingBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Holder;Lnet/minecraft/world/level/Level;)Lnet/minecraft/client/renderer/block/MovingBlockRenderState;"))
	private MovingBlockRenderState renderRedirect(BlockPos blockPos, BlockState blockState, Holder<Biome> biome, Level world) {
		if(blockState.is(Blocks.PISTON_HEAD) && !blockState.getValue(PistonHeadBlock.SHORT)) {
			return createMovingBlock(blockPos, blockState.setValue(PistonHeadBlock.SHORT, true), biome, world);
		} else {
			return createMovingBlock(blockPos, blockState, biome, world);
		}
	}
	
	@Shadow
	private static MovingBlockRenderState createMovingBlock(BlockPos pos, BlockState blockState, Holder<Biome> biome, Level world) {
        return null;
    }
}
