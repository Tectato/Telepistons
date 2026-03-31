package net.fabricmc.telepistons.mixin;

import net.fabricmc.telepistons.access.PistonRenderStateAccess;
import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;
import net.minecraft.client.renderer.blockentity.state.PistonHeadRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(PistonHeadRenderState.class)
public class PistonRenderStateMixin implements PistonRenderStateAccess {
    @Unique
    public PistonMovingBlockEntity pistonBlockEntity;

    @Unique
    public float f;

    @Unique
    public ModelFeatureRenderer.CrumblingOverlay crumblingOverlayCommand;

    @Unique
    @Override
    public PistonMovingBlockEntity getPistonBlockEntity() {
        return pistonBlockEntity;
    }

    @Unique
    @Override
    public void setPistonBlockEntity(PistonMovingBlockEntity pistonBlockEntity) {
        this.pistonBlockEntity = pistonBlockEntity;
    }

    @Override
    public float getFValue() {
        return f;
    }

    @Override
    public void setFValue(float f) {
        this.f = f;
    }

    @Override
    public ModelFeatureRenderer.CrumblingOverlay getCrumblingOverlayCommand() {
        return this.crumblingOverlayCommand;
    }

    @Override
    public void setCrumblingOverlayCommand(ModelFeatureRenderer.CrumblingOverlay crumblingOverlayCommand) {
        this.crumblingOverlayCommand = crumblingOverlayCommand;
    }
}
