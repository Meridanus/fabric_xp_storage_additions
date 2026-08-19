package com.notker.xps_additions.blocks;

import com.notker.xps_additions.entity.XpItemInserterEntity;
import com.notker.xps_additions.regestry.AdditionBlocks;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

public class XpItemInserter extends BlockWithEntity implements Waterloggable {

    // Shapes are immutable, so build them once instead of on every outline/collision query.
    private static final VoxelShape CENTER_SHAPE = VoxelShapes.union(
            Block.createCuboidShape(1D, 0D, 1D, 15D, 1D, 15D),   // bottom
            Block.createCuboidShape(3D, 1D, 3D, 13D, 13D, 13D),  // middle
            Block.createCuboidShape(5D, 13D, 5D, 11D, 16D, 11D)  // top
    );
    private static final VoxelShape NORTH_OUT = Block.createCuboidShape(6D, 6D, -1D, 10D, 10D, 3D);  // Output
    private static final VoxelShape EAST_OUT = Block.createCuboidShape(13D, 6D, 6D, 17D, 10D, 10D);  // Output
    private static final VoxelShape SOUTH_OUT = Block.createCuboidShape(6D, 6D, 13D, 10D, 10D, 17D); // Output
    private static final VoxelShape WEST_OUT = Block.createCuboidShape(-1D, 6D, 6D, 3D, 10D, 10D);   // Output
    private static final VoxelShape NORTH_IN = Block.createCuboidShape(5D, 3D, 0D, 11D, 9D, 3D);     // Input
    private static final VoxelShape WEST_IN = Block.createCuboidShape(0D, 3D, 5D, 3D, 9D, 11D);      // Input
    private static final VoxelShape SOUTH_IN = Block.createCuboidShape(5D, 3D, 13D, 11D, 9D, 16D);   // Input
    private static final VoxelShape EAST_IN = Block.createCuboidShape(13D, 3D, 5D, 16D, 9D, 11D);    // Input

    private static final VoxelShape SHAPE_NORTH = VoxelShapes.union(CENTER_SHAPE, NORTH_OUT, EAST_IN, SOUTH_IN, WEST_IN);
    private static final VoxelShape SHAPE_SOUTH = VoxelShapes.union(CENTER_SHAPE, NORTH_IN, EAST_IN, SOUTH_OUT, WEST_IN);
    private static final VoxelShape SHAPE_EAST = VoxelShapes.union(CENTER_SHAPE, NORTH_IN, EAST_OUT, SOUTH_IN, WEST_IN);
    private static final VoxelShape SHAPE_WEST = VoxelShapes.union(CENTER_SHAPE, NORTH_IN, EAST_IN, SOUTH_IN, WEST_OUT);

    public XpItemInserter() {
        super(FabricBlockSettings
                .create()
                .mapColor(MapColor.IRON_GRAY)
                .sounds(BlockSoundGroup.METAL)
                .strength(6f, 6f)
                .requiresTool()
                .nonOpaque()
        );
        setDefaultState(getStateManager()
                .getDefaultState()
                .with(Properties.HORIZONTAL_FACING, Direction.NORTH)
                .with(Properties.WATERLOGGED, false)
        );
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }


    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!world.isClient) {
            NamedScreenHandlerFactory screenHandlerFactory = state.createScreenHandlerFactory(world, pos);
            if (screenHandlerFactory != null) {
                player.openHandledScreen(screenHandlerFactory);
            }
        }
        return ActionResult.SUCCESS;
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        FluidState fluidState = ctx.getWorld().getFluidState(ctx.getBlockPos());
        boolean bl = fluidState.getFluid() == Fluids.WATER;

        // The player can be null when the block is placed by a dispenser / other automation
        PlayerEntity player = ctx.getPlayer();
        boolean sneaking = player != null && player.isSneaking();

        // Sneaking: place the block facing the same direction as the player, otherwise facing the player
        Direction facing = sneaking ? ctx.getHorizontalPlayerFacing() : ctx.getHorizontalPlayerFacing().getOpposite();

        return this.getDefaultState().with(Properties.HORIZONTAL_FACING, facing).with(Properties.WATERLOGGED, bl);
    }
    @SuppressWarnings("deprecation")
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (state.get(Properties.WATERLOGGED)) {
            world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }

        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    @SuppressWarnings("deprecation")
    public FluidState getFluidState(BlockState state) {
        return state.get(Properties.WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, ShapeContext ctx) {
        return switch (state.get(Properties.HORIZONTAL_FACING)) {
            case SOUTH -> SHAPE_SOUTH;
            case EAST -> SHAPE_EAST;
            case WEST -> SHAPE_WEST;
            default -> SHAPE_NORTH;
        };
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState rotate(BlockState state, BlockRotation rotation) {
        return state.with(Properties.HORIZONTAL_FACING, rotation.rotate(state.get(Properties.HORIZONTAL_FACING)));
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(Properties.HORIZONTAL_FACING)));
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new XpItemInserterEntity(pos, state);
    }
    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> stateManager) {
        stateManager.add(Properties.HORIZONTAL_FACING);
        stateManager.add(Properties.WATERLOGGED);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity entity = world.getBlockEntity(pos);
            if (entity instanceof XpItemInserterEntity) {
                ItemScatterer.spawn(world, pos, (XpItemInserterEntity)entity);
                world.updateComparators(pos, this);
            }
            super.onStateReplaced(state, world, pos, newState, moved);
        }
    }



    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        // The inventory only exists server side, so there is nothing to do on the client
        return world.isClient ? null : checkType(type, AdditionBlocks.XP_ITEM_INSERTER_ENTITY, XpItemInserterEntity::tick);
    }

}
