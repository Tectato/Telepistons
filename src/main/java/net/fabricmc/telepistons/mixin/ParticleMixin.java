package net.fabricmc.telepistons.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.fabricmc.telepistons.Telepistons;
import net.minecraft.block.PistonBlock;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

@Mixin(PistonBlock.class)
public class ParticleMixin {
	@Inject(at = @At("HEAD"), method = "move("
			+ "Lnet/minecraft/world/World;"
			+ "Lnet/minecraft/util/math/BlockPos;"
			+ "Lnet/minecraft/util/math/Direction;"
			+ "Z)Z")
	public void spawnParticles(World world, BlockPos pos, Direction dir, boolean retract, CallbackInfoReturnable info) {
		if(Telepistons.emitSteam) {
			int signFlip = retract ? 1 : -1;
			float dx = dir.getOffsetX();
			float dy = dir.getOffsetY();
			float dz = dir.getOffsetZ();
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
