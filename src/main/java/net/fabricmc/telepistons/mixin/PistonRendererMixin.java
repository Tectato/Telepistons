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
				 //float dist = pistonBlockEntity.getProgress(f);
				 float dist = 1-(Math.abs(pistonBlockEntity.getRenderOffsetX(f))
						 + Math.abs(pistonBlockEntity.getRenderOffsetY(f))
						 + Math.abs(pistonBlockEntity.getRenderOffsetZ(f)));
				 /*if(pistonBlockEntity.getProgress(f) < .2f && Telepistons.random.nextFloat() < .2f) {
					 float dx = dir.getOffsetX();
					 float dy = dir.getOffsetY();
					 float dz = dir.getOffsetZ();
					 world.addParticle(ParticleTypes.CLOUD,
							 blockPos.getX()+Telepistons.random.nextFloat()*Math.abs(dy+dz),
							 blockPos.getY()+Telepistons.random.nextFloat()*Math.abs(dx+dz),
							 blockPos.getZ()+Telepistons.random.nextFloat()*Math.abs(dx+dy),
							 .0625f*dx,
							 .0625f*dy,
							 .0625f*dz);
				 }*/
				 //BlockState blockState = pistonBlockEntity.getPushedBlock();
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
		    		 /*
		    		 float dx = Math.abs(dir.getOffsetX());
		    		 float dy = Math.abs(dir.getOffsetY());
		    		 float dz = Math.abs(dir.getOffsetZ());
		    		 
		    		 //matrixStack.translate(-.5f, -.5f, -.5f);
		    		 if(pistonBlockEntity.isExtending()) {
			    		 matrixStack.scale(
			    				 (1-dx) + pistonBlockEntity.getProgress(f)*dx,
			    				 (1-dy) + pistonBlockEntity.getProgress(f)*dy,
			    				 (1-dz) + pistonBlockEntity.getProgress(f)*dz);
		    		 } else {
		    			 matrixStack.scale(
			    				 ((1) - pistonBlockEntity.getProgress(f)*dx),
			    				 ((1) - pistonBlockEntity.getProgress(f)*dy),
			    				 ((1) - pistonBlockEntity.getProgress(f)*dz));
		    		 }
		    		 //matrixStack.translate(.5f, .5f, .5f);
		    		 
		    		 int signflip = pistonBlockEntity.isExtending() ? 1 : -1;
		    		 dx = dir.getOffsetX();
		    		 dy = dir.getOffsetY();
		    		 dz = dir.getOffsetZ();
		    		 
		    		 if(dx + dy + dz < 0) {
						 matrixStack.translate(
		    					 .5f*dx*pistonBlockEntity.getProgress(f),
		    					 .5f*dy*pistonBlockEntity.getProgress(f),
		    					 .5f*dz*pistonBlockEntity.getProgress(f));
						 if(pistonBlockEntity.isExtending()) {
							 //matrixStack.translate(-1.5f*dx, -1.5f*dy, -1.5f*dz);
						 }
		    		 }
		    		 
		    		 if(pistonBlockEntity.isExtending()) {
		    			 matrixStack.translate(.5f*dx,.5f*dy,.5f*dz);
					 } else {
						 matrixStack.translate(-.5f*dx,-.5f*dy,-.5f*dz);
					 }
		    		 
		    		 
		    		 //matrixStack.translate(-.5f*signflip*dx,-.5f*signflip*dy,-.5f*signflip*dz);
		    		 /*
		    		 matrixStack.translate(
	    					 .5f*dx*pistonBlockEntity.getProgress(f),
	    					 .5f*dy*pistonBlockEntity.getProgress(f),
	    					 .5f*dz*pistonBlockEntity.getProgress(f));
		    		 */
		    	 } else {
			    	 matrixStack.translate(extendRate*(double)pistonBlockEntity.getRenderOffsetX(f), extendRate*(double)pistonBlockEntity.getRenderOffsetY(f), extendRate*(double)pistonBlockEntity.getRenderOffsetZ(f));
			    	 
					 if(!pistonBlockEntity.isExtending()) {
						 Direction d = pistonBlockEntity.getMovementDirection();
						 matrixStack.translate(-.5f*d.getOffsetX(),-.5f*d.getOffsetY(),-.5f*d.getOffsetZ());
					 }
		    	 }
		    	 BlockState blockState2 = (BlockState)((BlockState)Telepistons.PISTON_ARM.getDefaultState()).with(PistonHeadBlock.FACING, pistonBlockEntity.getFacing());
		    	 this.method_3575(blockPos, blockState2, matrixStack, vertexConsumerProvider, world, false, j);

		    	 matrixStack.pop();
		    	 BlockModelRenderer.disableBrightnessCache();
			  }
		}
	}
	
	@Shadow private void method_3575(BlockPos blockPos, BlockState blockState, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, World world, boolean bl, int i) {
		
	}
}
