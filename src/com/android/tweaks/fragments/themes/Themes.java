/*
 * Copyright (C) 2024-2025 The EverestOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.tweaks.fragments.themes;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settingslib.search.SearchIndexable;

import java.util.List;

import com.android.tweaks.utils.DeviceUtils;

@SearchIndexable
public class Themes extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String TAG = "Themes";

    private static final String KEY_ICONS_CATEGORY = "themes_icons_category";
    private static final String KEY_NAVBAR_ICON = "android.theme.customization.navbar";
    private static final String KEY_SIGNAL_ICON = "android.theme.customization.signal_icon";

    private PreferenceCategory mIconsCategory;
    private Preference mNavbarIcon;
    private Preference mSignalIcon;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.everest_themes);

        final Context context = getContext();
        final ContentResolver resolver = context.getContentResolver();
        final PreferenceScreen prefScreen = getPreferenceScreen();
        final Resources resources = context.getResources();

        mIconsCategory = (PreferenceCategory) findPreference(KEY_ICONS_CATEGORY);
        mNavbarIcon = (Preference) findPreference(KEY_NAVBAR_ICON);
        mSignalIcon = (Preference) findPreference(KEY_SIGNAL_ICON);

        if (!DeviceUtils.deviceSupportsMobileData(context)) {
            mIconsCategory.removePreference(mSignalIcon);
        }

        if (DeviceUtils.isEdgeToEdgeEnabled(context)) {
            mIconsCategory.removePreference(mNavbarIcon);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        final Context context = getContext();
        final ContentResolver resolver = context.getContentResolver();
        return false;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.EVEREST;
    }

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
        new BaseSearchIndexProvider(R.xml.everest_themes) {

            @Override
            public List<String> getNonIndexableKeys(Context context) {
                List<String> keys = super.getNonIndexableKeys(context);
                final Resources resources = context.getResources();

                if (!DeviceUtils.deviceSupportsMobileData(context)) {
                    keys.add(KEY_SIGNAL_ICON);
                }
                return keys;
            }
        };
}
