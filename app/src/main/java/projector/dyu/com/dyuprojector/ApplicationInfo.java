package projector.dyu.com.dyuprojector;

import android.content.ComponentName;
import android.content.Intent;
import android.graphics.drawable.Drawable;

public class ApplicationInfo {
    public int appType = -1;
    boolean filtered;
    Drawable icon;
    Intent intent;
    CharSequence title;

    public boolean equals(Object paramObject) {
        if (this == paramObject) ;
        while (true) {
            if (!(paramObject instanceof ApplicationInfo)) {
                return false;
            }
            ApplicationInfo localApplicationInfo = (ApplicationInfo) paramObject;
            if ((this.title.equals(localApplicationInfo.title)) && (this.intent.getComponent().getClassName().equals(localApplicationInfo.intent.getComponent().getClassName()))) {
                return true;
            }
        }
    }

    public int hashCode() {
        int i = 0;
        if (this.title != null) ;
        for (int j = this.title.hashCode(); ; j = 0) {
            String str = this.intent.getComponent().getClassName();
            int k = j * 31;
            if (str != null)
                i = str.hashCode();
            return k + i;
        }
    }

    final void setActivity(ComponentName paramComponentName, int paramInt) {
        this.intent = new Intent("android.intent.action.MAIN");
        this.intent.addCategory("android.intent.category.LAUNCHER");
        this.intent.setComponent(paramComponentName);
        this.intent.setFlags(paramInt);
    }
}
