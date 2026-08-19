package com.notker.xps_additions.blocks;

import com.notker.xps_additions.XpsAdditions;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;

import java.util.Map;
import java.util.Objects;

@SuppressWarnings("deprecation")
public class Street extends Block {

    public static final BooleanProperty NORTH;
    public static final BooleanProperty EAST;
    public static final BooleanProperty SOUTH;
    public static final BooleanProperty WEST;
    protected static final Map<Direction, BooleanProperty> FACING_PROPERTIES;

    public Street() {
        super(FabricBlockSettings
                .create()
                .mapColor(MapColor.GREEN)
                .pistonBehavior(PistonBehavior.DESTROY) // former Material.GOURD behaviour
                .strength(1F, 6F)
                .velocityMultiplier(XpsAdditions.RUNNING_SPEED));

        this.setDefaultState(getDefaultState()
                .with(NORTH, false)
                .with(EAST, false)
                .with(SOUTH, false)
                .with(WEST, false));
    }

    // Shapes are immutable: build them once instead of on every outline/collision query
    private static final VoxelShape OUTLINE_SHAPE = VoxelShapes.fullCube();
    private static final VoxelShape COLLISION_SHAPE = Block.createCuboidShape(0D, 0D, 0D, 16D, 14D, 16D);

    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return OUTLINE_SHAPE;
    }

    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return COLLISION_SHAPE;
    }

    public boolean canConnect(BlockState state) {
        Block block = state.getBlock();
        return  block instanceof Street;
    }


    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockView blockView = ctx.getWorld();
        BlockPos blockPos = ctx.getBlockPos();
        BlockState blockState_north = blockView.getBlockState(blockPos.north());
        BlockState blockState_east = blockView.getBlockState(blockPos.east());
        BlockState blockState_south = blockView.getBlockState(blockPos.south());
        BlockState blockState_west = blockView.getBlockState(blockPos.west());
        return Objects.requireNonNull(super.getPlacementState(ctx))
                .with(NORTH, this.canConnect(blockState_north))
                .with(EAST, this.canConnect(blockState_east))
                .with(SOUTH, this.canConnect(blockState_south))
                .with(WEST, this.canConnect(blockState_west));
    }

    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        return direction.getAxis().getType() == Direction.Type.HORIZONTAL ? state.with(FACING_PROPERTIES.get(direction), this.canConnect(neighborState)) : super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
        return false;
    }

    public BlockState rotate(BlockState state, BlockRotation rotation) {
        return switch (rotation) {
            case CLOCKWISE_180 -> (((state.with(NORTH, state.get(SOUTH))).with(EAST, state.get(WEST))).with(SOUTH, state.get(NORTH))).with(WEST, state.get(EAST));
            case COUNTERCLOCKWISE_90 -> (((state.with(NORTH, state.get(EAST))).with(EAST, state.get(SOUTH))).with(SOUTH, state.get(WEST))).with(WEST, state.get(NORTH));
            case CLOCKWISE_90 -> (((state.with(NORTH, state.get(WEST))).with(EAST, state.get(NORTH))).with(SOUTH, state.get(EAST))).with(WEST, state.get(SOUTH));
            default -> state;
        };
    }

    public BlockState mirror(BlockState state, BlockMirror mirror) {
        return switch (mirror) {
            case LEFT_RIGHT -> (state.with(NORTH, state.get(SOUTH))).with(SOUTH, state.get(NORTH));
            case FRONT_BACK -> (state.with(EAST, state.get(WEST))).with(WEST, state.get(EAST));
            default -> super.mirror(state, mirror);
        };
    }


    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> stateManager) {
        stateManager.add(Properties.NORTH, EAST, WEST, SOUTH);
    }

    static {
        NORTH = ConnectingBlock.NORTH;
        EAST = ConnectingBlock.EAST;
        SOUTH = ConnectingBlock.SOUTH;
        WEST = ConnectingBlock.WEST;
        FACING_PROPERTIES = ConnectingBlock.FACING_PROPERTIES.entrySet().stream().filter((entry) -> (entry.getKey()).getAxis().isHorizontal()).collect(Util.toMap());
    }


}
