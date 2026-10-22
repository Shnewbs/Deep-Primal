package tfcmt.tfcitembarrelsandmore.common;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import net.minecraft.world.inventory.ContainerData;

/** Small integer binding table shared by machine menus and their block entities. */
public final class IntArrayBuilder implements ContainerData {
    private record Binding(IntSupplier get, IntConsumer set) {}
    private final List<Binding> bindings = new ArrayList<>();
    public IntArrayBuilder add(IntSupplier getter, IntConsumer setter) {
        bindings.add(new Binding(getter, setter));
        return this;
    }
    @Override public int get(int index) { return bindings.get(index).get().getAsInt(); }
    @Override public void set(int index, int value) { bindings.get(index).set().accept(value); }
    @Override public int getCount() { return bindings.size(); }
}
