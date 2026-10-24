package tfcmt.tfcitembarrelsandmore.common;

public class BraceData {
    public final String type;
    public final String variant;
    public final String mod;

    public BraceData(String type, String variant, String mod) {
        this.type = type;
        this.variant = variant;
        this.mod = mod;
    }

    public boolean isValid() {
        return this.type != null && !this.type.isEmpty() && this.variant != null && !this.variant.isEmpty() && this.mod != null && !this.mod.isEmpty();
    }

    public String toString() {
        return "BraceData{" + this.type + "/" + this.mod + ":" + this.variant + "}";
    }
}

