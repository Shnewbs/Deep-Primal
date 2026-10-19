package com.shnewbs.deepprimal.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** These controls budget optional work; they never change gameplay simulation. */
public final class HorizonSettingsScreen extends Screen {
    private final Screen parent;
    private EditBox fps, memory;
    private boolean adaptive;
    private Button apply;
    public HorizonSettingsScreen(Screen parent) { super(Component.literal("Deep Primal performance")); this.parent=parent; }
    @Override protected void init() {
        int x=width/2-155, y=height/2-78;
        adaptive=ClientPerformanceConfig.ADAPTIVE_WORK.get();
        addRenderableWidget(Button.builder(label(),b->{ adaptive=!adaptive; b.setMessage(label()); }).bounds(x,y,310,20).build());
        fps=addRenderableWidget(new EditBox(font,x+220,y+32,90,20,Component.literal("Target FPS")));
        memory=addRenderableWidget(new EditBox(font,x+220,y+60,90,20,Component.literal("Horizon memory MiB")));
        for (var input : new EditBox[]{fps,memory}) { input.setMaxLength(3); input.setFilter(t->t.chars().allMatch(Character::isDigit)); }
        fps.setValue(Integer.toString(ClientPerformanceConfig.TARGET_FPS.get())); memory.setValue(Integer.toString(ClientPerformanceConfig.HORIZON_MEMORY_MIB.get()));
        apply=addRenderableWidget(Button.builder(Component.literal("Apply"),b->{
            if (!valid()) return;
            ClientPerformanceConfig.TARGET_FPS.set(Integer.parseInt(fps.getValue()));
            ClientPerformanceConfig.HORIZON_MEMORY_MIB.set(Integer.parseInt(memory.getValue()));
            ClientPerformanceConfig.ADAPTIVE_WORK.set(adaptive); ClientPerformanceConfig.SPEC.save(); onClose();
        }).bounds(x,y+138,150,20).build());
        addRenderableWidget(Button.builder(Component.literal("Cancel"),b->onClose()).bounds(x+160,y+138,150,20).build());
        fps.setResponder(t->apply.active=valid()); memory.setResponder(t->apply.active=valid());
    }
    private Component label() { return Component.literal("Adaptive horizon work: " + (adaptive?"ON":"OFF")); }
    private boolean valid() {
        return com.shnewbs.deepprimal.core.DistancePolicy.parse(fps.getValue(),15,240).isPresent()
            && com.shnewbs.deepprimal.core.DistancePolicy.parse(memory.getValue(),1,256).isPresent();
    }
    @Override public void onClose() { minecraft.setScreen(parent); }
    @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float delta) {
        super.extractRenderState(g,mx,my,delta); int x=width/2-155,y=height/2-78;
        g.centeredText(font,title,width/2,y-24,0xffffffff);
        g.text(font,Component.literal("Target FPS (15–240)"),x,y+38,0xffdddddd);
        g.text(font,Component.literal("Horizon memory MiB (1–256)"),x,y+66,0xffdddddd);
        g.textWithWordWrap(font,Component.literal("Uses the lower of this target and your FPS cap. Limits background requests and retained mesh data; simulation stays unchanged."),x,y+90,310,0xffaaaaaa);
    }
}
