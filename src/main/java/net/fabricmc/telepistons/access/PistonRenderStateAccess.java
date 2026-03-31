package net.fabricmc.telepistons.access;

import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;

public interface PistonRenderStateAccess {
    PistonMovingBlockEntity getPistonBlockEntity();
    void setPistonBlockEntity(PistonMovingBlockEntity pistonBlockEntity);

    float getFValue();
    void setFValue(float f);

    ModelFeatureRenderer.CrumblingOverlay getCrumblingOverlayCommand();
    void setCrumblingOverlayCommand(ModelFeatureRenderer.CrumblingOverlay crumblingOverlayCommand);
}
