package net.fabricmc.telepistons.mixin;

import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
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
					 boolean extending = pistonBlockEntity.isExtending();
		    		 float dx = dir.getOffsetX();
		    		 float dy = dir.getOffsetY();
		    		 float dz = dir.getOffsetZ();

					 Vec3f squishFactorsSrc =
							  (dx != 0f)?Telepistons.squishFactorsX
							 :(dy != 0f)?Telepistons.squishFactorsY
							 :Telepistons.squishFactorsZ;

					 Vec3f squishFactors = new Vec3f(squishFactorsSrc.getX(), squishFactorsSrc.getY(), squishFactorsSrc.getZ());

					 matrixStack.translate(.5f,.5f,.5f);

					 if(extending) {
						 squishFactors.lerp(new Vec3f(1f,1f,1f),dist);

						 matrixStack.translate(.25f*dx,.25f*dy,.25f*dz);
						 matrixStack.translate(-dx,-dy,-dz);

						 /*
						 matrixStack.scale(
								 (1 - Math.abs(dx)) + dist*Math.abs(dx),
								 (1 - Math.abs(dy)) + dist*Math.abs(dy),
								 (1 - Math.abs(dz)) + dist*Math.abs(dz));*/

						 matrixStack.scale(
								 squishFactors.getX(),
								 squishFactors.getY(),
								 squishFactors.getZ());

						 matrixStack.translate(-.5f - .25f*dx,-.5f - .25f*dy,-.5f - .25f*dz);
						 matrixStack.translate(.5f*dx,.5f*dy,.5f*dz);

						 matrixStack.translate(.5f*dx,.5f*dy,.5f*dz);
					 } else {
						 Vec3f squish = new Vec3f(1f,1f,1f);
						 squish.lerp(squishFactors,dist);

						 matrixStack.translate(-.25f*dx,-.25f*dy,-.25f*dz);

						 /*
						 matrixStack.scale(
								 1 - dist*Math.abs(dx),
								 1 - dist*Math.abs(dy),
								 1 - dist*Math.abs(dz));*/
						 matrixStack.scale(
								 squish.getX(),
								 squish.getY(),
								 squish.getZ());

						 matrixStack.translate(-.5f - .25f*dx,-.5f - .25f*dy,-.5f - .25f*dz);
						 matrixStack.translate(-.5f*dx,-.5f*dy,-.5f*dz);
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
