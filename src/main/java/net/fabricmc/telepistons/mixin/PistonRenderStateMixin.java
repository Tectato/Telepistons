package net.fabricmc.telepistons.mixin;

import net.fabricmc.telepistons.access.PistonRenderStateAccess;
import net.minecraft.block.entity.PistonBlockEntity;
import net.minecraft.client.render.block.entity.state.PistonBlockEntityRenderState;
import net.minecraft.client.render.command.ModelCommandRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(PistonBlockEntityRenderState.class)
public class PistonRenderStateMixin implements PistonRenderStateAccess {
    @Unique
    public PistonBlockEntity pistonBlockEntity;

    @Unique
    public float f;

    @Unique
    public ModelCommandRenderer.CrumblingOverlayCommand crumblingOverlayCommand;

    @Unique
    @Override
    public PistonBlockEntity getPistonBlockEntity() {
        return pistonBlockEntity;
    }

    @Unique
    @Override
    public void setPistonBlockEntity(PistonBlockEntity pistonBlockEntity) {
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
    public ModelCommandRenderer.CrumblingOverlayCommand getCrumblingOverlayCommand() {
        return this.crumblingOverlayCommand;
    }

    @Override
    public void setCrumblingOverlayCommand(ModelCommandRenderer.CrumblingOverlayCommand crumblingOverlayCommand) {
        this.crumblingOverlayCommand = crumblingOverlayCommand;
    }
}
