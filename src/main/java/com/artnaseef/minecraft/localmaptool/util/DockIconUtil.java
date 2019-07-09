package com.artnaseef.minecraft.localmaptool.util;

import java.awt.*;
import java.lang.reflect.Method;

/**
 * Created by art on 7/8/19.
 */
public class DockIconUtil {

    public void setDockIcon(Image iconImage) {
        // Attempt to use the OSX-specific methods
        this.trySetMacDockIcon(iconImage);

        // Nothing to do on windows; the frame icon is used, it seems.
    }

    private void trySetMacDockIcon(Image iconImage) {
        try {
            Class clazz = Class.forName("com.apple.eawt.Application");

            Method getApplicationMethod = clazz.getMethod("getApplication");
            if (getApplicationMethod != null) {
                Object application = getApplicationMethod.invoke(null);

                if (application != null) {
                    Method method = clazz.getMethod("setDockIconImage", java.awt.Image.class);
                    if (method != null) {
                        method.invoke(application, iconImage);
                    }
                }
            }
        } catch (Exception exc) {
            // Failed
        }
    }
}
