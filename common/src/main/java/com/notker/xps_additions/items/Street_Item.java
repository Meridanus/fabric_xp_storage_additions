package com.notker.xps_additions.items;

import com.notker.xps_additions.TooltipHelper;
import com.notker.xps_additions.XpsAdditions;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;

import java.util.List;

public class Street_Item extends BlockItem {

    public Street_Item(Block block, Settings settings) {
        super(block, settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType type) {
        super.appendTooltip(stack, context, tooltip, type);

        tooltip.add(Text.translatable("item.tooltip.street", TooltipHelper.chanceToString(XpsAdditions.RUNNING_SPEED)));

    }
}
