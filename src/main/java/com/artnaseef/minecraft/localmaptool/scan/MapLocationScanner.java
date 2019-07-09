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

package com.artnaseef.minecraft.localmaptool.scan;

import com.artnaseef.minecraft.localmaptool.MinecraftEnvironmentType;
import com.artnaseef.minecraft.localmaptool.util.MinecraftMapDirectoryFinder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;
import java.util.function.Consumer;

/**
 * Created by art on 6/28/2019.
 */
public class MapLocationScanner {

    private Logger DEFAULT_LOGGER = LoggerFactory.getLogger(MapLocationScanner.class);

    private Logger log = DEFAULT_LOGGER;

    private MinecraftMapDirectoryFinder minecraftMapDirectoryFinder = new MinecraftMapDirectoryFinder();

    private List<File> allMatchedMaps = new LinkedList<>();
    private Map<MinecraftEnvironmentType, List<File>> matchedFilesByEnvironment = new HashMap<>();
    private final Object matchedMapsSync = new Object();

    private Consumer<MapLocationScanner> onScanComplete;
    private boolean hasRunInit = false;

    private File linuxJavaSaveFolder;
    private File macJavaSaveFolder;
    private File windowsJavaSaveFolder;
    private File windowsNativeSaveFolder;

//========================================
// Getters and Setters
//----------------------------------------

    public Consumer<MapLocationScanner> getOnScanComplete() {
        return onScanComplete;
    }

    public void setOnScanComplete(Consumer<MapLocationScanner> onScanComplete) {
        this.onScanComplete = onScanComplete;
    }

    public List<File> getAllMatchedMaps() {
        synchronized (this.matchedMapsSync) {
            return new LinkedList<>(this.allMatchedMaps);
        }
    }

    public List<File> getMatchedForEnvironment(MinecraftEnvironmentType environmentType) {
        synchronized (this.matchedMapsSync) {
            List<File> matches = this.matchedFilesByEnvironment.get(environmentType);
            if (matches == null) {
                matches = Collections.EMPTY_LIST;
            }

            return new LinkedList<>(matches);
        }
    }

    public File getLinuxJavaSaveFolder() {
        return linuxJavaSaveFolder;
    }

    public File getMacJavaSaveFolder() {
        return macJavaSaveFolder;
    }

    public File getWindowsJavaSaveFolder() {
        return windowsJavaSaveFolder;
    }

    public File getWindowsNativeSaveFolder() {
        return windowsNativeSaveFolder;
    }

//========================================
// Lifecycle
//----------------------------------------

    public void init() {
        String appData = System.getenv("APPDATA");
        String userHome = System.getProperty("user.home");
        System.out.println("APP DATA = " + appData);
        System.out.println("USER HOME = " + userHome);

        this.linuxJavaSaveFolder = this.locateLinuxJavaSaveFolder();
        this.macJavaSaveFolder = this.locateMacJavaSaveFolder();
        this.windowsJavaSaveFolder = this.locateWindowsJavaSaveFolder();
        this.windowsNativeSaveFolder = this.locateWindowsNativeSaveFolder();
    }


//========================================
// Operations
//----------------------------------------

