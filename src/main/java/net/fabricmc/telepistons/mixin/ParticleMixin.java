package net.fabricmc.telepistons.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.fabricmc.telepistons.Telepistons;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

@Mixin(PistonBaseBlock.class)
public class ParticleMixin {
	@Inject(at = @At("HEAD"), method = "moveBlocks(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;Z)Z")
	public void spawnParticles(Level world, BlockPos pos, Direction dir, boolean retract, CallbackInfoReturnable info) {
		if(Telepistons.emitSteam) {
			float dx = dir.getStepX();
			float dy = dir.getStepY();
			float dz = dir.getStepZ();
			for(int i=0; i<Telepistons.particleCount; i++) {
				world.addParticle(ParticleTypes.CLOUD,
						pos.getX()+.5f+(dx*.5f),
						pos.getY()+.5f+(dy*.5f),
						pos.getZ()+.5f+(dz*.5f),
						.125f*(.5f-Telepistons.random.nextFloat())*Math.abs(dy+dz),
						.125f*(.5f-Telepistons.random.nextFloat())*Math.abs(dx+dz),
						.125f*(.5f-Telepistons.random.nextFloat())*Math.abs(dy+dx));
			}
		}
	}
}
