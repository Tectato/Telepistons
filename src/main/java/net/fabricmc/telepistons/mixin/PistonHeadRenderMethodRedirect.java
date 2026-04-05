package net.fabricmc.telepistons.mixin;

import net.fabricmc.telepistons.PistonArm;
import net.fabricmc.telepistons.Telepistons;
import net.fabricmc.telepistons.access.PistonRenderStateAccess;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.MovingBlockRenderState;
import net.minecraft.client.renderer.blockentity.state.PistonHeadRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.piston.PistonHeadBlock;
import net.minecraft.client.renderer.blockentity.PistonHeadRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PistonHeadRenderer.class)
abstract class PistonHeadRenderMethodRedirect {
	@Environment(EnvType.CLIENT)

	@Redirect(
			method = "Lnet/minecraft/client/renderer/blockentity/PistonHeadRenderer;extractRenderState(Lnet/minecraft/world/level/block/piston/PistonMovingBlockEntity;Lnet/minecraft/client/renderer/blockentity/state/PistonHeadRenderState;FLnet/minecraft/world/phys/Vec3;Lnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/blockentity/PistonHeadRenderer;createMovingBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Holder;Lnet/minecraft/client/multiplayer/ClientLevel;)Lnet/minecraft/client/renderer/block/MovingBlockRenderState;"))
	private MovingBlockRenderState renderRedirect(final BlockPos pos, final BlockState blockState, final Holder<Biome> biome, final ClientLevel level) {
		if(blockState.is(Blocks.PISTON_HEAD) && !blockState.getValue(PistonHeadBlock.SHORT)) {
			return createMovingBlock(pos, blockState.setValue(PistonHeadBlock.SHORT, true), biome, level);
		} else {
			return createMovingBlock(pos, blockState, biome, level);
		}
	}

	@Inject(
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;getBiome(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/core/Holder;"),
			method = "extractRenderState*"
	)
	public void extractRenderStateInjection(final PistonMovingBlockEntity blockEntity, final PistonHeadRenderState state, final float partialTicks, final Vec3 cameraPosition, final ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress, CallbackInfo ci){
		BlockPos pos = blockEntity.getBlockPos().relative(blockEntity.getMovementDirection().getOpposite());
		Level level = blockEntity.getLevel();
		if (!(level instanceof ClientLevel)) return;
		if (!blockEntity.isSourcePiston()) return;
		((PistonRenderStateAccess)state).setArm(createMovingBlock(pos, Telepistons.pistonArmBlock.defaultBlockState().setValue(PistonArm.FACING, blockEntity.getDirection()), level.getBiome(pos), (ClientLevel) level));
	}
	
	@Shadow
	private static MovingBlockRenderState createMovingBlock(final BlockPos pos, final BlockState blockState, final Holder<Biome> biome, final ClientLevel level) {
        return null;
    }
}
