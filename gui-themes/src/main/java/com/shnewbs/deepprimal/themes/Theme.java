package com.shnewbs.deepprimal.themes;

import java.util.Locale;

public enum Theme {
    VANILLA("Vanilla", "Original Minecraft appearance", 0xc6c6c6, 0x555555, 0xffffff, 0x404040),
    BASALT_COPPER("Basalt & Copper", "Dark stone with warm copper edges", 0x252b2d, 0x13191b, 0xc38a57, 0xf2e8d7),
    FIELD_JOURNAL("Field Journal", "Parchment, ink and olive accents", 0xe2d0aa, 0xc2b18d, 0x626e43, 0x332a21),
    RAWHIDE_FLINT("Rawhide & Flint", "Stitched hide and flint", 0x3b2c22, 0x231d19, 0xcda251, 0xf1e0be),
    RIVERSTONE_MOSS("Riverstone & Moss", "Pale stone and weathered green", 0xbac0b5, 0x8d988c, 0x4a6950, 0x25372b),
    TIMBER_IRON("Timber & Iron", "Timber grain with iron fittings", 0x46301e, 0x271d16, 0xa99b70, 0xf3e4c8),
    CLAY_EMBER("Clay & Ember", "Warm clay and ember details", 0xe6caa0, 0xc6a67b, 0xb85d2d, 0x482917),
    OBSIDIAN_ASH("Obsidian & Ash", "Charcoal with ember orange", 0x202326, 0x101315, 0xe26336, 0xe6e7e4),
    FROST_SLATE("Frost & Slate", "Blue slate and ice accents", 0x293b4d, 0x172738, 0x8fc4e1, 0xe4f0f5);

    public final String displayName, description;
    public final int surface, well, accent, text;
    Theme(String name, String description, int surface, int well, int accent, int text) {
        this.displayName=name; this.description=description;
        this.surface=0xff000000|surface; this.well=0xff000000|well;
        this.accent=0xff000000|accent; this.text=0xff000000|text;
    }
    public String id() { return name().toLowerCase(Locale.ROOT); }
    public boolean custom() { return this != VANILLA; }
}
