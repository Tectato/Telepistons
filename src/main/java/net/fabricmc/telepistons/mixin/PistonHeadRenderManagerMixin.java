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
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.BlockModelRenderer;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;

@Mixin(BlockRenderManager.class)
public class PistonHeadRenderManagerMixin {
	private BlockModelRenderer blockModelRenderer;
	
	@Inject(at = @At("HEAD"), method = "renderBlock(Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/BlockRenderView;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;ZLjava/util/Random;)Z")
	public void renderBlock(BlockState state, BlockPos pos, BlockRenderView world, MatrixStack matrix, VertexConsumer vertexConsumer, boolean cull, Random random, CallbackInfoReturnable cbi) {
		System.out.println("RenderManager Mixin");
		if(state.isOf(Blocks.PISTON_HEAD)) {
			System.out.println("Rendering Piston");
			try {
				BlockState state2 = Telepistons.PISTON_ARM.getDefaultState().with(PistonHeadBlock.FACING, (state.get(PistonHeadBlock.FACING)));
				
		        this.blockModelRenderer.render(world, this.getModel(state2), state2, pos, matrix, vertexConsumer, cull, random, state.getRenderingSeed(pos), OverlayTexture.DEFAULT_UV);
			} catch (Throwable var11) {
				CrashReport crashReport = CrashReport.create(var11, "Tesselating block in world");
				CrashReportSection crashReportSection = crashReport.addElement("Block being tesselated");
				CrashReportSection.addBlockInfo(crashReportSection, world, pos, state);
				throw new CrashException(crashReport);
		    }
		}
	}

	@Shadow private BakedModel getModel(BlockState state) {return null;}
}
