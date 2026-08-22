package com.notker.xps_additions.client;

import com.notker.xps_additions.entity.XpEnchanterEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.world.World;

/**
 * Shows the item waiting in the input slot hovering in the arms on top of the XP Enchanter, so you
 * can see what the block is about to enchant without opening its GUI.
 *
 * <p>The stack comes from the block entity, which only sends the input slot to clients - see
 * {@code XpEnchanterEntity#syncIfInputChanged()}.
 */
public class XpEnchanterRenderer implements BlockEntityRenderer<XpEnchanterEntity> {

    /**
     * Clear of the three arms on top of the model, whose tips reach 16.84 px. The GROUND transform
     * the item is drawn with lifts it another 3 px and halves it, so with {@link #SCALE} on top the
     * item ends up spanning roughly 19 to 25 px - about a pixel and a half of daylight under it at
     * the bottom of the bob. Low enough that the arms still read as holding it, high enough that
     * nothing dips through them.
     */
    private static final float HOVER_HEIGHT = 1.25f;

    /** Roughly the width of the cradle the arms form, a full sized item would swallow them. */
    private static final float SCALE = 0.75f;

    /** 2 degrees a tick is a full turn every 180 ticks, nine seconds. */
    private static final float SPIN_DEGREES_PER_TICK = 2f;

    /** About one pixel up and one down. 3.6 degrees a tick is one bob every 100 ticks, five seconds. */
    private static final float BOB_HEIGHT = 0.06f;
    private static final float BOB_DEGREES_PER_TICK = 3.6f;

    /**
     * World time keeps growing over the life of a world and a float loses its fractional part
     * somewhere around 16 million, which would first make the animation stutter and then stop it
     * dead, so the clock is wrapped. The wrap is a whole number of turns (43200 degrees, 120 turns)
     * and a whole number of bobs (77760 degrees, 216 bobs), otherwise the item would visibly jump
     * every time the clock came around - pick a different speed and this has to be rechecked.
     */
    private static final long CLOCK_WRAP = 21600L;

    private final ItemRenderer itemRenderer;

    public XpEnchanterRenderer(BlockEntityRendererFactory.Context ctx) {
        this.itemRenderer = ctx.getItemRenderer();
    }

    @Override
    public void render(XpEnchanterEntity entity, float tickDelta, MatrixStack matrices,
                       VertexConsumerProvider vertexConsumers, int light, int overlay) {
        ItemStack stack = entity.getRenderStack();
        if (stack.isEmpty()) {
            return;
        }

        World world = entity.getWorld();
        if (world == null) {
            return;
        }

        BlockPos pos = entity.getPos();
        // Without the offset, a row of enchanters would spin and bob in lockstep like a chorus line.
        // It is in ticks, so it shifts both animations at once and survives the clock wrap.
        float phase = pos.hashCode() & 0xFF;
        float time = (world.getTime() % CLOCK_WRAP) + tickDelta + phase;

        float bob = MathHelper.sin(time * BOB_DEGREES_PER_TICK * MathHelper.RADIANS_PER_DEGREE) * BOB_HEIGHT;

        matrices.push();
        matrices.translate(0.5f, HOVER_HEIGHT + bob, 0.5f);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(time * SPIN_DEGREES_PER_TICK));
        matrices.scale(SCALE, SCALE, SCALE);

        // The light of the block itself would be the light inside the enchanter, which is dark.
        // The item hangs in the open air above it, so it is lit like that spot.
        int lightAbove = WorldRenderer.getLightmapCoordinates(world, pos.up());
        itemRenderer.renderItem(stack, ModelTransformationMode.GROUND, lightAbove,
                OverlayTexture.DEFAULT_UV, matrices, vertexConsumers, world, (int) pos.asLong());

        matrices.pop();
    }
}
