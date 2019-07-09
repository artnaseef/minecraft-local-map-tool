/*
 * Copyright 2019 Arthur Naseef
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
