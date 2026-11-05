# Renderer/API audit — 2026-11-05

Audited GitHub main at `ba857e83dd0de64ad9f360a978182a720f114914`, focusing on backend preferences, main and legacy graphics screens, renderer ownership, startup recovery, window/device creation, backend naming, module inclusion and the installed development package.

## Confirmed installation cause

The local `tfc` test instance loaded `DeepPrimal-26.1.2-0.0.1-alpha.29-dev.127-v8-native-coordinate-preflight.1.jar`. Its JarJar metadata lists Core, GraphicsSettings, Themes, LOD and Performance; the archive contains **no Vulkan entries**. The current session log likewise lists no `deepprimal_vulkan` module. The current renderer properties contain `backend=OpenGL`. These observations establish the current installation state; they do not reconstruct the saved setting at the instant of an earlier screenshot.

The default Gradle package deliberately excludes the optional backend. `build.gradle` adds it only with `-PvulkanPlatform=windows` (or another supported platform) or `-PbundleVulkan`. Merely choosing Vulkan in settings cannot install that backend.

## Findings and corrections

| Finding | Impact | Correction |
| --- | --- | --- |
| Main selector displays the saved/next-launch API with a generic Graphics API label. | A Vulkan preference can be mistaken for an active Vulkan device before restarting or after fallback. | Label it API after restart; report active and staged APIs together using the live device name. |
| Main graphics Apply unconditionally saves the backend; saving clears the startup marker. | Changing unrelated options can erase Vulkan crash recovery state. | Save the backend only after an explicit selector interaction or an actual preference change. Preserve explicit same-choice retry. Apply the same intent rule to the legacy API screen. |
| The menu omits the startup fallback reason and launch override. | Missing backend, failed startup, competing renderer and launch override look alike to the player. | Report missing module, delegated ownership, crash recovery, window/device failure and the overriding JVM property without loading native graphics libraries. |
| Legacy selector is entirely disabled without the optional module, including when Vulkan remains saved. | User cannot switch a stale Vulkan preference back to OpenGL there. | Permit switching that saved preference to OpenGL, then disable unavailable Vulkan selection. |

## Backend identity and fallback review

`VkGpuDevice.getBackendName()` returns Vulkan. The main menu's active API already reads `RenderSystem.getDevice().getBackendName()`; it is not derived from the saved setting. No inspected Deep Primal code relabels an active Vulkan device as OpenGL.

The Vulkan mixin plugin only activates when startup requests Vulkan. Startup may decline after an incomplete attempt or when renderer ownership is delegated. A JVM `deepprimal.graphicsBackend` property overrides the saved preference. Device creation failures attempt OpenGL and log the original exception. These fallback behaviors are retained.

This is a scoped audit, not a proof that the entire renderer has no bugs. A screenshot of Vulkan selected alongside OpenGL does not establish whether Vulkan initialized successfully; the active device, module inventory and startup log are the relevant evidence.

## Validation

- Java 25.0.4.1 compiled seven audited Java sources (the six changed/new production classes plus unchanged RendererOwnership) against 94 existing classpath entries, including Minecraft 26.1.2 / NeoForge 26.1.2.122 and installed dev.127 settings/core dependencies: exit 0.
- `tools/validate_renderer_status.py`: 33 Java assertions across six fresh-process scenarios (diagnostics matrix, real preference-file/marker I/O, crash recovery, launch override, external ownership, loaded first-frame lifecycle).
- Negative regression: restoring the original unconditional backend-save block causes the marker-preservation check to fail; the runner verifies this failure.
- Focused runtime checks use loader/client API doubles and the main screen's verbatim extracted backend-save block. They are not a complete native UI test.
- No full Gradle build, new release JAR, Vulkan hardware launch, shader acceptance or performance certification is claimed. Existing local water-engine edits and installed game files were not modified.

Run the checks with a JDK using `python tools/validate_renderer_status.py --java-home <JDK-directory>`.

## Installed-client follow-through

Build/package the existing source with `gradlew.bat -PvulkanPlatform=windows build` to include the Windows Vulkan adapter and required natives. Install that resulting Vulkan-enabled package, select Vulkan, Apply and fully restart Minecraft. Verify that the loaded modules include `deepprimal_vulkan`, the live API reads Vulkan, and startup reaches `DEEPPRIMAL_VULKAN_FIRST_FRAME`. A successful OpenGL fallback does not count as Vulkan validation.
