package com.shnewbs.deepprimal.settings;

/** Reports the live device separately from the restart preference; never initializes a GPU. */
public final class RendererStatus {
  public static String summary(String active, RendererPreferences.Backend next) {
    return "Active: " + active + " | After restart: " + next;
  }

  public static String explanation(String active, RendererPreferences.Backend next) {
    return explanation(
        active, next, RendererOwnership.installed("deepprimal_vulkan"),
        RendererOwnership.owned(), RendererOwnership.owner(),
        RendererPreferences.startupFallbackReason(), System.getProperty("deepprimal.graphicsBackend"));
  }

  static String explanation(
      String active, RendererPreferences.Backend next, boolean installed, boolean owned,
      String owner, String fallback, String override) {
    String reason;
    if (!installed && !"Vulkan".equals(active))
      reason = "Vulkan module is not installed. Install a Vulkan-enabled Deep Primal build.";
    else if (next == RendererPreferences.Backend.Vulkan && !owned)
      reason = "Deep Primal Vulkan is disabled while the renderer is delegated to " + owner + ".";
    else if (!"Vulkan".equals(active) && !fallback.isEmpty())
      reason = fallback;
    else if (!active.equals(next.name()))
      reason = "Apply, then fully restart Minecraft to use " + next + ".";
    else
      reason = "The active device matches the selected API.";
    if (override != null)
      reason += " Launch override deepprimal.graphicsBackend=" + override
          + " takes priority over the saved choice; change or remove it before restarting.";
    return reason;
  }

  private RendererStatus() {}
}
