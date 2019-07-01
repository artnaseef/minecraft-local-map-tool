package com.artnaseef.minecraft.localmaptool.zip;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;

/**
 * Created by art on 7/1/19.
 */
public interface ZipUtil {
    void extractZip(File zipFile, File destination, ZipParameters zipParameters) throws IOException;
    void createZip(File zipFile, File source) throws IOException;
    List<? extends ZipEntry> readZipEntryList(File zipFile) throws IOException;
}
