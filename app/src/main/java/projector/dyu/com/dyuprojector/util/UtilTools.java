package projector.dyu.com.dyuprojector.util;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ApplicationInfo;
import android.text.TextUtils;

public class UtilTools {
        public static boolean checkApkExist(Context context, String packageName){
            if (TextUtils.isEmpty(packageName))
                return false;
            try {
                ApplicationInfo info = context.getPackageManager()
                        .getApplicationInfo(packageName,
                                PackageManager.GET_UNINSTALLED_PACKAGES);
                return true;
            } catch (PackageManager.NameNotFoundException e) {
                return false;
            }
        }
}
