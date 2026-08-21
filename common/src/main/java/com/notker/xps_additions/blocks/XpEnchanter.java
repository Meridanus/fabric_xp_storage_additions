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
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * An enchanting table that pulls its XP out of an adjacent XP Obelisk. Triggered by a redstone
 * pulse (rising edge, like a dispenser) or by the button in its GUI.
 */
public class XpEnchanter extends BlockWithEntity {

    public static final MapCodec<XpEnchanter> CODEC = createCodec(XpEnchanter::new);

    // Same height as the vanilla enchanting table
    private static final VoxelShape SHAPE = Block.createCuboidShape(0D, 0D, 0D, 16D, 12D, 16D);

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
                .with(Properties.TRIGGERED, false)
        );
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> stateManager) {
        stateManager.add(Properties.TRIGGERED);
    }

    @Override
    protected BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, ShapeContext ctx) {
        return SHAPE;
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
        return ScreenHandler.calculateComparatorOutput(world.getBlockEntity(pos));
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
