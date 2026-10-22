package tfcmt.tfcitembarrelsandmore.client.screen;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.dries007.tfc.common.recipes.AnvilRecipe;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import tfcmt.tfcitembarrelsandmore.common.blockentities.MechanicalPowerHammerBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.container.MechanicalPowerHammerContainer;
import tfcmt.tfcitembarrelsandmore.common.network.TIBAMNetwork;
import tfcmt.tfcitembarrelsandmore.common.recipes.PowerHammerRecipes;

public class MechanicalPowerHammerScreen
extends AbstractContainerScreen<MechanicalPowerHammerContainer> {
    private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "textures/gui/mechanical_power_hammer.png");
    private static final int LIST_X = 32;
    private static final int LIST_Y = 10;
    private static final int LIST_W = 133;
    private static final int LIST_H = 58;
    private static final int ROW_H = 18;
    private static final int VISIBLE_ROWS = 3;
    private static final int SCROLL_W = 12;
    private static final int SCROLL_X = 153;
    private static final int STATUS_Y = 74;
    private List<AnvilRecipe> recipes = new ArrayList<AnvilRecipe>();
    private int scrollOffset = 0;
    @Nullable
    private ResourceLocation selectedId = null;
    private List<ItemStack> lastInventorySnapshot = new ArrayList<ItemStack>();

    public MechanicalPowerHammerScreen(MechanicalPowerHammerContainer container, Inventory playerInventory, Component title) {
        super((AbstractContainerMenu)container, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.inventoryLabelY = this.imageHeight + 10;
    }

    protected void init() {
        super.init();
        this.selectedId = ((MechanicalPowerHammerBlockEntity)((MechanicalPowerHammerContainer)this.menu).getBlockEntity()).getPrimedRecipeId();
        this.buildRecipeList();
    }

    private void buildRecipeList() {
        if (this.minecraft == null || this.minecraft.level == null || this.minecraft.player == null) {
            this.recipes = new ArrayList<AnvilRecipe>();
            return;
        }
        MechanicalPowerHammerBlockEntity be = (MechanicalPowerHammerBlockEntity)((MechanicalPowerHammerContainer)this.menu).getBlockEntity();
        ItemStack dieStack = be.getHammerInventory().getStackInSlot(2);
        List<AnvilRecipe> allAllowed = PowerHammerRecipes.getAvailableRecipes((Level)this.minecraft.level, be.getAnvilTier(), dieStack, be.isPneumaticHammer());
        HashSet<ResourceLocation> craftable = new HashSet<ResourceLocation>();
        NonNullList playerItems = this.minecraft.player.getInventory().items;
        for (ItemStack playerStack : playerItems) {
            if (playerStack.isEmpty()) continue;
            for (AnvilRecipe recipe : allAllowed) {
                if (!recipe.getInput().test(playerStack)) continue;
                craftable.add(recipe.getId());
            }
        }
        List<AnvilRecipe> filtered = allAllowed.stream().filter(r -> craftable.contains(r.getId())).toList();
        HashSet<ResourceLocation> filteredIds = new HashSet<ResourceLocation>();
        for (AnvilRecipe r2 : filtered) {
            filteredIds.add(r2.getId());
        }
        if (this.selectedId != null && !filteredIds.contains(this.selectedId)) {
            this.selectedId = null;
            TIBAMNetwork.setPrimedRecipe(((MechanicalPowerHammerBlockEntity)((MechanicalPowerHammerContainer)this.menu).getBlockEntity()).getBlockPos(), null);
        }
        if (this.selectedId != null && this.minecraft.player != null) {
            ItemStack held = this.minecraft.player.getMainHandItem();
            AnvilRecipe selected = allAllowed.stream().filter(r -> r.getId().equals((Object)this.selectedId)).findFirst().orElse(null);
            if (selected != null && !held.isEmpty() && !selected.getInput().test(held)) {
                this.selectedId = null;
                TIBAMNetwork.setPrimedRecipe(((MechanicalPowerHammerBlockEntity)((MechanicalPowerHammerContainer)this.menu).getBlockEntity()).getBlockPos(), null);
            }
        }
        this.recipes = new ArrayList<AnvilRecipe>(filtered);
        this.scrollOffset = Math.min(this.scrollOffset, Math.max(0, this.recipes.size() - 3));
        if (this.selectedId == null && this.recipes.size() == 1) {
            this.selectedId = this.recipes.get(0).getId();
            TIBAMNetwork.setPrimedRecipe(((MechanicalPowerHammerBlockEntity)((MechanicalPowerHammerContainer)this.menu).getBlockEntity()).getBlockPos(), this.selectedId);
        }
    }

    protected void containerTick() {
        super.containerTick();
        if (this.minecraft != null && this.minecraft.player != null) {
            boolean changed;
            NonNullList current = this.minecraft.player.getInventory().items;
            boolean bl = changed = current.size() != this.lastInventorySnapshot.size();
            if (!changed) {
                for (int i = 0; i < current.size(); ++i) {
                    ItemStack b;
                    ItemStack a = (ItemStack)current.get(i);
                    if (ItemStack.isSameItemSameComponents((ItemStack)a, (ItemStack)(b = this.lastInventorySnapshot.get(i))) && a.getCount() == b.getCount()) continue;
                    changed = true;
                    break;
                }
            }
            if (changed) {
                this.lastInventorySnapshot = current.stream().map(ItemStack::copy).collect(Collectors.toList());
                this.buildRecipeList();
            }
        }
    }

    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(BACKGROUND, this.leftPos, this.topPos, 0.0f, 0.0f, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);
        this.renderRecipeList(graphics, mouseX, mouseY);
        if (this.recipes.size() > 3) {
            this.renderScrollArrows(graphics, mouseX, mouseY);
        }
        if (((MechanicalPowerHammerContainer)this.menu).getSlot(0).getItem().isEmpty() && this.recipes.isEmpty()) {
            this.renderCenteredInPanel(graphics, (Component)Component.translatable((String)"container.tfc_item_barrels_and_more.hammer.insert_anvil"), 0x888888);
        }
    }

    private void renderRecipeList(GuiGraphics graphics, int mouseX, int mouseY) {
        int idx;
        if (this.minecraft == null || this.minecraft.level == null) {
            return;
        }
        int listScreenX = this.leftPos + 32 + 2;
        int listScreenY = this.topPos + 10 + 2;
        int rowW = 117;
        for (int i = 0; i < 3 && (idx = i + this.scrollOffset) < this.recipes.size(); ++i) {
            AnvilRecipe recipe = this.recipes.get(idx);
            ResourceLocation id = recipe.getId();
            boolean selected = id.equals((Object)this.selectedId);
            boolean hovered = this.isMouseOverRow(mouseX, mouseY, i);
            int rowY = listScreenY + i * 18;
            if (selected) {
                graphics.fill(listScreenX, rowY, listScreenX + rowW, rowY + 18, -2141159232);
            } else if (hovered) {
                graphics.fill(listScreenX, rowY, listScreenX + rowW, rowY + 18, 1085325536);
            }
            ItemStack result = recipe.getResultItem(this.minecraft.level.registryAccess());
            if (!result.isEmpty()) {
                graphics.renderItem(result, listScreenX + 1, rowY + 1);
            }
            Component label = result.isEmpty() ? Component.literal((String)id.getPath()) : result.getHoverName();
            String text = this.font.plainSubstrByWidth(label.getString(), rowW - 22);
            int textColor = selected ? 0xFFFFFF : 0xC8C8C8;
            graphics.drawString(this.font, text, listScreenX + 20, rowY + 5, textColor, false);
        }
    }

    private void renderScrollArrows(GuiGraphics graphics, int mouseX, int mouseY) {
        int ax = this.leftPos + 153 + 2;
        int upY = this.topPos + 10 + 2;
        int dnY = this.topPos + 10 + 58 - 10;
        boolean upHover = this.isOverArrow(mouseX, mouseY, ax, upY);
        boolean dnHover = this.isOverArrow(mouseX, mouseY, ax, dnY);
        graphics.fill(ax, upY, ax + 8, upY + 8, this.scrollOffset > 0 ? (upHover ? -5592406 : -10066330) : -13421773);
        graphics.drawString(this.font, "\u25b2", ax, upY, this.scrollOffset > 0 ? 0xFFFFFF : 0x666666, false);
        int maxScroll = this.recipes.size() - 3;
        graphics.fill(ax, dnY, ax + 8, dnY + 8, this.scrollOffset < maxScroll ? (dnHover ? -5592406 : -10066330) : -13421773);
        graphics.drawString(this.font, "\u25bc", ax, dnY, this.scrollOffset < maxScroll ? 0xFFFFFF : 0x666666, false);
    }

    private void renderCenteredInPanel(GuiGraphics graphics, Component msg, int color) {
        int cx = this.leftPos + 32 + 66;
        int cy = this.topPos + 10 + 29 - 4;
        graphics.drawCenteredString(this.font, msg, cx, cy, color);
    }

    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
    }

    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        AnvilRecipe recipe;
        ItemStack result;
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
        this.renderEquipmentSlotTooltip(graphics, mouseX, mouseY, 7, 8, "container.tfc_item_barrels_and_more.hammer.anvil", 0);
        this.renderEquipmentSlotTooltip(graphics, mouseX, mouseY, 7, 28, "container.tfc_item_barrels_and_more.hammer.hammer", 1);
        this.renderEquipmentSlotTooltip(graphics, mouseX, mouseY, 7, 48, "container.tfc_item_barrels_and_more.hammer.die", 2);
        int hoveredRow = this.getHoveredRow(mouseX, mouseY);
        if (hoveredRow >= 0 && hoveredRow + this.scrollOffset < this.recipes.size() && this.minecraft != null && this.minecraft.level != null && !(result = (recipe = this.recipes.get(hoveredRow + this.scrollOffset)).getResultItem(this.minecraft.level.registryAccess())).isEmpty()) {
            ArrayList<Object> lines = new ArrayList<Object>();
            lines.add(result.getHoverName());
            lines.add(Component.translatable((String)"container.tfc_item_barrels_and_more.hammer.min_tier", (Object[])new Object[]{recipe.getMinTier()}));
            graphics.renderComponentTooltip(this.font, lines, mouseX, mouseY);
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int idx;
        int row = this.getHoveredRow((int)mouseX, (int)mouseY);
        if (row >= 0 && (idx = row + this.scrollOffset) < this.recipes.size()) {
            ResourceLocation id = this.recipes.get(idx).getId();
            if (id.equals((Object)this.selectedId)) {
                this.selectedId = null;
                TIBAMNetwork.setPrimedRecipe(((MechanicalPowerHammerBlockEntity)((MechanicalPowerHammerContainer)this.menu).getBlockEntity()).getBlockPos(), null);
            } else {
                this.selectedId = id;
                TIBAMNetwork.setPrimedRecipe(((MechanicalPowerHammerBlockEntity)((MechanicalPowerHammerContainer)this.menu).getBlockEntity()).getBlockPos(), id);
            }
            return true;
        }
        if (this.recipes.size() > 3) {
            int ax = this.leftPos + 153 + 2;
            int upY = this.topPos + 10 + 2;
            int dnY = this.topPos + 10 + 58 - 10;
            int max = this.recipes.size() - 3;
            if (mouseX >= (double)ax && mouseX < (double)(ax + 8)) {
                if (mouseY >= (double)upY && mouseY < (double)(upY + 8) && this.scrollOffset > 0) {
                    --this.scrollOffset;
                    return true;
                }
                if (mouseY >= (double)dnY && mouseY < (double)(dnY + 8) && this.scrollOffset < max) {
                    ++this.scrollOffset;
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (this.isMouseOverList((int)mouseX, (int)mouseY)) {
            int max = Math.max(0, this.recipes.size() - 3);
            this.scrollOffset = (int)Math.max(0.0, Math.min((double)max, (double)this.scrollOffset - delta));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    private void renderEquipmentSlotTooltip(GuiGraphics graphics, int mouseX, int mouseY, int slotX, int slotY, String labelKey, int slotIndex) {
        int sx = this.leftPos + slotX;
        int sy = this.topPos + slotY;
        if (mouseX >= sx && mouseX < sx + 18 && mouseY >= sy && mouseY < sy + 18 && ((MechanicalPowerHammerContainer)this.menu).getSlot(slotIndex).getItem().isEmpty()) {
            graphics.renderComponentTooltip(this.font, List.of(Component.translatable((String)labelKey)), mouseX, mouseY);
        }
    }

    private boolean isMouseOverList(int mx, int my) {
        return mx >= this.leftPos + 32 && mx < this.leftPos + 32 + 133 - 12 && my >= this.topPos + 10 && my < this.topPos + 10 + 58;
    }

    private boolean isMouseOverRow(int mx, int my, int rowIndex) {
        int rowY = this.topPos + 10 + 2 + rowIndex * 18;
        int rowW = 117;
        return mx >= this.leftPos + 32 + 2 && mx < this.leftPos + 32 + 2 + rowW && my >= rowY && my < rowY + 18;
    }

    private boolean isOverArrow(int mx, int my, int ax, int ay) {
        return mx >= ax && mx < ax + 8 && my >= ay && my < ay + 8;
    }

    private int getHoveredRow(int mx, int my) {
        for (int i = 0; i < 3; ++i) {
            if (!this.isMouseOverRow(mx, my, i)) continue;
            return i;
        }
        return -1;
    }
}

