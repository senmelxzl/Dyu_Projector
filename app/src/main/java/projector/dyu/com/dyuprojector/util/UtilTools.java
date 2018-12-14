package projector.dyu.com.dyuprojector.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

/**
 * Util Tools
 *
 * @author xiezhenlin
 */
public class UtilTools {
    /**
     * check if app installed
     *
     * @param context
     * @param packageName
     * @return
     */
    public static boolean checkApkExist(Context context, String packageName) {
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

    /**
     * check is need add
     *
     * @param paramString
     * @return
     */
    public static boolean isNeedAddtoApplication(String paramString) {
        if (paramString.equals("cn.wps.moffice_i18n_TV")
                || paramString.equals("com.mediatek.filemanager")
                || paramString.equals("com.mediatek.camera")
                || paramString.equals("com.hpplay.happyplay.aw")
                || paramString.equals("com.elinkway.tvlive2")
                || paramString.equals("com.android.settings")
                || paramString.equals("com.iflytek.inputmethod")
                || paramString.equals("com.android.mms")
                || paramString.equals("com.android.contacts")
                || paramString.equals("com.android.cellbroadcastreceiver")
                || paramString.equals("com.android.calendar")
                || paramString.equals("com.android.dialer")
                || paramString.equals("com.android.email")
                || paramString.equals("com.android.deskclock")
                || paramString.equals("com.mediatek.mtklogger")
                || paramString.equals("com.android.music")) {
            return false;
        }
        return true;
    }

    /**
     * @param paramString1
     * @param paramString2
     * @return
     */
    public static int getAppType(String paramString1, String paramString2) {
        int i = 0;
        if (paramString1 == null || paramString2 == null) {
            i = -1;
        }
        if (paramString1 != null && paramString2 != null) {
            if (paramString1.contains("video") || paramString1.contains("myvst") || paramString1.contains("sohu.tv")) {
                i = 1;
            } else if (paramString1.contains("game") || paramString2.contains("game")) {
                i = 2;
            } else if (paramString1.contains("mobileqq") || paramString1.contains("tencent.mm")) {
                i = 3;
            }
        }
        return i;
    }
}
