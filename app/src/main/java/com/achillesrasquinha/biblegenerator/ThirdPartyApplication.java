package com.achillesrasquinha.biblegenerator;

import android.content.Context;
import android.content.pm.PackageManager;

public class ThirdPartyApplication {
    public static boolean isInstalled(Context context, String packageName) {
        boolean isInstalled = true;

        try {
            PackageManager lPackageManager = context.getPackageManager();
            lPackageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
        }
        catch (PackageManager.NameNotFoundException e) {
            isInstalled = false;
        }

        return isInstalled;
    }
}
