/*
 * Copyright (C) 2016 The Pure Nexus Project
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

import android.content.Context;
import android.content.ContentResolver;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;

import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.internal.widget.LockPatternUtils;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.Utils;
import com.pure.settings.preferences.SystemSettingSwitchPreference;

import java.util.List;
import java.util.ArrayList;

public class LockScreenSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener, Indexable {

    private static final int MY_USER_ID = UserHandle.myUserId();

    private static final String APPS_SECURITY = "apps_security";
    private static final String LS_OPTIONS_CAT = "lockscreen_options";
    private static final String LS_SECURE_CAT = "lockscreen_secure_options";

    private static final String FINGERPRINT_VIB = "fingerprint_success_vib";
    private static final String SMS_OUTGOING_CHECK_MAX_COUNT = "sms_outgoing_check_max_count";

    private FingerprintManager mFingerprintManager;
    private SystemSettingSwitchPreference mFingerprintVib;
    private ListPreference mSmsCount;

    private int mSmsCountValue;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.lockscreen_settings);
        PreferenceScreen prefScreen = getPreferenceScreen();
        final LockPatternUtils lockPatternUtils = new LockPatternUtils(getActivity());
        ContentResolver resolver = getActivity().getContentResolver();

        PreferenceCategory appsSecCategory = (PreferenceCategory) findPreference(APPS_SECURITY);
        PreferenceCategory optionsCategory = (PreferenceCategory) findPreference(LS_OPTIONS_CAT);
        PreferenceCategory secureCategory = (PreferenceCategory) findPreference(LS_SECURE_CAT);

        mFingerprintManager = (FingerprintManager) getActivity().getSystemService(Context.FINGERPRINT_SERVICE);
        mFingerprintVib = (SystemSettingSwitchPreference) findPreference(FINGERPRINT_VIB);
        if (!mFingerprintManager.isHardwareDetected()){
            secureCategory.removePreference(mFingerprintVib);
        }

        if (!lockPatternUtils.isSecure(MY_USER_ID)) {
            prefScreen.removePreference(secureCategory);
        }

        mSmsCount = (ListPreference) findPreference(SMS_OUTGOING_CHECK_MAX_COUNT);
        mSmsCountValue = Settings.Secure.getInt(resolver,
                Settings.Global.SMS_OUTGOING_CHECK_MAX_COUNT, 30);
        mSmsCount.setValue(Integer.toString(mSmsCountValue));
        mSmsCount.setSummary(mSmsCount.getEntry());
        mSmsCount.setOnPreferenceChangeListener(this);

        if (!Utils.isVoiceCapable(getActivity())) {
            appsSecCategory.removePreference(mSmsCount);
            prefScreen.removePreference(appsSecCategory);
        }
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        // If we didn't handle it, let preferences handle it.
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();

        if (preference == mSmsCount) {
            mSmsCountValue = Integer.valueOf((String) newValue);
            int index = mSmsCount.findIndexOfValue((String) newValue);
            mSmsCount.setSummary(
                    mSmsCount.getEntries()[index]);
            Settings.Global.putInt(resolver,
                    Settings.Global.SMS_OUTGOING_CHECK_MAX_COUNT, mSmsCountValue);
            return true;
        }
        return false;
    }

    @Override
    protected int getMetricsCategory() {
        return MetricsEvent.PURE_SETTINGS;
    }

    public static final Indexable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {
                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(Context context,
                        boolean enabled) {
                    ArrayList<SearchIndexableResource> result =
                            new ArrayList<SearchIndexableResource>();

                    SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.lockscreen_settings;
                    result.add(sir);

                    return result;
                }

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    ArrayList<String> result = new ArrayList<String>();
                    return result;
                }
            };
}
