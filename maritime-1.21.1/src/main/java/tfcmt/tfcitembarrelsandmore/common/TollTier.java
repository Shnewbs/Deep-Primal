package tfcmt.tfcitembarrelsandmore.common;

import tfcmt.tfcitembarrelsandmore.common.TIBAMConfig;

public enum TollTier {
    LIGHT,
    MEDIUM,
    HEAVY;


    public int getRate() {
        return switch (this) {
            default -> throw new IncompatibleClassChangeError();
            case LIGHT -> (Integer)TIBAMConfig.SERVER.tollRateLight.get();
            case MEDIUM -> (Integer)TIBAMConfig.SERVER.tollRateMedium.get();
            case HEAVY -> (Integer)TIBAMConfig.SERVER.tollRateHeavy.get();
        };
    }
}

