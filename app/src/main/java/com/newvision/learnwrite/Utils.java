package com.newvision.learnwrite;


import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;

import java.util.Locale;

/**
 * @author Nir Parikman
 */
public class Utils {

	/**
	 * indicate if the locale is Left to right
	 * @return
	 */
	public static boolean isLTR() {
		Locale locale = Locale.getDefault();
		String displayName = locale.getDisplayName(locale);
		byte directionality = Character.getDirectionality(displayName.charAt(0));
		if(directionality == Character.DIRECTIONALITY_RIGHT_TO_LEFT || directionality == Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC) {
			return false;
		}
		return true;
	}

    /**
     * update locale and configuration with user choose
     * @param activity
     */
    public static void updateApplicationWithLanguageFromSetting(Activity activity) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
        String language = sharedPreferences.getString(MainActivity.LOCALE,"");

        if(language==null || language.trim().length()==0) {
            return;
        }

        Locale[] availableLocales = Locale.getAvailableLocales();
        Locale locale = Locale.getDefault();
        for(int index=0;index<availableLocales.length;index++) {
            if(availableLocales[index].getLanguage().equals(language)) {
                locale = availableLocales[index];
                index = availableLocales.length+1;
            }
        }

        Locale.setDefault(locale);
        Resources res = activity.getResources();
        // Change locale settings in the app.
        DisplayMetrics dm = res.getDisplayMetrics();
        android.content.res.Configuration conf = res.getConfiguration();
        conf.locale = locale;
        res.updateConfiguration(conf, dm);

    }



}
