#include "DlssImageAliasGuard.h"

#include <algorithm>
#include <array>
#include <cstdint>
#include <cstdio>
#include <cstdlib>
#include <random>
#include <set>

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

bool reference(const std::array<std::int64_t, 8>& pairs) {
  std::set<std::int64_t> images, views;
  for (int i = 0; i < 4; ++i) {
    if (pairs[2*i] == 0 || pairs[2*i+1] == 0) return false;
    if (!images.insert(pairs[2*i]).second) return false;
    if (!views.insert(pairs[2*i+1]).second) return false;
  }
  return true;
}
}

int main() {
  using deepprimal::dlss::hasDistinctImagesAndViews;
  constexpr std::array<std::int64_t, 8> clean{11, 21, 12, 22, 13, 23, 14, 24};
  static_assert(hasDistinctImagesAndViews(clean), "clean Vulkan frame rejected");
  expect(hasDistinctImagesAndViews(clean), "clean frame rejected");
  for (int a = 0; a < 4; ++a) {
    for (int b = a + 1; b < 4; ++b) {
      for (int slot = 0; slot < 2; ++slot) {
        auto collision = clean;
        collision[2 * b + slot] = collision[2 * a + slot];
        expect(!hasDistinctImagesAndViews(collision), "pair collision accepted");
        collision = clean;
        collision[2 * a + slot] = collision[2 * b + slot];
        expect(!hasDistinctImagesAndViews(collision), "reversed pair collision accepted");
      }
    }
  }
  for (int i = 0; i < 8; ++i) {
    auto missing = clean;
    missing[i] = 0;
    expect(!hasDistinctImagesAndViews(missing), "zero Vulkan handle accepted");
  }
  // Different Vulkan object types may use the same numeric handle. Do not reject those.
  for (int a = 0; a < 4; ++a) {
    for (int b = 0; b < 4; ++b) {
      auto crossType = clean;
      crossType[2 * a] = crossType[2 * b + 1];
      expect(hasDistinctImagesAndViews(crossType), "cross-type numeric overlap rejected");
    }
  }
  std::mt19937_64 rng(0xD155A11A5u);
  for (int test = 0; test < 200000; ++test) {
    std::array<std::int64_t, 8> sample{};
    for (auto& handle : sample) handle = static_cast<std::int64_t>(rng() % 19);
    expect(hasDistinctImagesAndViews(sample) == reference(sample), "random parity mismatch");
  }
  std::printf("PASS assertions=%llu\n", static_cast<unsigned long long>(assertions));
}
