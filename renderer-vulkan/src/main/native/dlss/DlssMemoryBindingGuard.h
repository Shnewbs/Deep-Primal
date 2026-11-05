#pragma once

#include <array>
#include <cstddef>
#include <cstdint>
#include <limits>

namespace deepprimal::dlss {

struct MemoryBinding {
  std::uint64_t memory = 0;
  std::uint64_t offset = 0;
  std::uint64_t size = 0;
};

[[nodiscard]] constexpr bool rangeValid(const MemoryBinding& binding) noexcept {
  return binding.memory != 0 && binding.size != 0
      && binding.offset <= std::numeric_limits<std::uint64_t>::max() - binding.size;
}

[[nodiscard]] constexpr bool rangesOverlap(
    const MemoryBinding& first, const MemoryBinding& second) noexcept {
  if (first.memory != second.memory) return false;
  const std::uint64_t firstEnd = first.offset + first.size;
  const std::uint64_t secondEnd = second.offset + second.size;
  return first.offset < secondEnd && second.offset < firstEnd;
}

// Four bindings: color, depth, motion, and output. This is a contract helper
// for the renderer's allocation owner; it is intentionally separate from the
// JNI handle guard because VkImage identity alone cannot prove disjoint memory.
[[nodiscard]] constexpr bool hasDisjointBindings(
    const std::array<MemoryBinding, 4>& bindings) noexcept {
  for (const auto& binding : bindings)
    if (!rangeValid(binding)) return false;
  for (std::size_t first = 0; first < bindings.size(); ++first)
    for (std::size_t second = first + 1; second < bindings.size(); ++second)
      if (rangesOverlap(bindings[first], bindings[second])) return false;
  return true;
}

} // namespace deepprimal::dlss
