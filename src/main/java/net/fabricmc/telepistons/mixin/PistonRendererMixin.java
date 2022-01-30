package net.fabricmc.telepistons.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.telepistons.Telepistons;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.PistonBlock;
import net.minecraft.block.PistonHeadBlock;
import net.minecraft.block.entity.PistonBlockEntity;
import net.minecraft.block.enums.PistonType;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockModelRenderer;
import net.minecraft.client.render.block.entity.PistonBlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

@Mixin(PistonBlockEntityRenderer.class)
public class PistonRendererMixin {
	@Environment(EnvType.CLIENT)
	@Inject(at = @At("HEAD"), method = "render(Lnet/minecraft/block/entity/PistonBlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;II)V")
	private void render(PistonBlockEntity pistonBlockEntity, float f, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, int j, CallbackInfo info) {
		if(pistonBlockEntity.isSource()) {
			  World world = pistonBlockEntity.getWorld();
			  if (world != null) {
				 BlockPos blockPos = pistonBlockEntity.getPos();
				 Direction dir = pistonBlockEntity.getMovementDirection();
				 float dist = 1-(Math.abs(pistonBlockEntity.getRenderOffsetX(f))
						 + Math.abs(pistonBlockEntity.getRenderOffsetY(f))
						 + Math.abs(pistonBlockEntity.getRenderOffsetZ(f)));
		    	 BlockModelRenderer.enableBrightnessCache();
		    	 matrixStack.push();
		    	 float extendRate = 0.5F;
		    	 if(Telepistons.squishArm) {
		    		 float dx = dir.getOffsetX();
		    		 float dy = dir.getOffsetY();
		    		 float dz = dir.getOffsetZ();

		    		 if(pistonBlockEntity.isExtending()) {
		    			 matrixStack.translate(.5f*dist*dx, .5f*dist*dy, .5f*dist*dz);
		    			 matrixStack.translate(-.75f*dx, -.75f*dy, -.75f*dz);
		    			 matrixStack.translate(.5f, .5f, .5f);
			    		 matrixStack.scale(
			    				 (1 - Math.abs(dx)) + dist*Math.abs(dx),
			    				 (1 - Math.abs(dy)) + dist*Math.abs(dy),
			    				 (1 - Math.abs(dz)) + dist*Math.abs(dz));
			    		 matrixStack.translate(-.5f,-.5f,-.5f);
		    		 } else {
		    			 matrixStack.translate(.5f*dist*dx, .5f*dist*dy, .5f*dist*dz);
		    			 matrixStack.translate(-.75f*dx, -.75f*dy, -.75f*dz);
		    			 matrixStack.translate(.5f, .5f, .5f);
		    			 matrixStack.scale(
			    				 (1 - dist*Math.abs(dx)),
			    				 (1 - dist*Math.abs(dy)),
			    				 (1 - dist*Math.abs(dz)));
		    			 matrixStack.translate(-.5f,-.5f,-.5f);
		    		 }
		    	 } else {
			    	 matrixStack.translate(extendRate*(double)pistonBlockEntity.getRenderOffsetX(f), extendRate*(double)pistonBlockEntity.getRenderOffsetY(f), extendRate*(double)pistonBlockEntity.getRenderOffsetZ(f));
			    	 
					 if(!pistonBlockEntity.isExtending()) {
						 Direction d = pistonBlockEntity.getMovementDirection();
						 matrixStack.translate(-.5f*d.getOffsetX(),-.5f*d.getOffsetY(),-.5f*d.getOffsetZ());
					 }
		    	 }
		    	 BlockState blockState2 = (BlockState)((BlockState)Telepistons.PISTON_ARM.getDefaultState()).with(PistonHeadBlock.FACING, pistonBlockEntity.getFacing());
		    	 this.renderModel(blockPos, blockState2, matrixStack, vertexConsumerProvider, world, false, j);

		    	 matrixStack.pop();
		    	 BlockModelRenderer.disableBrightnessCache();
			  }
		}
	}
	
	@Shadow private void renderModel(BlockPos blockPos, BlockState blockState, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, World world, boolean bl, int i) {
		
	}
}
