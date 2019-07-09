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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by art on 6/27/2019.
 */
public class MinecraftMapDirectoryFinder {
    private static final Logger DEFAULT_LOGGER = LoggerFactory.getLogger(MinecraftMapDirectoryFinder.class);

    private Logger log = DEFAULT_LOGGER;

    public Logger getLog() {
        return log;
    }

    public void setLog(Logger log) {
        this.log = log;
    }

    public List<File> findMaps(File baseFolderFile) {
        this.log.debug("starting scan of folder " + baseFolderFile);
        List<File> matchedFolders = new LinkedList<>();

        File[] contents = baseFolderFile.listFiles();
        if (contents != null) {
            for (File oneCanditate : contents) {
                boolean matches = this.checkPossibleMapFolder(oneCanditate);
                if (matches) {
                    matchedFolders.add(oneCanditate);
                }
            }
        }

        return matchedFolders;
    }

    private boolean checkPossibleMapFolder(File candidate) {
        this.log.debug("checking child " + candidate);

        boolean matches = false;

        if (candidate.isDirectory()) {
            File levelDatFile = new File(candidate, "level.dat");
            if (levelDatFile.exists()) {
                if (levelDatFile.isFile()) {
                    matches = true;
                } else {
                    this.log.debug("> child " + candidate + " contains \"level.dat\" but it does not appear to be a regular file");
                }
            } else {
                this.log.debug("> child " + candidate + " does not contain \"level.dat\"");
            }
        } else {
            this.log.debug("> child " + candidate + " is not a directory");
        }

        return matches;
    }
}
