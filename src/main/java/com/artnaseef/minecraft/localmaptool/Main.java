package com.artnaseef.minecraft.localmaptool;

import com.artnaseef.minecraft.localmaptool.util.MinecraftMapDirectoryFinder;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
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

        File windowsJavaSaveFolder = this.locateWindowsJavaSaveFolder();
        List<File> allMatches = new LinkedList<>();
        List<File> tempMatches;

        if (windowsJavaSaveFolder != null) {
            tempMatches = this.minecraftMapDirectoryFinder.findMaps(windowsJavaSaveFolder);
            allMatches.addAll(tempMatches);
            for (File oneMatch : tempMatches) {
                System.out.println("WIN MATCHED " + oneMatch);
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
    }

    private File locateWindowsJavaSaveFolder() {
        String appData = System.getenv("APPDATA");

        if ((appData != null) && (! appData.isEmpty())) {
            File appDataDir = new File(appData);
            return this.openDescendent(appDataDir, ".minecraft", "saves");
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
