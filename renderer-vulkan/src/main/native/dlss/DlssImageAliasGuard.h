#pragma once

#include <array>
#include <cstddef>

namespace deepprimal::dlss {

// Four (VkImage, VkImageView) pairs: color, depth, motion, and output.
// Check identities within each Vulkan handle type, not across different types.
// This is a defensive JNI/NGX boundary check, NOT a VkDeviceMemory overlap test.
template <typename Handle>
[[nodiscard]] constexpr bool hasDistinctImagesAndViews(
    const std::array<Handle, 8>& handles) noexcept {
  for (std::size_t first = 0; first < 4; ++first) {
    if (handles[2 * first] == 0 || handles[2 * first + 1] == 0) return false;
    for (std::size_t second = first + 1; second < 4; ++second) {
      if (handles[2 * first] == handles[2 * second]
          || handles[2 * first + 1] == handles[2 * second + 1]) return false;
    }
  }
  return true;
}

} // namespace deepprimal::dlss
