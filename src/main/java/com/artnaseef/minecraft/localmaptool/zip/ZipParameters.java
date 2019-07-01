package com.artnaseef.minecraft.localmaptool.zip;

import java.io.File;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;

/**
 * Created by art on 7/1/19.
 */
public class ZipParameters {
    public static final ZipParameters DEFAULT = new ZipParameters();

    private Predicate<ZipEntry> includeEntryPredicate;
    private Function<File, File> mapDestinationFunction;

    public Predicate<ZipEntry> getIncludeEntryPredicate() {
        return includeEntryPredicate;
    }

    public void setIncludeEntryPredicate(Predicate<ZipEntry> includeEntryPredicate) {
        this.includeEntryPredicate = includeEntryPredicate;
    }

    public Function<File, File> getMapDestinationFunction() {
        return mapDestinationFunction;
    }

    public void setMapDestinationFunction(Function<File, File> mapDestinationFunction) {
        this.mapDestinationFunction = mapDestinationFunction;
    }
}
