package tfcmt.tfcitembarrelsandmore.compat.valkyrienskies;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.valkyrienskies.core.api.VsCoreApi;
import org.valkyrienskies.core.api.attachment.AttachmentRegistration;
import tfcmt.tfcitembarrelsandmore.compat.valkyrienskies.PropellerListenerSet;
import tfcmt.tfcitembarrelsandmore.compat.valkyrienskies.WaterPaddleListenerSet;
import tfcmt.tfcitembarrelsandmore.tfcitembarrelsandmore;

public final class VSRegistration {
    private static boolean registered = false;
    private static boolean paddleRegistered = false;

    public static void register() {
        AttachmentRegistration registration;
        VsCoreApi api = null;
        if (!registered) {
            api = VSRegistration.findApi();
            if (api == null) {
                tfcitembarrelsandmore.LOGGER.warn("[TFC Maritime Travelers] Could not find VsCoreApi singleton \u2014 propeller thrust will be disabled. If you are running VS 2.4.x, please report this on the mod's issue tracker.");
            } else {
                try {
                    registration = api.newAttachmentRegistrationBuilder(PropellerListenerSet.class).useTransientSerializer().build();
                    api.registerAttachment(registration);
                    registered = true;
                    tfcitembarrelsandmore.LOGGER.info("[TFC Maritime Travelers] Registered PropellerListenerSet with VsCoreApi.");
                }
                catch (Exception e) {
                    tfcitembarrelsandmore.LOGGER.warn("[TFC Maritime Travelers] registerAttachment failed for PropellerListenerSet \u2014 propeller thrust disabled. Cause: {}", (Object)(e.getCause() != null ? e.getCause().toString() : e.toString()));
                }
            }
        }
        if (!paddleRegistered) {
            if (api == null) {
                api = VSRegistration.findApi();
            }
            if (api == null) {
                tfcitembarrelsandmore.LOGGER.warn("[TFC Maritime Travelers] Could not find VsCoreApi singleton \u2014 water paddle thrust will be disabled.");
                return;
            }
            try {
                registration = api.newAttachmentRegistrationBuilder(WaterPaddleListenerSet.class).useTransientSerializer().build();
                api.registerAttachment(registration);
                paddleRegistered = true;
                tfcitembarrelsandmore.LOGGER.info("[TFC Maritime Travelers] Registered WaterPaddleListenerSet with VsCoreApi.");
            }
            catch (Exception e) {
                tfcitembarrelsandmore.LOGGER.warn("[TFC Maritime Travelers] registerAttachment failed for WaterPaddleListenerSet \u2014 water paddle thrust disabled. Cause: {}", (Object)(e.getCause() != null ? e.getCause().toString() : e.toString()));
            }
        }
    }

    public static boolean isRegistered() {
        return registered;
    }

    public static boolean isPaddleRegistered() {
        return paddleRegistered;
    }

    private static VsCoreApi findApi() {
        VsCoreApi api = VSRegistration.tryGetStaticApi("org.valkyrienskies.mod.api.ValkyrienSkies", "api");
        if (api != null) {
            return api;
        }
        return VSRegistration.tryGetApi("org.valkyrienskies.mod.common.ValkyrienSkiesMod", "getApi");
    }

    private static VsCoreApi tryGetStaticApi(String className, String methodName) {
        try {
            Class<?> cls = Class.forName(className);
            Method getter = cls.getMethod(methodName, new Class[0]);
            Object result = getter.invoke(null, new Object[0]);
            if (result instanceof VsCoreApi) {
                VsCoreApi coreApi = (VsCoreApi)result;
                return coreApi;
            }
        }
        catch (ClassNotFoundException cls) {
        }
        catch (Exception e) {
            tfcitembarrelsandmore.LOGGER.debug("[TFC Maritime Travelers] VsCoreApi probe {}#{} failed: {}", new Object[]{className, methodName, e.getMessage()});
        }
        return null;
    }

    private static VsCoreApi tryGetApi(String className, String methodName) {
        try {
            Class<?> cls = Class.forName(className);
            Field instance = cls.getField("INSTANCE");
            Object obj = instance.get(null);
            Method getter = cls.getMethod(methodName, new Class[0]);
            Object result = getter.invoke(obj, new Object[0]);
            if (result instanceof VsCoreApi) {
                VsCoreApi coreApi = (VsCoreApi)result;
                return coreApi;
            }
        }
        catch (ClassNotFoundException cls) {
        }
        catch (Exception e) {
            tfcitembarrelsandmore.LOGGER.debug("[TFC Maritime Travelers] VsCoreApi probe {}#{} failed: {}", new Object[]{className, methodName, e.getMessage()});
        }
        return null;
    }

    private VSRegistration() {
    }
}

