/*
 * Copyright (C) 2024-2025 The EverestOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.tweaks.fragments.quicksettings;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.provider.Settings;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settingslib.search.SearchIndexable;

import java.util.List;

import lineageos.preference.LineageSecureSettingSwitchPreference;
import lineageos.providers.LineageSettings;

import com.android.internal.util.everest.ThemeUtils;
import com.android.settings.preferences.SystemSettingSwitchPreference;
import com.android.tweaks.utils.DeviceUtils;

@SearchIndexable
public class QuickSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String TAG = "QuickSettings";

    private static final String KEY_BRIGHTNESS_SLIDER_POSITION = "qs_brightness_slider_position";
    private static final String KEY_BRIGHTNESS_SLIDER_HAPTIC = "qs_brightness_slider_haptic";
    private static final String KEY_INTERFACE_CATEGORY = "quick_settings_interface_category";
    private static final String KEY_MISCELLANEOUS_CATEGORY = "quick_settings_miscellaneous_category";
    private static final String KEY_QS_BLUETOOTH_SHOW_DIALOG = "qs_bt_show_dialog";
    private static final String KEY_SHOW_BRIGHTNESS_SLIDER = "qs_show_brightness_slider";
    private static final String KEY_SHOW_AUTO_BRIGHTNESS = "qs_show_auto_brightness";
    private static final String KEY_NOTIF_STYLE = "notification_style";

    private PreferenceCategory mInterfaceCategory;
    private PreferenceCategory mMiscellaneousCategory;
    private ListPreference mShowBrightnessSlider;
    private ListPreference mBrightnessSliderPosition;
    private LineageSecureSettingSwitchPreference mShowAutoBrightness;
    private SystemSettingSwitchPreference mBrightnessSliderHaptic;
    private Preference mSplitShadePref;
    private Preference  mNotificationStyleSwitch;

    private  static ThemeUtils mThemeUtils;

    private  ThemeUtils mThemeUtils_split;

    private  ThemeUtils mThemeUtils_noti;

    private Handler mHandler = new Handler();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.everest_quicksettings);

        final Context context = getContext();
        final ContentResolver resolver = context.getContentResolver();
        final PreferenceScreen prefScreen = getPreferenceScreen();
        final Resources resources = context.getResources();

        mNotificationStyleSwitch = (Preference) findPreference("notification_style");
        mNotificationStyleSwitch.setOnPreferenceChangeListener(this);

        mSplitShadePref = (Preference) findPreference("qs_split_shade_enabled");
        mSplitShadePref.setOnPreferenceChangeListener(this);

        mShowBrightnessSlider = findPreference(KEY_SHOW_BRIGHTNESS_SLIDER);
        mShowBrightnessSlider.setOnPreferenceChangeListener(this);
        boolean showSlider = LineageSettings.Secure.getIntForUser(resolver,
                LineageSettings.Secure.QS_SHOW_BRIGHTNESS_SLIDER, 1, UserHandle.USER_CURRENT) > 0;

        mBrightnessSliderPosition = findPreference(KEY_BRIGHTNESS_SLIDER_POSITION);
        mBrightnessSliderPosition.setEnabled(showSlider);

        mBrightnessSliderHaptic = findPreference(KEY_BRIGHTNESS_SLIDER_HAPTIC);
        mBrightnessSliderHaptic.setEnabled(showSlider);

        mShowAutoBrightness = findPreference(KEY_SHOW_AUTO_BRIGHTNESS);
        boolean automaticAvailable = context .getResources().getBoolean(
                com.android.internal.R.bool.config_automatic_brightness_available);
        if (automaticAvailable) {
            mShowAutoBrightness.setEnabled(showSlider);
        } else {
            prefScreen.removePreference(mShowAutoBrightness);
        }

        mMiscellaneousCategory = (PreferenceCategory) findPreference(KEY_MISCELLANEOUS_CATEGORY);

        if (!DeviceUtils.deviceSupportsBluetooth(context )) {
            prefScreen.removePreference(mMiscellaneousCategory);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        final Context context = getContext();
        final ContentResolver resolver = context.getContentResolver();
        if (preference == mShowBrightnessSlider) {
            int value = Integer.parseInt((String) newValue);
            mBrightnessSliderPosition.setEnabled(value > 0);
            mBrightnessSliderHaptic.setEnabled(value > 0);
            if (mShowAutoBrightness != null)
                mShowAutoBrightness.setEnabled(value > 0);
            return true;
        } else if (preference == mSplitShadePref) {
            int value = (boolean) newValue ? 1 : 0;
            Settings.System.putIntForUser(resolver,
                "qs_split_shade_enabled", value, UserHandle.USER_CURRENT);
            updateSplitShadeEnabled(getActivity());
            return true;
        } else if (preference == mNotificationStyleSwitch ) {
                int value = (boolean) newValue ? 1 : 0;
                Settings.System.putIntForUser(resolver,
                    "notification_style", value, UserHandle.USER_CURRENT);
                    updateNotifStyle(getActivity());
                return true;
        }
        return false;
    }

    private void updateSplitShadeEnabled(Context context) {
        ContentResolver resolver = context.getContentResolver();
        boolean splitShadeEnabled = Settings.System.getIntForUser(
                resolver,
                "qs_split_shade_enabled" , 0, UserHandle.USER_CURRENT) != 0;
	    String splitShadeStyleCategory = "android.theme.customization.better_qs";
        String overlayThemeTarget  = "com.android.systemui";
        String overlayThemePackage  = "com.android.system.qs.ui.better_qs";
        if (mThemeUtils_split == null) {
            mThemeUtils_split = new ThemeUtils(context);
        }
        mHandler.postDelayed(() -> {
            mThemeUtils_split.setOverlayEnabled(splitShadeStyleCategory, overlayThemeTarget, overlayThemeTarget);
            if (splitShadeEnabled) {
                mThemeUtils_split.setOverlayEnabled(splitShadeStyleCategory, overlayThemePackage, overlayThemeTarget);
            }
        }, 1250);
    }

    private void updateNotifStyle(Context context) {
        ContentResolver resolver = context.getContentResolver();
        // Fetch the current notification style setting
        boolean enableStyle = Settings.System.getIntForUser(
                resolver,
                "notification_style",
                0,
                UserHandle.USER_CURRENT
        ) == 1;
        String notifStyleCategory = "android.theme.customization.notification";
        String overlayThemeTarget = "com.android.systemui";
        String overlayPackage = null;
        if (mThemeUtils_noti == null) {
                mThemeUtils_noti = new ThemeUtils(context);
        }
        // Apply the style
        if (enableStyle) {
            overlayPackage = "com.android.theme.notification.fluid";
        }
        // Apply or reset the overlay
        mThemeUtils_noti.setOverlayEnabled(
                notifStyleCategory,
                overlayPackage != null ? overlayPackage : overlayThemeTarget,
                overlayThemeTarget
        );
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.EVEREST;
    }

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
        new BaseSearchIndexProvider(R.xml.everest_quicksettings) {

            @Override
            public List<String> getNonIndexableKeys(Context context) {
                List<String> keys = super.getNonIndexableKeys(context);
                final Resources resources = context.getResources();

                boolean autoBrightnessAvailable = resources.getBoolean(
                        com.android.internal.R.bool.config_automatic_brightness_available);
                if (!autoBrightnessAvailable) {
                    keys.add(KEY_SHOW_AUTO_BRIGHTNESS);
                }
                if (!DeviceUtils.deviceSupportsBluetooth(context)) {
                    keys.add(KEY_QS_BLUETOOTH_SHOW_DIALOG);
                }
                return keys;
            }
        };
}
