/*
 * Copyright 2014 ParanoidAndroid Project
 *
 * This file is part of Paranoid OTA.
 *
 * Paranoid OTA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Paranoid OTA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Paranoid OTA.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.aicp.aicpota.updater;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import android.content.Context;

import com.aicp.aicpota.R;
import com.aicp.aicpota.Utils;
import com.aicp.aicpota.Version;
import com.aicp.aicpota.helpers.SettingsHelper;
import com.aicp.aicpota.updater.server.AICPServer;

public class GappsUpdater extends Updater {

    private static final String PROPERTIES_FILE = "/system/etc/g.prop";
    private static final String VERSION_PROPERTY = "ro.addon.aicp_version";
    private static final String VERSION_PROPERTY_EXT = "ro.addon.version";
    private static final String PLATFORM_PROPERTY = "ro.build.version.release";
    private static final String TYPE_PROPERTY = "ro.addon.aicp_type";

    private String mPlatform;
    private String mVersion = "-1";
    private String mType;

    public GappsUpdater(Context context, boolean fromAlarm) {
        super(context, new Server[] { new AICPServer() }, fromAlarm);

        File file = new File(PROPERTIES_FILE);
        if (file.exists()) {
            Properties properties = new Properties();
            try {
                properties.load(new FileInputStream(file));
                String versionString = properties.getProperty(VERSION_PROPERTY);
                if (versionString == null || "".equals(versionString)) {
                    versionString = properties.getProperty(VERSION_PROPERTY_EXT);
                }
                mType = properties.getProperty(TYPE_PROPERTY);
                mPlatform = Utils.getProp(PLATFORM_PROPERTY);
                mPlatform = mPlatform.replace(".", "");
                while (mPlatform.length() < 3) {
                    mPlatform = mPlatform + "0";
                }
                if (versionString != null && !"".equals(versionString)) {
                    versionString = versionString.replaceAll("[^\\d.]", "");
                    String[] version = versionString.split("-");
                    for (int i = 0; i < version.length; i++) {
                        try {
                            Integer.parseInt(version[i]);
                            mVersion = version[i];
                            break;
                        } catch (NumberFormatException ex) {
                            // ignore
                        }
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public String getType() {
        return mType == null ? "" : mType;
    }

    private int getTypeForSettings() {
        int type = SettingsHelper.GAPPS_FULL;
        if ("micro".equals(mType)) {
            type = SettingsHelper.GAPPS_MICRO;
        } else if ("mini".equals(mType)) {
            type = SettingsHelper.GAPPS_MINI;
        } else if ("stock".equals(mType)) {
            type = SettingsHelper.GAPPS_STOCK;
        }
        return type;
    }

    public String getPlatform() {
        return mPlatform == null ? "0" : mPlatform;
    }

    @Override
    public Version getVersion() {
        if (mPlatform == null || mPlatform.isEmpty() || mVersion == null || mVersion.isEmpty()) {
            return new Version();
        }
        return Version.fromGapps(getPlatform(), mVersion);
    }

    @Override
    public boolean isRom() {
        return false;
    }

    @Override
    public String getDevice() {
        switch (getSettingsHelper().getGappsType(getTypeForSettings())) {
            case SettingsHelper.GAPPS_MICRO :
                return "gapps";
            case SettingsHelper.GAPPS_MINI :
                return "gapps";
            case SettingsHelper.GAPPS_STOCK:
                return "gapps";
            case SettingsHelper.GAPPS_FULL :
            default :
                return "gapps";
        }
    }

    @Override
    public int getErrorStringId() {
        return R.string.check_gapps_updates_error;
    }

}
