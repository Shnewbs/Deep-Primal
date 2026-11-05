#include "DlssMemoryBindingGuard.h"

#include <array>
#include <cstdint>
#include <cstdio>
#include <cstdlib>
#include <limits>
#include <random>

namespace {
std::uint64_t assertions = 0;

void expect(bool value, const char* message) {
  ++assertions;
  if (!value) {
    std::fprintf(stderr, "FAIL assertion=%llu: %s\n",
        static_cast<unsigned long long>(assertions), message);
    std::exit(1);
}
}

bool reference(const std::array<deepprimal::dlss::MemoryBinding, 4>& bindings) {
  for (const auto& binding : bindings) {
    if (binding.memory == 0 || binding.size == 0
        || binding.offset > std::numeric_limits<std::uint64_t>::max() - binding.size)
      return false;
  }
  for (std::size_t first = 0; first < bindings.size(); ++first) {
    const auto firstEnd = bindings[first].offset + bindings[first].size;
    for (std::size_t second = first + 1; second < bindings.size(); ++second) {
      const auto secondEnd = bindings[second].offset + bindings[second].size;
      if (bindings[first].memory == bindings[second].memory
          && bindings[first].offset < secondEnd
          && bindings[second].offset < firstEnd)
        return false;
    }
  }
  return true;
}
}

int main() {
  using deepprimal::dlss::MemoryBinding;
  using deepprimal::dlss::hasDisjointBindings;
  constexpr std::array<MemoryBinding, 4> clean{{
      {11, 0, 64}, {11, 64, 64}, {11, 128, 64}, {12, 0, 128}}};
  static_assert(hasDisjointBindings(clean), "clean bindings rejected");
  expect(hasDisjointBindings(clean), "clean bindings rejected");

  auto overlap = clean;
  overlap[1] = {11, 32, 64};
  expect(!hasDisjointBindings(overlap), "overlap accepted");

  auto touching = clean;
  touching[1] = {11, 64, 64};
  expect(hasDisjointBindings(touching), "touching ranges rejected");

  auto zeroMemory = clean;
  zeroMemory[0].memory = 0;
  expect(!hasDisjointBindings(zeroMemory), "zero memory accepted");

  auto zeroSize = clean;
  zeroSize[0].size = 0;
  expect(!hasDisjointBindings(zeroSize), "zero size accepted");

  auto overflow = clean;
  overflow[0] = {11, std::numeric_limits<std::uint64_t>::max() - 3, 8};
  expect(!hasDisjointBindings(overflow), "overflowing range accepted");

  std::mt19937_64 rng(0xD155A11Au);
  for (int test = 0; test < 200000; ++test) {
    std::array<MemoryBinding, 4> sample{};
    for (auto& binding : sample) {
      binding.memory = rng() % 5;
      binding.offset = rng() % 256;
      binding.size = rng() % 128;
    }
    expect(hasDisjointBindings(sample) == reference(sample), "random parity mismatch");
  }
  std::printf("PASS assertions=%llu\n", static_cast<unsigned long long>(assertions));
}
