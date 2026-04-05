package net.fabricmc.telepistons.access;

import net.minecraft.client.renderer.block.MovingBlockRenderState;
import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;

public interface PistonRenderStateAccess {
    PistonMovingBlockEntity getPistonBlockEntity();
    void setPistonBlockEntity(PistonMovingBlockEntity pistonBlockEntity);

    MovingBlockRenderState getArm();
    void setArm(MovingBlockRenderState value);
}
