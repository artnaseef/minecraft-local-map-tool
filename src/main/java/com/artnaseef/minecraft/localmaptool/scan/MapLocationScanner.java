package com.artnaseef.minecraft.localmaptool.scan;

import com.artnaseef.minecraft.localmaptool.MinecraftEnvironmentType;
import com.artnaseef.minecraft.localmaptool.util.MinecraftMapDirectoryFinder;

import java.io.File;
import java.util.*;
import java.util.function.Consumer;

/**
 * Created by art on 6/28/2019.
 */
public class MapLocationScanner {

    private MinecraftMapDirectoryFinder minecraftMapDirectoryFinder = new MinecraftMapDirectoryFinder();

    private List<File> allMatchedMaps = new LinkedList<>();
    private Map<MinecraftEnvironmentType, List<File>> matchedFilesByEnvironment = new HashMap<>();
    private final Object matchedMapsSync = new Object();

    private Consumer<MapLocationScanner> onScanComplete;

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

    public void executeScan() {
        String appData = System.getenv("APPDATA");
        String userHome = System.getProperty("user.home");
        System.out.println("APP DATA = " + appData);
        System.out.println("USER HOME = " + userHome);

        try {
            synchronized (this.allMatchedMaps) {
                this.allMatchedMaps.clear();
            }
            List<File> tempMatches;

            File windowsJavaSaveFolder = this.locateWindowsJavaSaveFolder();
            if (windowsJavaSaveFolder != null) {
                tempMatches = this.minecraftMapDirectoryFinder.findMaps(windowsJavaSaveFolder);
                this.addMatched(tempMatches);
                this.setEnvironmentMatches(MinecraftEnvironmentType.WIN_JAVA, tempMatches);
                for (File oneMatch : tempMatches) {
                    System.out.println("WIN+JAVA MATCHED " + oneMatch);
                }
            }

            File windowsNativeSaveFolder = this.locateWindowsNativeSaveFolder();
            if (windowsNativeSaveFolder != null) {
                tempMatches = this.minecraftMapDirectoryFinder.findMaps(windowsNativeSaveFolder);
                this.addMatched(tempMatches);
                this.setEnvironmentMatches(MinecraftEnvironmentType.WIN_NATIVE, tempMatches);
                for (File oneMatch : tempMatches) {
                    System.out.println("WIN+NATIVE MATCHED " + oneMatch);
                }
            }

            File macJavaSaveFolder = this.locateMacJavaSaveFolder();
            if (macJavaSaveFolder != null) {
                tempMatches = this.minecraftMapDirectoryFinder.findMaps(macJavaSaveFolder);
                this.addMatched(tempMatches);
                this.setEnvironmentMatches(MinecraftEnvironmentType.MAC, tempMatches);
                for (File oneMatch : tempMatches) {
                    System.out.println("MAC MATCHED " + oneMatch);
                }
            }

            File linuxJavaSaveFolder = this.locateLinuxJavaSaveFolder();
            if (linuxJavaSaveFolder != null) {
                tempMatches = this.minecraftMapDirectoryFinder.findMaps(linuxJavaSaveFolder);
                this.addMatched(tempMatches);
                this.setEnvironmentMatches(MinecraftEnvironmentType.LINUX, tempMatches);
                for (File oneMatch : tempMatches) {
                    System.out.println("LINUX MATCHED " + oneMatch);
                }
            }
        } finally {
            Consumer<MapLocationScanner> listener = this.onScanComplete;
            if (listener != null) {
                listener.accept(this);
            }
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