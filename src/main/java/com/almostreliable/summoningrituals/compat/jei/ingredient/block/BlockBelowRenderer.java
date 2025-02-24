package com.almostreliable.summoningrituals.compat.jei.ingredient.block;

import com.almostreliable.summoningrituals.Constants;
import com.almostreliable.summoningrituals.util.TextUtils;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import mezz.jei.api.ingredients.IIngredientRenderer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.EmptyModelData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockBelowRenderer implements IIngredientRenderer<BlockState> {

    private final Minecraft mc;
    private final BlockRenderDispatcher blockRenderer;
    private final Map<Integer, List<Component>> tooltipCache;
    private final Map<Integer, List<Component>> tooltipCacheAdvanced;
    private final int size;

    public BlockBelowRenderer(int size) {
        mc = Minecraft.getInstance();
        blockRenderer = mc.getBlockRenderer();
        tooltipCache = new HashMap<>();
        tooltipCacheAdvanced = new HashMap<>();
        this.size = size;
    }

    @Override
    public void render(PoseStack stack, BlockState blockBelow) {
        stack.pushPose();
        {
            stack.translate(0.93f * size, 0.77f * size, 0);
            stack.scale(0.625f * size, 0.625f * size, 0.625f * size);
            stack.mulPose(Vector3f.ZN.rotationDegrees(180));
            stack.mulPose(Vector3f.XN.rotationDegrees(30));
            stack.mulPose(Vector3f.YP.rotationDegrees(45));
            RenderSystem.disableDepthTest();
            var bufferSource = mc.renderBuffers().bufferSource();
            blockRenderer.renderSingleBlock(
                blockBelow,
                stack,
                bufferSource,
                LightTexture.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY,
                EmptyModelData.INSTANCE
            );
            bufferSource.endBatch();
        }
        stack.popPose();
    }

    @Override
    public List<Component> getTooltip(BlockState blockBelow, TooltipFlag tooltipFlag) {
        var stack = new ItemStack(blockBelow.getBlock());
        try {
            var stateId = Block.getId(blockBelow);
            var tooltip = getTooltipCache(tooltipFlag).get(stateId);
            if (tooltip != null) return tooltip;

            tooltip = stack.getTooltipLines(mc.player, tooltipFlag);
            tooltip.set(
                0,
                TextUtils.translate(Constants.TOOLTIP, Constants.BLOCK_BELOW, ChatFormatting.GOLD)
                    .append(": ")
                    .append(TextUtils.colorize(tooltip.get(0).getString(), ChatFormatting.WHITE))
            );
            constructTooltip(blockBelow, tooltip);
            getTooltipCache(tooltipFlag).put(stateId, tooltip);
            return tooltip;
        } catch (Exception e) {
            return List.of(new TextComponent("Error rendering tooltip!").append(e.getMessage())
                .withStyle(ChatFormatting.DARK_RED));
        }
    }

    @Override
    public int getWidth() {
        return size;
    }

    @Override
    public int getHeight() {
        return size;
    }

    private Map<Integer, List<Component>> getTooltipCache(TooltipFlag flag) {
        return flag.isAdvanced() ? tooltipCacheAdvanced : tooltipCache;
    }

    private void constructTooltip(BlockState blockBelow, List<Component> tooltip) {
        var defaultState = blockBelow.getBlock().defaultBlockState();
        List<String> modifiedProps = new ArrayList<>();
        for (var property : blockBelow.getProperties()) {
            if (!blockBelow.getValue(property).equals(defaultState.getValue(property))) {
                modifiedProps.add(property.getName() + ": " + blockBelow.getValue(property));
            }
        }
        if (modifiedProps.isEmpty()) return;

        tooltip.add(TextUtils.translate(Constants.TOOLTIP, Constants.PROPERTIES, ChatFormatting.AQUA)
            .append(TextUtils.colorize(":", ChatFormatting.AQUA)));
        for (var prop : modifiedProps) {
            tooltip.add(TextUtils.colorize("» ", ChatFormatting.GRAY)
                .append(TextUtils.colorize(prop, ChatFormatting.WHITE)));
        }
    }
}
