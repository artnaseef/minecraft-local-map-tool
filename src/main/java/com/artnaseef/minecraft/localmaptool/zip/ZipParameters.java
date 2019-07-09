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
