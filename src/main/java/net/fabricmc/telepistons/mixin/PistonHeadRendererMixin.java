package net.fabricmc.telepistons.mixin;

import java.util.Random;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.At;

import net.fabricmc.telepistons.Telepistons;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.PistonHeadBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.BlockModelRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockRenderView;

@Mixin(BlockModelRenderer.class)
public class PistonHeadRendererMixin {
	@Inject(at = @At("HEAD"), method = "render(Lnet/minecraft/world/BlockRenderView;Lnet/minecraft/client/render/model/BakedModel;Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;ZLjava/util/Random;JI)Z")
	public void render(BlockRenderView world, BakedModel model, BlockState state, BlockPos pos, MatrixStack matrix, VertexConsumer vertexConsumer, boolean cull, Random random, long seed, int overlay, CallbackInfoReturnable cbi) {
		if(state.isOf(Blocks.PISTON_HEAD)) {
			boolean bl = MinecraftClient.isAmbientOcclusionEnabled() && state.getLuminance() == 0 && model.useAmbientOcclusion();
			Vec3d vec3d = state.getModelOffset(world, pos);
			matrix.translate(vec3d.x, vec3d.y, vec3d.z);
			
			BlockState state2 = Telepistons.PISTON_ARM.getDefaultState().with(PistonHeadBlock.FACING, (state.get(PistonHeadBlock.FACING)));
			  
			try {
				if(bl) this.renderSmooth(world, MinecraftClient.getInstance().getBlockRenderManager().getModel(state2), state2, pos, matrix, vertexConsumer, cull, random, seed, overlay);
				else this.renderFlat(world, MinecraftClient.getInstance().getBlockRenderManager().getModel(state2), state2, pos, matrix, vertexConsumer, cull, random, seed, overlay);
			} catch (Throwable var17) {
			CrashReport crashReport = CrashReport.create(var17, "Tesselating block model");
			CrashReportSection crashReportSection = crashReport.addElement("Block model being tesselated");
			CrashReportSection.addBlockInfo(crashReportSection, world, pos, state);
			crashReportSection.add("Using AO", (Object)bl);
			throw new CrashException(crashReport);
			}
		}
	}
	
	@Shadow public boolean renderSmooth(BlockRenderView world, BakedModel model, BlockState state, BlockPos pos, MatrixStack buffer, VertexConsumer vertexConsumer, boolean cull, Random random, long seed, int overlay) {
		return true;
	}
	
	@Shadow public boolean renderFlat(BlockRenderView world, BakedModel model, BlockState state, BlockPos pos, MatrixStack buffer, VertexConsumer vertexConsumer, boolean cull, Random random, long l, int i) {
		return true;
	}
}
