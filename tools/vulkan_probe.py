"""Standalone Vulkan-instance availability check. Not loaded or distributed by the mod.

This is not a renderer test. A successful result only establishes that a Vulkan
1.2 instance and device enumeration are available in the execution environment.
"""
import ctypes as c
import ctypes.util
import json
import platform


class ApplicationInfo(c.Structure):
    _fields_ = [("sType", c.c_uint), ("pNext", c.c_void_p), ("name", c.c_char_p),
                ("version", c.c_uint), ("engine", c.c_char_p),
                ("engineVersion", c.c_uint), ("apiVersion", c.c_uint)]


class InstanceInfo(c.Structure):
    _fields_ = [("sType", c.c_uint), ("pNext", c.c_void_p), ("flags", c.c_uint),
                ("application", c.POINTER(ApplicationInfo)), ("layerCount", c.c_uint),
                ("layers", c.c_void_p), ("extensionCount", c.c_uint), ("extensions", c.c_void_p)]


def probe():
    name = c.util.find_library("vulkan")
    if name is None:
        name = "vulkan-1.dll" if platform.system() == "Windows" else "libvulkan.1.dylib" if platform.system() == "Darwin" else "libvulkan.so.1"
    try:
        loader = c.WinDLL(name) if platform.system() == "Windows" else c.CDLL(name)
    except OSError as error:
        return {"loaderAvailable": False, "error": str(error), "rendererValidated": False}
    app = ApplicationInfo(0, None, b"Deep Primal standalone probe", 1, b"Deep Primal", 1, (1 << 22) | (2 << 12))
    info = InstanceInfo(1, None, 0, c.pointer(app), 0, None, 0, None)
    handle = c.c_void_p()
    loader.vkCreateInstance.argtypes = [c.POINTER(InstanceInfo), c.c_void_p, c.POINTER(c.c_void_p)]
    loader.vkCreateInstance.restype = c.c_int
    code = loader.vkCreateInstance(c.byref(info), None, c.byref(handle))
    result = {"loaderAvailable": True, "requestedApi": "1.2", "instanceResult": code,
              "resultName": "VK_SUCCESS" if code == 0 else "VK_ERROR_INCOMPATIBLE_DRIVER" if code == -9 else "Vulkan error",
              "rendererValidated": False}
    if code == 0:
        try:
            count = c.c_uint()
            loader.vkEnumeratePhysicalDevices.argtypes = [c.c_void_p, c.POINTER(c.c_uint), c.c_void_p]
            loader.vkEnumeratePhysicalDevices.restype = c.c_int
            result["enumerationResult"] = loader.vkEnumeratePhysicalDevices(handle, c.byref(count), None)
            result["physicalDevices"] = count.value
        finally:
            loader.vkDestroyInstance.argtypes = [c.c_void_p, c.c_void_p]
            loader.vkDestroyInstance(handle, None)
    return result


if __name__ == "__main__":
    print(json.dumps(probe(), indent=2))
