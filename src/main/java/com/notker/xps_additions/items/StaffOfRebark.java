package com.notker.xps_additions.items;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.notker.xps_additions.XpsAdditions;
import com.notker.xps_additions.mixin.AxeItemAccessor;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.PillarBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;
import java.util.Optional;


public class StaffOfRebark extends Item {
    /** stripped block -> the block(s) it can be turned back into. Built lazily from the axe stripping map. */
    private static volatile Multimap<Block, Block> STRIPPED_BLOCKS = null;

    public StaffOfRebark(Settings settings) {
        super(settings);
    }

    /**
     * Inverse of {@code AxeItem.STRIPPED_BLOCKS}. Built on first use (after every mod had the chance to register
     * its strippable blocks) and cached for the rest of the game session.
     */
    public static Multimap<Block, Block> getStrippedBlocks() {
        Multimap<Block, Block> map = STRIPPED_BLOCKS;
        if (map == null) {
            ImmutableMultimap.Builder<Block, Block> builder = ImmutableMultimap.builder();
            AxeItemAccessor.getStrip().forEach((block, strippedBlock) -> builder.put(strippedBlock, block));
            map = builder.build();
            STRIPPED_BLOCKS = map;

            map.forEach((stripped, original) -> XpsAdditions.LOGGER.debug("Staff of Rebark: {} -> {}", Registries.BLOCK.getId(stripped), Registries.BLOCK.getId(original)));
            XpsAdditions.LOGGER.info("Staff of Rebark knows {} stripped block variants", map.size());
        }
        return map;
    }


    private Optional<BlockState> getUnStrippedState(BlockState state) {
        List<BlockState> list = getStrippedBlocks().get(state.getBlock()).stream()
                .map(block -> {
                    BlockState unStripped = block.getDefaultState();
                    // keep the log orientation; other mods may register strippables without an axis
                    if (state.contains(PillarBlock.AXIS) && unStripped.contains(PillarBlock.AXIS)) {
                        unStripped = unStripped.with(PillarBlock.AXIS, state.get(PillarBlock.AXIS));
                    }
                    return unStripped;
                })
                .toList();
        if (list.isEmpty()) {
            return Optional.empty();
        }
        // Return a random variant when several blocks strip to the same block
        return Optional.of(list.get((int) (list.size() * Math.random())));
    }



    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos blockPos = context.getBlockPos();
        PlayerEntity playerEntity = context.getPlayer();
        BlockState blockState = world.getBlockState(blockPos);
        Optional<BlockState> unStrippedState = this.getUnStrippedState(blockState);

        if (unStrippedState.isPresent()) {
            world.playSound(playerEntity, blockPos, SoundEvents.ITEM_DYE_USE, SoundCategory.BLOCKS, 1.0F, 1.0F);

            if (playerEntity != null && !playerEntity.isCreative()) {
                playerEntity.getStackInHand(context.getHand()).damage(5, playerEntity, p -> p.sendToolBreakStatus(context.getHand()));
            }

            if (!world.isClient()) {
                world.setBlockState(blockPos, unStrippedState.get(), 11);
            }

            return ActionResult.success(world.isClient());
        }

        return super.useOnBlock(context);
    }
}
