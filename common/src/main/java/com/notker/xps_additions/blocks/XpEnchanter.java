package com.notker.xps_additions.blocks;

import com.mojang.serialization.MapCodec;
import com.notker.xps_additions.entity.XpEnchanterEntity;
import com.notker.xps_additions.regestry.AdditionBlocks;
import dev.architectury.registry.menu.MenuRegistry;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.MapColor;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * An enchanting table that pulls its XP out of an adjacent XP Obelisk. Triggered by a redstone
 * pulse (rising edge, like a dispenser) or by the button in its GUI.
 */
public class XpEnchanter extends BlockWithEntity {

    public static final MapCodec<XpEnchanter> CODEC = createCodec(XpEnchanter::new);

    private static final VoxelShape CENTER_SHAPE = VoxelShapes.union(
            Block.createCuboidShape(1D, 0D, 1D, 15D, 1D, 15D),   // bottom
            Block.createCuboidShape(2D, 1D, 2D, 14D, 12D, 14D),  // middle
            Block.createCuboidShape(1.5D, 1D, 1.5D, 3.5D, 12.5D, 3.5D), // Pillar 1
            Block.createCuboidShape(12.5D, 1D, 1.5D, 14.5, 12.5D, 3.5D), // Pillar 2
            Block.createCuboidShape(1.5D, 1D, 12.5D, 3.5D, 12.5D, 14.5D),  // Pillar 3
            Block.createCuboidShape(12.5D, 1D, 12.5D, 14.5D, 12.5D, 14.5D)  // Pillar 4
    );

    private static final VoxelShape NORTH_OUT = Block.createCuboidShape(6D, 6D, -1D, 10D, 10D, 3D);  // Output
    private static final VoxelShape EAST_OUT = Block.createCuboidShape(13D, 6D, 6D, 17D, 10D, 10D);  // Output
    private static final VoxelShape SOUTH_OUT = Block.createCuboidShape(6D, 6D, 13D, 10D, 10D, 17D); // Output
    private static final VoxelShape WEST_OUT = Block.createCuboidShape(-1D, 6D, 6D, 3D, 10D, 10D);   // Output
    private static final VoxelShape NORTH_IN = Block.createCuboidShape(5D, 3D, 0D, 11D, 9D, 3D);     // Input
    private static final VoxelShape WEST_IN = Block.createCuboidShape(0D, 3D, 5D, 3D, 9D, 11D);      // Input
    private static final VoxelShape SOUTH_IN = Block.createCuboidShape(5D, 3D, 13D, 11D, 9D, 16D);   // Input
    private static final VoxelShape EAST_IN = Block.createCuboidShape(13D, 3D, 5D, 16D, 9D, 11D);    // Input

    private static final VoxelShape SHAPE_NORTH = VoxelShapes.union(CENTER_SHAPE, NORTH_OUT, EAST_IN, WEST_IN);
    private static final VoxelShape SHAPE_EAST = VoxelShapes.union(CENTER_SHAPE, NORTH_IN, EAST_OUT, SOUTH_IN);
    private static final VoxelShape SHAPE_SOUTH = VoxelShapes.union(CENTER_SHAPE, EAST_IN, SOUTH_OUT, WEST_IN);
    private static final VoxelShape SHAPE_WEST = VoxelShapes.union(CENTER_SHAPE, NORTH_IN, SOUTH_IN, WEST_OUT);


    /** Ticks between the redstone pulse and the enchant, same delay a dispenser uses. */
    private static final int TRIGGER_DELAY = 4;

    /** The block settings, kept here so {@link #CODEC} can rebuild the block from settings alone. */
    public static AbstractBlock.Settings settings() {
        return AbstractBlock.Settings
                .create()
                .mapColor(MapColor.PURPLE)
                .sounds(BlockSoundGroup.METAL)
                .strength(5f, 6f)
                .requiresTool()
                .nonOpaque()
                .luminance(state -> 7); // the vanilla enchanting table glows just as faintly
    }

    public XpEnchanter(AbstractBlock.Settings settings) {
        super(settings);
        setDefaultState(getStateManager()
                .getDefaultState()
                .with(Properties.HORIZONTAL_FACING, Direction.NORTH)
                .with(Properties.TRIGGERED, false)
        );
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> stateManager) {
        stateManager.add(Properties.HORIZONTAL_FACING);
        stateManager.add(Properties.TRIGGERED);
    }

    @Override
    protected BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, ShapeContext ctx) {
        return switch (state.get(Properties.HORIZONTAL_FACING)) {
            case SOUTH -> SHAPE_SOUTH;
            case EAST -> SHAPE_EAST;
            case WEST -> SHAPE_WEST;
            default -> SHAPE_NORTH;
        };
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!world.isClient && player instanceof ServerPlayerEntity serverPlayer) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof XpEnchanterEntity enchanter) {
                MenuRegistry.openExtendedMenu(serverPlayer, enchanter);
            }
        }
        return ActionResult.SUCCESS;
    }


    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {

        // The player can be null when the block is placed by a dispenser / other automation
        PlayerEntity player = ctx.getPlayer();
        boolean sneaking = player != null && player.isSneaking();

        // Sneaking: place the block facing the same direction as the player, otherwise facing the player
        Direction facing = sneaking ? ctx.getHorizontalPlayerFacing().getOpposite() : ctx.getHorizontalPlayerFacing();

        return this.getDefaultState().with(Properties.HORIZONTAL_FACING, facing);
    }

    @Override
    protected BlockState rotate(BlockState state, BlockRotation rotation) {
        return state.with(Properties.HORIZONTAL_FACING, rotation.rotate(state.get(Properties.HORIZONTAL_FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(Properties.HORIZONTAL_FACING)));
    }

    /**
     * Rising edge only: TRIGGERED remembers that the current signal was already used, so holding
     * a lever on does not enchant over and over.
     */
    @Override
    protected void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
        if (world.isClient) {
            return;
        }

        boolean powered = world.isReceivingRedstonePower(pos);
        boolean triggered = state.get(Properties.TRIGGERED);

        if (powered && !triggered) {
            world.scheduleBlockTick(pos, this, TRIGGER_DELAY);
            world.setBlockState(pos, state.with(Properties.TRIGGERED, true), Block.NOTIFY_LISTENERS | Block.NO_REDRAW);
        } else if (!powered && triggered) {
            world.setBlockState(pos, state.with(Properties.TRIGGERED, false), Block.NOTIFY_LISTENERS | Block.NO_REDRAW);
        }
    }

    @Override
    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (world.getBlockEntity(pos) instanceof XpEnchanterEntity enchanter) {
            enchanter.tryEnchant();
        }
    }

    @Override
    protected boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    @Override
    protected int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        if (world.getBlockEntity(pos) instanceof XpEnchanterEntity blockEntity) {
            boolean ready = blockEntity.getXpEnchantingStatus();
            return ready ? 15 : 0;
        }
        return 0;
        //return ScreenHandler.calculateComparatorOutput(world.getBlockEntity(pos));
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new XpEnchanterEntity(pos, state);
    }

    @Override
    protected void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity entity = world.getBlockEntity(pos);
            if (entity instanceof XpEnchanterEntity) {
                ItemScatterer.spawn(world, pos, (XpEnchanterEntity) entity);
                world.updateComparators(pos, this);
            }
            super.onStateReplaced(state, world, pos, newState, moved);
        }
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        // The inventory and the obelisk lookup only exist server side
        return world.isClient ? null : validateTicker(type, AdditionBlocks.XP_ENCHANTER_ENTITY.get(), XpEnchanterEntity::tick);
    }
}
