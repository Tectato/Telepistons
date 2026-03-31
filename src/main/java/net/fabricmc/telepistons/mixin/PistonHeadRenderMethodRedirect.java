package net.fabricmc.telepistons.mixin;

import net.minecraft.client.render.block.MovingBlockRenderState;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.biome.Biome;
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

	@Redirect(
			method = "updateRenderState(Lnet/minecraft/block/entity/PistonBlockEntity;Lnet/minecraft/client/render/block/entity/state/PistonBlockEntityRenderState;FLnet/minecraft/util/math/Vec3d;Lnet/minecraft/client/render/command/ModelCommandRenderer$CrumblingOverlayCommand;)V",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/block/entity/PistonBlockEntityRenderer;renderModel(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/registry/entry/RegistryEntry;Lnet/minecraft/world/World;)Lnet/minecraft/client/render/block/MovingBlockRenderState;"))
	private MovingBlockRenderState renderRedirect(BlockPos blockPos, BlockState blockState, RegistryEntry<Biome> biome, World world) {
		if(blockState.isOf(Blocks.PISTON_HEAD) && !blockState.get(PistonHeadBlock.SHORT)) {
			return renderModel(blockPos, blockState.with(PistonHeadBlock.SHORT, true), biome, world);
		} else {
			return renderModel(blockPos, blockState, biome, world);
		}
	}
	
	@Shadow
	private static MovingBlockRenderState renderModel(BlockPos pos, BlockState blockState, RegistryEntry<Biome> biome, World world) {
        return null;
    }
}
