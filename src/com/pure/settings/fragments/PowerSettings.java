/*
 * Copyright (C) 2016 The ABC rom
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.pure.settings.fragments;

import android.app.Activity;
import android.content.ContentResolver;
import android.os.Bundle;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.provider.Settings;

import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.internal.util.pure.DuUtils;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class PowerSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String KEY_PROXIMITY_WAKE = "proximity_on_wake";
    private static final String KEYGUARD_TOGGLE_TORCH = "keyguard_toggle_torch";

    private SwitchPreference mKeyguardTorch;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Activity activity = getActivity();
        final ContentResolver resolver = activity.getContentResolver();

        addPreferencesFromResource(R.xml.power_settings);
        PreferenceScreen prefSet = getPreferenceScreen();

        boolean proximityCheckOnWake = getResources().getBoolean(
                com.android.internal.R.bool.config_proximityCheckOnWake);
        if (!proximityCheckOnWake) {
            getPreferenceScreen().removePreference(findPreference(KEY_PROXIMITY_WAKE));
            Settings.System.putInt(resolver, Settings.System.PROXIMITY_ON_WAKE, 0);
        }

        mKeyguardTorch = (SwitchPreference) findPreference(KEYGUARD_TOGGLE_TORCH);
        mKeyguardTorch.setOnPreferenceChangeListener(this);
        if (!DuUtils.deviceSupportsFlashLight(getActivity())) {
            prefSet.removePreference(mKeyguardTorch);
        } else {
        mKeyguardTorch.setChecked((Settings.System.getInt(resolver,
                Settings.System.KEYGUARD_TOGGLE_TORCH, 0) == 1));
        }
    }

    public boolean onPreferenceChange(Preference preference, Object objValue) {
        if  (preference == mKeyguardTorch) {
            boolean checked = ((SwitchPreference)preference).isChecked();
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.KEYGUARD_TOGGLE_TORCH, checked ? 1:0);
            return true;
        }
        return false;
    }

    @Override
    protected int getMetricsCategory() {
        return MetricsEvent.PURE_SETTINGS;
    }
}
