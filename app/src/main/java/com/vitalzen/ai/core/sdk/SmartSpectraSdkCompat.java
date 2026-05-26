package com.vitalzen.ai.core.sdk;

import androidx.lifecycle.LiveData;

import com.presagetech.smartspectra.SmartSpectraSdk;

public final class SmartSpectraSdkCompat {
    private SmartSpectraSdkCompat() {
    }

    public static LiveData<String> getErrorMessage(SmartSpectraSdk sdk) {
        return sdk.getErrorMessage$sdk_release();
    }

    public static void clearMetricsBuffer(SmartSpectraSdk sdk) {
        sdk.clearMetricsBuffer$sdk_release();
    }
}
