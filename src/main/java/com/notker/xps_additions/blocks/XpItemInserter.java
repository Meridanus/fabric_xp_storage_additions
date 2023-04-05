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

import java.util.Objects;

public class XpItemInserter extends BlockWithEntity implements Waterloggable {

    public XpItemInserter() {
        super(FabricBlockSettings
                .of(Material.METAL)
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
        // When player is sneaking place the block in Facing direction
        if (Objects.requireNonNull(ctx.getPlayer()).isSneaking()) {
            return this.getDefaultState().with(Properties.HORIZONTAL_FACING, ctx.getPlayerFacing()).with(Properties.WATERLOGGED, bl);
        }
        // If not place it the opposite way
        return this.getDefaultState().with(Properties.HORIZONTAL_FACING, ctx.getPlayerFacing().getOpposite()).with(Properties.WATERLOGGED, bl);
    }
    @SuppressWarnings("deprecation")
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (state.get(Properties.WATERLOGGED)) {
            world.createAndScheduleFluidTick(pos, Fluids.WATER,Fluids.WATER.getTickRate(world));
        }

        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    @SuppressWarnings("deprecation")
    public FluidState getFluidState(BlockState state) {
        return state.get(Properties.WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
    }

    public boolean tryFillWithFluid(WorldAccess world, BlockPos pos, BlockState state, FluidState fluidState) {
        if (!(Boolean)state.get(Properties.WATERLOGGED) && fluidState.getFluid() == Fluids.WATER) {
            BlockState blockState = state.with(Properties.WATERLOGGED, true);

            world.setBlockState(pos, blockState, 3);
            world.createAndScheduleFluidTick(pos, fluidState.getFluid(), fluidState.getFluid().getTickRate(world));
            return true;
        } else {
            return false;
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, ShapeContext ctx) {

        VoxelShape bottom = Block.createCuboidShape(1D, 0D, 1D, 15D, 1D, 15D);
        VoxelShape middle = Block.createCuboidShape(3D, 1D, 3D, 13D, 13D, 13D);
        VoxelShape top = Block.createCuboidShape(5D, 13D, 5D, 11D, 16D, 11D);

        VoxelShape north_out = Block.createCuboidShape(6D, 6D, -1D, 10D, 10D, 3D); // Output
        VoxelShape east_out = Block.createCuboidShape(13D, 6D, 6D, 17D, 10D, 10D); // Output
        VoxelShape south_out = Block.createCuboidShape(6D, 6D, 13D, 10D, 10D, 17D); // Output
        VoxelShape west_out = Block.createCuboidShape(-1D, 6D, 6D, 3D, 10D, 10D); // Output

        VoxelShape north_in = Block.createCuboidShape(5D, 3D, 0D, 11D, 9D, 3D); // Input
        VoxelShape west_in = Block.createCuboidShape(0D, 3D, 5D, 3D, 9D, 11D); // Input
        VoxelShape south_in = Block.createCuboidShape(5D, 3D, 13D, 11D, 9D, 16D); // Input
        VoxelShape east_in = Block.createCuboidShape(13D, 3D, 5D, 16D, 9D, 11D); // Input

        VoxelShape centerPart = VoxelShapes.union(bottom, middle, top);

        Direction dir = state.get(Properties.HORIZONTAL_FACING);

        return switch (dir) {
            case NORTH -> VoxelShapes.union(centerPart, north_out, east_in, south_in, west_in);
            case SOUTH -> VoxelShapes.union(centerPart, north_in, east_in, south_out, west_in);
            case EAST -> VoxelShapes.union(centerPart, north_in, east_out, south_in, west_in);
            case WEST -> VoxelShapes.union(centerPart, north_in, east_in, south_in, west_out);
            default -> VoxelShapes.union(centerPart);
        };

    }

    @Override
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
        return checkType(type, AdditionBlocks.XP_ITEM_INSERTER_ENTITY, XpItemInserterEntity::tick);
        //return BlockEntityProvider.super.getTicker(world, state, type);
    }

}
