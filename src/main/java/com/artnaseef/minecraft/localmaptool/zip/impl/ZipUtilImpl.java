package com.artnaseef.minecraft.localmaptool.zip.impl;

import com.artnaseef.minecraft.localmaptool.zip.ZipParameters;
import com.artnaseef.minecraft.localmaptool.zip.ZipUtil;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * Created by art on 7/1/19.
 */
public class ZipUtilImpl implements ZipUtil {

    private static final Logger DEFAULT_LOGGER = LoggerFactory.getLogger(ZipUtilImpl.class);

    private Logger log = DEFAULT_LOGGER;

//========================================
// Getter and Setter
//----------------------------------------

    public Logger getLog() {
        return log;
    }

    public void setLog(Logger log) {
        this.log = log;
    }

//========================================
// Processing Interface
//----------------------------------------

    public void extractZip(File zipFile, File destination, ZipParameters zipParameters) throws IOException {
        ZipFile inputZipFile = new ZipFile(zipFile);

        Stream<? extends ZipEntry> inputStream = inputZipFile.stream();

        Predicate<ZipEntry> inclusionPredicate = zipParameters.getIncludeEntryPredicate();
        if (inclusionPredicate != null) {
            inputStream = inputStream.filter(inclusionPredicate);
        }

        try {
        inputStream.forEach((entry) -> this.UnzipEntry(inputZipFile, destination, entry, zipParameters));
        } catch (InternalException intExc) {
            if (intExc.getCause() instanceof IOException) {
                throw (IOException) intExc.getCause();
            }

            throw new IOException(intExc.getMessage(), intExc.getCause());
        }
    }

    @Override
    public void createZip(File zipFile, File source) throws IOException {
        try (OutputStream outputStream = new FileOutputStream(zipFile)) {
            try (ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream)) {
                this.addToZip(zipOutputStream, source, source);
            }
        }
    }

    @Override
    public List<? extends ZipEntry> readZipEntryList(File zipFile) throws IOException {
        ZipFile reader = new ZipFile(zipFile);
        List<? extends ZipEntry> result = Collections.list(reader.entries());
        reader.close();

        return result;
    }

//========================================
// Internals
//----------------------------------------

    private void UnzipEntry(ZipFile inputZipFile, File destination, ZipEntry entry, ZipParameters zipParameters) {
        File finalLocation = this.resolveEntryLocation(destination, entry, zipParameters);

        this.log.debug("unzip entry: entry-name={}; destination={}; final-location={}", entry.getName(), destination,
                       finalLocation);

        if (finalLocation != null) {
            //entry.getLastAccessTime();
            //entry.getLastModifiedTime();

            if (entry.isDirectory()) {
                if (! finalLocation.exists()) {
                    boolean created = finalLocation.mkdirs();
                    if (!created) {
                        throw new InternalException("failed to create directory " + finalLocation);
                    }
                }
            } else {
                this.checkParentDirectory(finalLocation);

                try (InputStream entryInputStream = inputZipFile.getInputStream(entry)) {
                    try (OutputStream outputStream = new FileOutputStream(finalLocation)) {
                        IOUtils.copy(entryInputStream, outputStream);

                        boolean updated = finalLocation.setLastModified(entry.getLastModifiedTime().toMillis());
                        if (!updated) {
                            this.log.warn("failed to update last modified time on : entry-name={}; location={}",
                                          entry.getName(), finalLocation);
                        }
                    }
                } catch (IOException ioExc) {
                    throw new InternalException(ioExc);
                }
            }
        }
    }

    private void addToZip(ZipOutputStream zipOutputStream, File zipSourceRoot, File fileToZip)
        throws IOException {

        String pathInZip =
            fileToZip
                .getPath()
                .replaceFirst(Pattern.quote(zipSourceRoot.getPath()), "")
            ;

        // Remove leading slashes (yes, this could be done more efficiently - please feel free to provide a patch)
        while (pathInZip.startsWith("/") || pathInZip.startsWith("\\")) {
            pathInZip = pathInZip.substring(1);
        }

        this.log.debug("ZIP file naming: root={}; file-to-zip={}; path-in-zip={}", zipSourceRoot, fileToZip, pathInZip);

        if (fileToZip.isDirectory()) {
            // Don't add the source root directory to the zip
            if (!fileToZip.equals(zipSourceRoot)) {
                // The trailing slash makes this a directory entry, by definition
                ZipEntry dirEntry = new ZipEntry(pathInZip + "/");
                zipOutputStream.putNextEntry(dirEntry);
                zipOutputStream.closeEntry();
            }

            // Recursively add the directory contents
            File[] contents = fileToZip.listFiles();
            if (contents != null) {
                for (File oneDirContent : contents) {
                    this.addToZip(zipOutputStream, zipSourceRoot, oneDirContent);
                }
            }
        } else {
            ZipEntry fileEntry = new ZipEntry(pathInZip);
            zipOutputStream.putNextEntry(fileEntry);
            try (InputStream inputStream = new FileInputStream(fileToZip)) {
                IOUtils.copy(inputStream, zipOutputStream);
            }
            zipOutputStream.closeEntry();
        }
    }

    private void checkParentDirectory(File file) {
        File parent = file.getParentFile();
        if (parent != null) {
            if (!parent.exists()) {
                boolean created = parent.mkdirs();
                if (! created) {
                    throw new InternalException("failed to create directory " + parent);
                }
            }
        }
    }

    private File resolveEntryLocation(File destination, ZipEntry entry, ZipParameters zipParameters) {
        File entryLocation = new File(entry.getName());
        Function<File, File> mapDestinationFunction = zipParameters.getMapDestinationFunction();

        File mappedLocation = entryLocation;
        if (mapDestinationFunction != null) {
            mappedLocation = mapDestinationFunction.apply(entryLocation);
            if (mappedLocation == null) {
                this.log.debug("ignoring entry {}; map destination function returned null", entryLocation);
                return null;
            }
        }

        File finalLocation = new File(destination, mappedLocation.getPath());
        return finalLocation;
    }

    // TBD999: remove temporary
    public static void main(String[] args) {
//        ZipUtilImpl impl = new ZipUtilImpl();
//
//        try {
//            ZipParameters zipParameters = new ZipParameters();
//            zipParameters.setIncludeEntryPredicate((entry) -> (! entry.getName().endsWith("2")));
//            zipParameters.setMapDestinationFunction(
//                (orig) -> new File(
//                    new File(orig.getParentFile(), "xxx"),
//                    orig.getName()));
//            impl.extractZip(new File("/tmp/test.zip"), new File("/tmp/out"), zipParameters);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        ZipUtilImpl impl = new ZipUtilImpl();

        try {
            impl.createZip(new File("/tmp/test2.zip"), new File("/tmp/junk"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//========================================
// Internal Classes
//----------------------------------------

    private static final class InternalException extends RuntimeException {
        public InternalException(String message) {
            super(message);
        }

        public InternalException(Throwable cause) {
            super(cause);
        }
    }
}