    public void executeScan() {
        this.checkInit();
        String appData = System.getenv("APPDATA");
        String userHome = System.getProperty("user.home");
        System.out.println("APP DATA = " + appData);
        System.out.println("USER HOME = " + userHome);

        try {
            synchronized (this.allMatchedMaps) {
                this.allMatchedMaps.clear();
            }

            List<File> tempMatches;

            if (this.windowsJavaSaveFolder != null) {
                tempMatches = this.minecraftMapDirectoryFinder.findMaps(windowsJavaSaveFolder);
                this.addMatched(tempMatches);
                this.setEnvironmentMatches(MinecraftEnvironmentType.WIN_JAVA, tempMatches);
                for (File oneMatch : tempMatches) {
                    this.log.info("WIN+JAVA MATCHED {}", oneMatch);
                }
            }

            if (this.windowsNativeSaveFolder != null) {
                tempMatches = this.minecraftMapDirectoryFinder.findMaps(windowsNativeSaveFolder);
                this.addMatched(tempMatches);
                this.setEnvironmentMatches(MinecraftEnvironmentType.WIN_NATIVE, tempMatches);
                for (File oneMatch : tempMatches) {
                    this.log.info("WIN+NATIVE MATCHED {}", oneMatch);
                }
            }

            if (this.macJavaSaveFolder != null) {
                tempMatches = this.minecraftMapDirectoryFinder.findMaps(macJavaSaveFolder);
                this.addMatched(tempMatches);
                this.setEnvironmentMatches(MinecraftEnvironmentType.MAC, tempMatches);
                for (File oneMatch : tempMatches) {
                    this.log.info("MAC MATCHED {}", oneMatch);
                }
            }

            if (this.linuxJavaSaveFolder != null) {
                tempMatches = this.minecraftMapDirectoryFinder.findMaps(linuxJavaSaveFolder);
                this.addMatched(tempMatches);
                this.setEnvironmentMatches(MinecraftEnvironmentType.LINUX, tempMatches);
                for (File oneMatch : tempMatches) {
                    this.log.info("LINUX MATCHED {}", oneMatch);
                }
            }
        } finally {
            // Notify the listener that scanning is complete
            Consumer<MapLocationScanner> listener = this.onScanComplete;
            if (listener != null) {
                listener.accept(this);
            }
        }
    }

//========================================
// Internals
//----------------------------------------

    private void checkInit() {
        if (!this.hasRunInit) {
            this.init();
            this.hasRunInit = true;
        }
    }

    private void addMatched(List<File> matched) {
        synchronized (this.matchedMapsSync) {
            this.allMatchedMaps.addAll(matched);
        }
    }

    private void setEnvironmentMatches(MinecraftEnvironmentType environmentType, List<File> matched) {
        synchronized (this.matchedMapsSync) {
            this.matchedFilesByEnvironment.put(environmentType, new LinkedList<>(matched));
        }
    }

    private File locateWindowsJavaSaveFolder() {
        String appData = System.getenv("APPDATA");

        if ((appData != null) && (! appData.isEmpty())) {
            File appDataDir = new File(appData);
            return this.openDescendent(appDataDir, ".minecraft", "saves");
        }

        return null;
    }

    private File locateWindowsNativeSaveFolder() {
        String localAppData = System.getenv("LOCALAPPDATA");

        // TODO: as yet, untested
        // C:\Users\(your pc username)\AppData\Local\Packages\Microsoft.MinecraftUWP_8wekyb3d8bbwe\LocalState\games\com.mojang\
        if ((localAppData != null) && (! localAppData.isEmpty())) {
            File appDataDir = new File(localAppData);
            return this.openDescendent(appDataDir, "Packages", "Microsoft.MinecraftUWP_8wekyb3d8bbwe",
                    "LocalState", "games", "com.mojang", "saves");
        }

        return null;
    }

    private File locateMacJavaSaveFolder() {
        String userHome = System.getProperty("user.home");

        if ((userHome != null) && (! userHome.isEmpty())) {
            File userHomeDir = new File(userHome);
            return this.openDescendent(userHomeDir, "Library", "Application Support", "minecraft", "saves");
        }

        return null;
    }

    private File locateLinuxJavaSaveFolder() {
        String userHome = System.getProperty("user.home");

        if ((userHome != null) && (! userHome.isEmpty())) {
            File userHomeDir = new File(userHome);
            return this.openDescendent(userHomeDir, ".minecraft", "saves");
        }

        return null;
    }

    private File openDescendent(File top, String... names) {
        if ((names == null) || (names.length == 0)) {
            return top;
        }

        if (top.isDirectory()) {
            File current = new File(top, names[0]);

            int index = 1;
            while (index < names.length) {
                if (current.isDirectory()) {
                    current = new File(current, names[index]);
                } else {
                    return null;
                }

                index++;
            }

            if (current.exists()) {
                return current;
            }
        }

        return null;
    }
}
