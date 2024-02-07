/*
 * Copyright (C) 2024-2025 The EverestOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.tweaks.fragments.notifications;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;

import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settingslib.search.SearchIndexable;

import com.android.settings.preferences.SystemSettingSeekBarPreference;

import java.util.List;

@SearchIndexable
public class Notifications extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String TAG = "Notifications";

    private static final String NOTIF_PANEL_MAX_NOTIF_CONFIG = "notif_panel_max_notif_cofig";

    private SystemSettingSeekBarPreference mMaxNotifPanelNotifConfig;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.everest_notifications);

        final Context context = getContext();
        final ContentResolver resolver = context.getContentResolver();
        final PreferenceScreen prefScreen = getPreferenceScreen();
        final Resources resources = context.getResources();

        mMaxNotifPanelNotifConfig = (SystemSettingSeekBarPreference) findPreference(NOTIF_PANEL_MAX_NOTIF_CONFIG);
        int nPconf = Settings.System.getInt(getContentResolver(),
                Settings.System.NOTIF_PANEL_MAX_NOTIF_CONFIG, 3);
        mMaxNotifPanelNotifConfig.setValue(nPconf);
        mMaxNotifPanelNotifConfig.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        final Context context = getContext();
        final ContentResolver resolver = context.getContentResolver();
    if (preference == mMaxNotifPanelNotifConfig) {
            int nPconf = (Integer) newValue;
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.NOTIF_PANEL_MAX_NOTIF_CONFIG, nPconf);
            return true;
        }
        return false;
    }

    public static void reset(Context mContext) {
        ContentResolver resolver = mContext.getContentResolver();
        Settings.System.putIntForUser(resolver,
                Settings.System.NOTIF_PANEL_CUSTOM_NOTIF, 0, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.NOTIF_PANEL_MAX_NOTIF_CONFIG, 3, UserHandle.USER_CURRENT);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.EVEREST;
    }

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
        new BaseSearchIndexProvider(R.xml.everest_notifications) {

            @Override
            public List<String> getNonIndexableKeys(Context context) {
                List<String> keys = super.getNonIndexableKeys(context);
                final Resources resources = context.getResources();
                return keys;
            }
        };
}
