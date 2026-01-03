package net.fabricmc.telepistons.access;

import net.minecraft.block.entity.PistonBlockEntity;
import net.minecraft.client.render.command.ModelCommandRenderer;

public interface PistonRenderStateAccess {
    PistonBlockEntity getPistonBlockEntity();
    void setPistonBlockEntity(PistonBlockEntity pistonBlockEntity);

    float getFValue();
    void setFValue(float f);

    ModelCommandRenderer.CrumblingOverlayCommand getCrumblingOverlayCommand();
    void setCrumblingOverlayCommand(ModelCommandRenderer.CrumblingOverlayCommand crumblingOverlayCommand);
}
