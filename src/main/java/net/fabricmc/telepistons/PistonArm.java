package net.fabricmc.telepistons;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FacingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

public class PistonArm extends FacingBlock {

	public PistonArm(Settings settings) {
		super(settings);
		setDefaultState(this.stateManager.getDefaultState().with(Properties.FACING, Direction.NORTH));
	}
	
	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> stateManager) {
		stateManager.add(Properties.FACING);
	}
	
	@Override
	 public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, ShapeContext context) {
		Direction dir = state.get(FACING);
		switch(dir) {
		case NORTH:
			return VoxelShapes.cuboid(0.3f, 0.3f, 0.625f, 0.7f, 0.7f, 1.25f);
		case EAST:
			return VoxelShapes.cuboid(0.3f, 0.3f, 0.3f, 0.7f, 0.7f, 0.7f);
		case SOUTH:
			return VoxelShapes.cuboid(0.3f, 0.3f, -0.25f, 0.7f, 0.7f, 0.375f);
		case WEST:
			return VoxelShapes.cuboid(0.3f, 0.3f, 0.3f, 0.7f, 0.7f, 0.7f);
		case UP:
			return VoxelShapes.cuboid(0.3f, 0.3f, 0.3f, 0.7f, 0.7f, 0.7f);
		case DOWN:
			return VoxelShapes.cuboid(0.3f, 0.3f, 0.3f, 0.7f, 0.7f, 0.7f);
		default:
			return VoxelShapes.fullCube();
		}
	 }

}
