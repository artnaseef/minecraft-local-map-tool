package com.artnaseef.minecraft.localmaptool.zip;

import com.sun.istack.internal.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.function.Function;
import java.util.zip.ZipException;

/**
 * Created by art on 7/1/19.
 */
public interface ZipUtil {
    void extractZip(File zipFile, File destination, @Nullable ZipParameters zipParameters) throws IOException;
    void createZip(File zipFile, File source) throws IOException;
}
