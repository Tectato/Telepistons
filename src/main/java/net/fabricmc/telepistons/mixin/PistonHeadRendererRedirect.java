package net.fabricmc.telepistons.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.block.BlockState;
import net.minecraft.block.PistonHeadBlock;
import net.minecraft.block.enums.PistonType;
import net.minecraft.client.render.block.entity.PistonBlockEntityRenderer;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.Direction;

@Mixin(PistonBlockEntityRenderer.class)
abstract class PistonHeadRendererRedirect {
	@Redirect(method = "render",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;with(Lnet/minecraft/state/property/Property;Ljava/lang/Comparable;)Ljava/lang/Object;"))
	private Object returnShortHead(BlockState blockState, Property property, Comparable state) {
		if(property.equals(PistonHeadBlock.SHORT)) {
			return blockState.with(PistonHeadBlock.SHORT, true);
		} else {
			if(state instanceof Boolean) {
				return blockState.with(property, (Boolean)state);
			} else if(state instanceof PistonType){
				return blockState.with(PistonHeadBlock.TYPE, (PistonType)state);
			} else {
				return blockState.with(PistonHeadBlock.FACING, (Direction)state);
			}
		}
	}
}
