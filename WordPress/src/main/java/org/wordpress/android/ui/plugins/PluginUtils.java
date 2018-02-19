package org.wordpress.android.ui.plugins;

import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.wordpress.android.fluxc.model.SiteModel;
import org.wordpress.android.fluxc.model.plugin.SitePluginModel;
import org.wordpress.android.fluxc.model.plugin.WPOrgPluginModel;
import org.wordpress.android.util.AppLog;
import org.wordpress.android.util.CrashlyticsUtils;
import org.wordpress.android.util.StringUtils;
import org.wordpress.android.util.helpers.Version;

public class PluginUtils {
    public static boolean isPluginFeatureAvailable(SiteModel site) {
        String jetpackVersion = site.getJetpackVersion();
        if (site.isUsingWpComRestApi() && site.isJetpackConnected() && !TextUtils.isEmpty(jetpackVersion)) {
            try {
                // strip any trailing "-beta" or "-alpha" from the version
                int index = jetpackVersion.lastIndexOf("-");
                if (index > 0) {
                    jetpackVersion = jetpackVersion.substring(0, index);
                }
                Version siteJetpackVersion = new Version(jetpackVersion);
                Version minVersion = new Version("5.6");
                return siteJetpackVersion.compareTo(minVersion) >= 0; // if the site has Jetpack 5.6 or newer installed
            } catch (IllegalArgumentException e) {
                String errorStr = "Invalid site jetpack version " + jetpackVersion;
                AppLog.e(AppLog.T.UTILS, errorStr, e);
                CrashlyticsUtils.logException(e, AppLog.T.UTILS, errorStr);
                return true;
            }
        }
        return false;
    }

    static int getAverageStarRating(@Nullable WPOrgPluginModel wpOrgPlugin) {
        if (wpOrgPlugin == null) {
            return 0;
        }
        int rating = StringUtils.stringToInt(wpOrgPlugin.getRating(), 1);
        return Math.round(rating / 20f);
    }

    static boolean isUpdateAvailable(@Nullable SitePluginModel plugin, @Nullable WPOrgPluginModel wpOrgPlugin) {
        if (plugin == null || wpOrgPlugin == null
                || TextUtils.isEmpty(plugin.getVersion())
                || TextUtils.isEmpty(wpOrgPlugin.getVersion())) {
            return false;
        }
        try {
            Version currentVersion = new Version(plugin.getVersion());
            Version availableVersion = new Version(wpOrgPlugin.getVersion());
            return currentVersion.compareTo(availableVersion) == -1;
        } catch (IllegalArgumentException e) {
            String errorStr = String.format("An IllegalArgumentException occurred while trying to compare site" +
                    " plugin version: %s with wporg plugin version: %s", plugin.getVersion(), wpOrgPlugin.getVersion());
            AppLog.e(AppLog.T.PLUGINS, errorStr, e);
            CrashlyticsUtils.logException(e, AppLog.T.PLUGINS, errorStr);
            // If the versions are not in the expected format, we can assume that an update is available if the version
            // values for the site plugin and wporg plugin are not the same
            return !plugin.getVersion().equalsIgnoreCase(wpOrgPlugin.getVersion());
        }
    }
}
