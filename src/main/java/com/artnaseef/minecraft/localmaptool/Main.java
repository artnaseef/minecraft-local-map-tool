package com.artnaseef.minecraft.localmaptool;

import com.artnaseef.minecraft.localmaptool.util.MinecraftMapDirectoryFinder;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * Locations from https://help.mojang.com/customer/portal/articles/1480874-where-are-minecraft-files-stored-
 *
 * Created by art on 6/27/2019.
 */
public class Main {

    private MinecraftMapDirectoryFinder minecraftMapDirectoryFinder = new MinecraftMapDirectoryFinder();

    public static void main(String[] args) {
        Main instance = new Main();
        instance.instanceMain(args);
    }

    public void instanceMain(String[] args) {
        String appData = System.getenv("APPDATA");
        String userHome = System.getProperty("user.home");
        System.out.println("APP DATA = " + appData);
        System.out.println("USER HOME = " + userHome);

        List<File> allMatches = new LinkedList<>();
        List<File> tempMatches;

        File windowsJavaSaveFolder = this.locateWindowsJavaSaveFolder();
        if (windowsJavaSaveFolder != null) {
            tempMatches = this.minecraftMapDirectoryFinder.findMaps(windowsJavaSaveFolder);
            allMatches.addAll(tempMatches);
            for (File oneMatch : tempMatches) {
                System.out.println("WIN+JAVA MATCHED " + oneMatch);
            }
        }

        File windowsNativeSaveFolder = this.locateWindowsNativeSaveFolder();
        if (windowsNativeSaveFolder != null) {
            tempMatches = this.minecraftMapDirectoryFinder.findMaps(windowsNativeSaveFolder);
            allMatches.addAll(tempMatches);
            for (File oneMatch : tempMatches) {
                System.out.println("WIN+NATIVE MATCHED " + oneMatch);
            }
        }

        File macJavaSaveFolder = this.locateMacJavaSaveFolder();
        if (macJavaSaveFolder != null) {
            tempMatches = this.minecraftMapDirectoryFinder.findMaps(macJavaSaveFolder);
            allMatches.addAll(tempMatches);
            for (File oneMatch : tempMatches) {
                System.out.println("MAC MATCHED " + oneMatch);
            }
        }

        File linuxJavaSaveFolder = this.locateLinuxJavaSaveFolder();
        if (linuxJavaSaveFolder != null) {
            tempMatches = this.minecraftMapDirectoryFinder.findMaps(linuxJavaSaveFolder);
            allMatches.addAll(tempMatches);
            for (File oneMatch : tempMatches) {
                System.out.println("LINUX MATCHED " + oneMatch);
            }
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
