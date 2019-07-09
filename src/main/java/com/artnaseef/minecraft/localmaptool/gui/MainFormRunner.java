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

package com.artnaseef.minecraft.localmaptool.gui;

import com.artnaseef.minecraft.localmaptool.MinecraftEnvironmentType;
import com.artnaseef.minecraft.localmaptool.scan.MapLocationScanner;
import com.artnaseef.minecraft.localmaptool.util.DockIconUtil;
import com.artnaseef.minecraft.localmaptool.zip.ZipParameters;
import com.artnaseef.minecraft.localmaptool.zip.ZipUtil;
import com.artnaseef.minecraft.localmaptool.zip.impl.ZipUtilImpl;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.PatternLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;

/**
 * TODO: add some form of progress display TODO: asynchronous operation of zip/unzip (so display is not frozen)
 *
 * Created by art on 6/28/2019.
 */
public class MainFormRunner {

    public static final String DEFAULT_LOG_FILE_NAME = "minecraft-local-map-tool.log";

    private static Logger DEFAULT_LOGGER = LoggerFactory.getLogger(MainFormRunner.class);
    private static DockIconUtil dockIconUtil = new DockIconUtil();

    private Logger log = DEFAULT_LOGGER;

    private JLabel windowsJavaMapsLabel;
    private JList windowsJavaMapList;
    private JLabel windowsNativeMapsLabel;
    private JList windowsNativeMapList;
    private JButton closeButton;
    private JButton scanButton;
    private JPanel rootPanel;
    private JList macMapList;
    private JButton zipButton;
    private JButton unzipButton;
    private JButton zipButton1;
    private JButton unzipButton1;
    private JButton zipButton2;
    private JButton unzipButton2;
    private JScrollPane windowsJavaMapListScrollPane;
    private JPanel windowsJavaMapPanel;
    private JPanel macMapPanel;
    private JPanel windowsNativeMapPanel;
    private JCheckBox showWindowsJavaCheckBox;
    private JCheckBox showWindowsNativeCheckBox;
    private JCheckBox showMacCheckBox;
    private JCheckBox showLinuxCheckBox;
    private JList linuxMapList;
    private JButton zipLinuxButton;
    private JButton unzipLinuxButton;
    private JPanel linuxMapPanel;

    private MapLocationScanner mapLocationScanner = new MapLocationScanner();
    private ZipUtil zipUtil = new ZipUtilImpl();

    public MainFormRunner() {
        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        scanButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO: run asynchronously
                MainFormRunner.this.startScan();
            }
        });
        zipButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MainFormRunner.this.startZipMac();
            }
        });
        unzipButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MainFormRunner.this.startUnzipMac();
            }
        });
        zipButton1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MainFormRunner.this.startZipWinNative();
            }
        });
        unzipButton1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MainFormRunner.this.startUnzipWinNative();
            }
        });
        zipButton2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MainFormRunner.this.startZipWinJava();
            }
        });
        unzipButton2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MainFormRunner.this.startUnzipWinJava();
            }
        });

        showWindowsJavaCheckBox.addActionListener(
            e -> windowsJavaMapPanel.setVisible(showWindowsJavaCheckBox.isSelected()));

        showWindowsNativeCheckBox.addActionListener(
            e -> windowsNativeMapPanel.setVisible(showWindowsNativeCheckBox.isSelected()));

        showMacCheckBox.addActionListener(
            e -> macMapPanel.setVisible(showMacCheckBox.isSelected()));

        zipLinuxButton.addActionListener(
            e -> MainFormRunner.this.startZipLinux());

        unzipLinuxButton.addActionListener(
            e -> MainFormRunner.this.startUnzipLinux());

        showLinuxCheckBox.addActionListener(
            e -> linuxMapPanel.setVisible(showLinuxCheckBox.isSelected()));
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Minecraft Local Map Tool");

        URL url = MainFormRunner.class.getResource("/icons/MinecraftLocalMapToolIcon.png");
        if (url != null) {
            Image icon = Toolkit.getDefaultToolkit().getImage(url);
            frame.setIconImage(icon);
            dockIconUtil.setDockIcon(icon);
        }

        // TODO: better command-line argument handling
        if (args.length > 0) {
            String logFileName = null;

            if (args[0].startsWith("--logfile=")) {
                logFileName = args[0].substring(10).trim();
            } else if (args[0].startsWith("--log-to-file")) {
                logFileName = DEFAULT_LOG_FILE_NAME;
            }

            if (!logFileName.isEmpty()) {
                try {
                    Layout layout = new PatternLayout("%d{ISO8601} %-5p [%t] %c - %m%n");
                    FileAppender fileAppender = new FileAppender(layout, logFileName, true);
                    org.apache.log4j.Logger.getRootLogger().addAppender(fileAppender);
                } catch (IOException ioExc) {
                    ioExc.printStackTrace();
                    System.exit(3);
                }
            }
        }

        MainFormRunner runner = new MainFormRunner();
        runner.init();

        frame.setContentPane(runner.rootPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    public void init() {
        this.mapLocationScanner.init();

        //
        // Initially, only show the environments for which we know the save folder, and it exists locally.
        //

        if (!this.folderExists(this.mapLocationScanner.getMacJavaSaveFolder())) {
            this.macMapPanel.setVisible(false);
            this.showMacCheckBox.setSelected(false);
        }

        if (!this.folderExists(this.mapLocationScanner.getWindowsJavaSaveFolder())) {
            this.windowsJavaMapPanel.setVisible(false);
            this.showWindowsJavaCheckBox.setSelected(false);
        }

        if (!this.folderExists(this.mapLocationScanner.getWindowsNativeSaveFolder())) {
            this.windowsNativeMapPanel.setVisible(false);
            this.showWindowsNativeCheckBox.setSelected(false);
        }

        if (!this.folderExists(this.mapLocationScanner.getLinuxJavaSaveFolder())) {
            this.linuxMapPanel.setVisible(false);
            this.showLinuxCheckBox.setSelected(false);
        }
    }

    private boolean folderExists(File folder) {
        if (folder != null) {
            return folder.exists();
        }

        return false;
    }

    private void startScan() {
        this.mapLocationScanner.setOnScanComplete(this::onScanComplete);

        // TBD: use a thread executor
        Thread scanRunner = new Thread(this.mapLocationScanner::executeScan);
        scanRunner.start();
    }

    private void onScanComplete(MapLocationScanner scanner) {
        System.out.println("SCAN COMPLETE");

        this.fillMapList(this.windowsJavaMapList, scanner.getMatchedForEnvironment(MinecraftEnvironmentType.WIN_JAVA));
        this.fillMapList(this.windowsNativeMapList,
                         scanner.getMatchedForEnvironment(MinecraftEnvironmentType.WIN_NATIVE));
        this.fillMapList(this.macMapList, scanner.getMatchedForEnvironment(MinecraftEnvironmentType.MAC));
        this.fillMapList(this.linuxMapList, scanner.getMatchedForEnvironment(MinecraftEnvironmentType.LINUX));
    }

    private void fillMapList(JList listControl, List<File> contents) {
        DefaultListModel<String> model = new DefaultListModel<String>();
        if (contents != null) {
            for (File oneEle : contents) {
                model.addElement(oneEle.getPath());
            }
        }

        listControl.setModel(model);
    }

    private void startZipLinux() {
        this.startZipGeneric("Linux", this.linuxMapList);
    }

    private void startZipMac() {
        this.startZipGeneric("Mac", this.macMapList);
    }

    private void startZipWinJava() {
        this.startZipGeneric("Windows Java", this.windowsJavaMapList);
    }

    private void startZipWinNative() {
        this.startZipGeneric("Windows Native", this.windowsNativeMapList);
    }

    private void startZipGeneric(String envName, JList listControl) {
        ListModel model = listControl.getModel();
        if ((model != null) && (model.getSize() > 0)) {
            int[] selectedIndexes = listControl.getSelectedIndices();

            if ((selectedIndexes != null) && (selectedIndexes.length > 0)) {
                for (int oneSelectedIndex : selectedIndexes) {
                    String entry = (String) model.getElementAt(oneSelectedIndex);
                    this.startZipPath(envName, entry);
                }
            } else {
                System.out.println("PLEASE SELECT AN ENTRY FIRST");
            }
        } else {
            this.log.info("EMPTY LIST - please choose at least one map to zip first.");
            JOptionPane.showMessageDialog(this.rootPanel, "Please select at least one map first", "No Map Selected",
                                          JOptionPane.WARNING_MESSAGE);
        }
    }

    private void startUnzipLinux() {
        File directory = this.mapLocationScanner.getLinuxJavaSaveFolder();
        this.startUnzipGeneric("Linux", directory);
    }

    private void startUnzipMac() {
        File directory = this.mapLocationScanner.getMacJavaSaveFolder();
        this.startUnzipGeneric("Mac", directory);

    }

    private void startUnzipWinJava() {
        File directory = this.mapLocationScanner.getWindowsJavaSaveFolder();
        this.startUnzipGeneric("Windows Java", directory);
    }

    private void startUnzipWinNative() {
        File directory = this.mapLocationScanner.getWindowsNativeSaveFolder();
        this.startUnzipGeneric("Windows Native", directory);
    }

    private void startUnzipGeneric(String envName, File saveFolderPath) {

        //
        // Choose the ZIP source
        //
        File sourceZip = this.chooseSource(envName);

        if (sourceZip != null) {
            //
            // Choose the save location
            //
            File destination = this.chooseDestinationFolder(envName + ": ", saveFolderPath);

            if (destination != null) {
                boolean doUnzip = false;

                if (destination.exists()) {
                    String[] contents = destination.list();
                    if ((contents == null) || (contents.length == 0)) {
                        // Empty destination; all good
                        doUnzip = true;
                    } else {
                        int confirmResult =
                            JOptionPane.showConfirmDialog(
                                this.rootPanel,
                                "Destination directory is not empty; existing contents will be overwritten and/or mixed with the new files.  Are you sure?",
                                "Non-empty Destination Directory Confirmation", JOptionPane.YES_NO_OPTION
                            );

                        if (confirmResult == JOptionPane.YES_OPTION) {
                            // Confirmed - continue with the unzip
                            doUnzip = true;
                        }
                    }
                } else {
                    boolean created = destination.mkdir();
                    if (created) {
                        doUnzip = true;
                    } else {
                        JOptionPane
                            .showMessageDialog(this.rootPanel, "Failed to create destination directory", "Unzip Error",
                                               JOptionPane.ERROR_MESSAGE);
                    }
                }

                if (doUnzip) {
                    try {
                        File chosenLevelDat = this.selectMapRootInZip(sourceZip);

                        if (chosenLevelDat != null) {
                            File levelDatParent = chosenLevelDat.getParentFile();
                            ZipParameters zipParameters;

                            // If the level.dat file is at the top level of the zip, skip remapping and filtering
                            if (levelDatParent == null) {
                                this.log.debug("chosen source root = zip root");
                                zipParameters = ZipParameters.DEFAULT;
                            } else {
                                this.log.debug("chosen source root = {}", levelDatParent);
                                zipParameters = this.prepareExtractionParameters(levelDatParent);
                            }

                            this.zipUtil.extractZip(sourceZip, destination, zipParameters);

                            this.startScan();
                        }
                    } catch (IOException ioExc) {
                        JOptionPane
                            .showMessageDialog(this.rootPanel,
                                               "Error occured during the unzip process: " + ioExc.getMessage(),
                                               "Unzip Error",
                                               JOptionPane.ERROR_MESSAGE);

                        this.log.error("Error during the unzip process", ioExc);
                    }
                }
            }
        }
    }

    private ZipParameters prepareExtractionParameters(File rootInZip) {
        ZipParameters result = new ZipParameters();

        result.setIncludeEntryPredicate((entry) -> this.directoryContainsFilePredicate(rootInZip, entry.getName()));
        result.setMapDestinationFunction((file) -> this.determineRelativePath(rootInZip, file));

        return result;
    }

    private boolean directoryContainsFilePredicate(File directory, String entryName) {
        String directoryPath = directory.getPath();

        return (entryName.startsWith(directoryPath) && (!entryName.equals(directoryPath)));
    }

    private File determineRelativePath(File root, File entry) {
        String result = entry.getPath().replaceFirst(Pattern.quote(root.getPath()), "");

        return new File(result);
    }

    private File chooseSource(String envName) {
        JFileChooser zipSourceChooser = new JFileChooser();
        zipSourceChooser.setMultiSelectionEnabled(false);
        FileNameExtensionFilter zipFilter = new FileNameExtensionFilter("ZIP files", "zip");

        zipSourceChooser.addChoosableFileFilter(zipFilter);
        zipSourceChooser.resetChoosableFileFilters();
        zipSourceChooser.setFileFilter(zipFilter);

        zipSourceChooser.setDialogTitle(envName + ": Choose source ZIP file");
        int sourceResult = zipSourceChooser.showOpenDialog(this.rootPanel);

        if (sourceResult == JFileChooser.APPROVE_OPTION) {
            return zipSourceChooser.getSelectedFile();
        }

        return null;
    }

    private File chooseDestinationFolder(String titlePrefix, File startingSaveFolderPath) {
        JFileChooser saveFolderChooser = new JFileChooser(startingSaveFolderPath);

        File suggestedDestination = new File(startingSaveFolderPath, "NewImportedWorld");
        saveFolderChooser.setSelectedFile(suggestedDestination);
        saveFolderChooser.setMultiSelectionEnabled(false);
        saveFolderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        saveFolderChooser.setDialogTitle(titlePrefix + "Choose destination folder");
        int destinationResult = saveFolderChooser.showSaveDialog(this.rootPanel);

        if (destinationResult == JFileChooser.APPROVE_OPTION) {
            return saveFolderChooser.getSelectedFile();
        }

        return null;
    }

    private File selectMapRootInZip(File zipFile) throws IOException {
        List<? extends ZipEntry> zipEntryList = this.zipUtil.readZipEntryList(zipFile);
        List<? extends ZipEntry> levelDatFiles =
            zipEntryList
                .stream()
                .filter(this::zipEntryIsLevelDatFile)
                .collect(Collectors.toList());

        ZipEntry resultEntry = null;

        if (!levelDatFiles.isEmpty()) {
            if (levelDatFiles.size() == 1) {
                resultEntry = levelDatFiles.get(0);
            } else {
                String[] selections =
                    levelDatFiles
                        .stream()
                        .map(ZipEntry::getName)
                        .toArray((size) -> new String[size]);

                String result = (String) JOptionPane.showInputDialog(
                    this.rootPanel,
                    "Found more than one map in the ZIP file; please choose the source Map folder to use",
                    "Choose Source Map Folder",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    selections,
                    selections[0]);

                if (result != null) {
                    return new File(result);
                }
            }
        } else {
            this.log.warn("Chosen source ZIP file does not contain any level.dat files; aborting unzip");
            JOptionPane.showMessageDialog(this.rootPanel,
                                          "The selected ZIP file does not contain a level.data file; aborting",
                                          "Missing level.dat",
                                          JOptionPane.ERROR_MESSAGE);
        }

        if (resultEntry != null) {
            return new File(resultEntry.getName());
        }

        return null;
    }

    private boolean zipEntryIsLevelDatFile(ZipEntry zipEntry) {
        File path = new File(zipEntry.getName());

        if (path != null) {
            return path.getName().equalsIgnoreCase("level.dat");
        }

        return false;
    }

    private void startZipPath(String envName, String path) {
        File directory = new File(path);
        String name = directory.getName();

        File zipFilePath = new File(directory.getParent(), name + ".zip");

        JFileChooser fileChooser = new JFileChooser(directory);
        fileChooser.setSelectedFile(zipFilePath);
        fileChooser.setDialogTitle(envName + ": Choose the destination for the zip file");
        int result = fileChooser.showSaveDialog(this.rootPanel);

        if (result == JFileChooser.APPROVE_OPTION) {
            zipFilePath = fileChooser.getSelectedFile();

            try {
                this.zipUtil.createZip(zipFilePath, directory);
            } catch (Exception exc) {
                // TODO: better handling
                exc.printStackTrace();
            }
        }
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer >>> IMPORTANT!! <<< DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        rootPanel = new JPanel();
        rootPanel.setLayout(new GridLayoutManager(20, 5, new Insets(2, 2, 2, 2), -1, -1));
        rootPanel.setMinimumSize(new Dimension(600, 400));
        rootPanel.setPreferredSize(new Dimension(600, 400));
        rootPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4), null));
        windowsJavaMapsLabel = new JLabel();
        windowsJavaMapsLabel.setText("Windows Java Maps");
        rootPanel.add(windowsJavaMapsLabel,
                      new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                                          GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null,
                                          new Dimension(150, -1), null, 0, false));
        windowsNativeMapsLabel = new JLabel();
        windowsNativeMapsLabel.setText("Windows Native Maps");
        rootPanel.add(windowsNativeMapsLabel,
                      new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                                          GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null,
                                          new Dimension(150, -1), null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Mac Maps");
        rootPanel.add(label1, new GridConstraints(10, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                                                  GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED,
                                                  null, null, null, 0, false));
        windowsJavaMapPanel = new JPanel();
        windowsJavaMapPanel.setLayout(new GridLayoutManager(3, 2, new Insets(0, 0, 0, 0), -1, -1));
        rootPanel.add(windowsJavaMapPanel,
                      new GridConstraints(1, 0, 3, 5, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                                          GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                          GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                          null, null, null, 0, false));
        windowsJavaMapListScrollPane = new JScrollPane();
        windowsJavaMapPanel.add(windowsJavaMapListScrollPane,
                                new GridConstraints(0, 0, 3, 1, GridConstraints.ANCHOR_CENTER,
                                                    GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK
                                                                               | GridConstraints.SIZEPOLICY_WANT_GROW,
                                                    GridConstraints.SIZEPOLICY_CAN_SHRINK
                                                    | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0,
                                                    false));
        windowsJavaMapList = new JList();
        windowsJavaMapListScrollPane.setViewportView(windowsJavaMapList);
        zipButton2 = new JButton();
        zipButton2.setText("Zip...");
        windowsJavaMapPanel.add(zipButton2, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_NORTH,
                                                                GridConstraints.FILL_HORIZONTAL,
                                                                GridConstraints.SIZEPOLICY_CAN_SHRINK
                                                                | GridConstraints.SIZEPOLICY_CAN_GROW,
                                                                GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0,
                                                                false));
        unzipButton2 = new JButton();
        unzipButton2.setText("Unzip...");
        windowsJavaMapPanel.add(unzipButton2, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_NORTH,
                                                                  GridConstraints.FILL_HORIZONTAL,
                                                                  GridConstraints.SIZEPOLICY_CAN_SHRINK
                                                                  | GridConstraints.SIZEPOLICY_CAN_GROW,
                                                                  GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0,
                                                                  false));
        final Spacer spacer1 = new Spacer();
        windowsJavaMapPanel.add(spacer1, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER,
                                                             GridConstraints.FILL_VERTICAL, 1,
                                                             GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0,
                                                             false));
        macMapPanel = new JPanel();
        macMapPanel.setLayout(new GridLayoutManager(3, 2, new Insets(0, 0, 0, 0), -1, -1));
        rootPanel.add(macMapPanel,
                      new GridConstraints(11, 0, 4, 5, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                                          GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                          GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                          null, null, null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        macMapPanel.add(scrollPane1,
                        new GridConstraints(0, 0, 3, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                                            GridConstraints.SIZEPOLICY_CAN_SHRINK
                                            | GridConstraints.SIZEPOLICY_WANT_GROW,
                                            GridConstraints.SIZEPOLICY_CAN_SHRINK
                                            | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        macMapList = new JList();
        scrollPane1.setViewportView(macMapList);
        zipButton = new JButton();
        zipButton.setText("Zip...");
        zipButton.setToolTipText("Create a ZIP file for the selected map");
        macMapPanel.add(zipButton,
                        new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_HORIZONTAL,
                                            GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                            GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        unzipButton = new JButton();
        unzipButton.setText("Unzip...");
        unzipButton.setToolTipText("Install map from ZIP file into local saves (Mac)");
        macMapPanel.add(unzipButton,
                        new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_HORIZONTAL,
                                            GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                            GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        macMapPanel.add(spacer2,
                        new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1,
                                            GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        windowsNativeMapPanel = new JPanel();
        windowsNativeMapPanel.setLayout(new GridLayoutManager(3, 2, new Insets(0, 0, 0, 0), -1, -1));
        rootPanel.add(windowsNativeMapPanel,
                      new GridConstraints(6, 0, 3, 5, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                                          GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                          GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                          null, null, null, 0, false));
        final JScrollPane scrollPane2 = new JScrollPane();
        windowsNativeMapPanel.add(scrollPane2, new GridConstraints(0, 0, 3, 1, GridConstraints.ANCHOR_CENTER,
                                                                   GridConstraints.FILL_BOTH,
                                                                   GridConstraints.SIZEPOLICY_CAN_SHRINK
                                                                   | GridConstraints.SIZEPOLICY_WANT_GROW,
                                                                   GridConstraints.SIZEPOLICY_CAN_SHRINK
                                                                   | GridConstraints.SIZEPOLICY_WANT_GROW, null,
                                                                   new Dimension(705, 128), null, 0, false));
        windowsNativeMapList = new JList();
        scrollPane2.setViewportView(windowsNativeMapList);
        zipButton1 = new JButton();
        zipButton1.setText("Zip...");
        windowsNativeMapPanel.add(zipButton1, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_NORTH,
                                                                  GridConstraints.FILL_HORIZONTAL,
                                                                  GridConstraints.SIZEPOLICY_CAN_SHRINK
                                                                  | GridConstraints.SIZEPOLICY_CAN_GROW,
                                                                  GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0,
                                                                  false));
        unzipButton1 = new JButton();
        unzipButton1.setText("Unzip...");
        windowsNativeMapPanel.add(unzipButton1, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_NORTH,
                                                                    GridConstraints.FILL_HORIZONTAL,
                                                                    GridConstraints.SIZEPOLICY_CAN_SHRINK
                                                                    | GridConstraints.SIZEPOLICY_CAN_GROW,
                                                                    GridConstraints.SIZEPOLICY_FIXED, null, null, null,
                                                                    0, false));
        final Spacer spacer3 = new Spacer();
        windowsNativeMapPanel.add(spacer3, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER,
                                                               GridConstraints.FILL_VERTICAL, 1,
                                                               GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null,
                                                               0, false));
        closeButton = new JButton();
        closeButton.setText("Close");
        rootPanel.add(closeButton,
                      new GridConstraints(19, 3, 1, 2, GridConstraints.ANCHOR_NORTHEAST, GridConstraints.FILL_NONE,
                                          GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW,
                                          GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer4 = new Spacer();
        rootPanel.add(spacer4,
                      new GridConstraints(19, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                                          GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, new Dimension(284, 11), null, 0,
                                          false));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        rootPanel.add(panel1, new GridConstraints(19, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                                                  GridConstraints.SIZEPOLICY_CAN_SHRINK
                                                  | GridConstraints.SIZEPOLICY_CAN_GROW,
                                                  GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        scanButton = new JButton();
        scanButton.setText("Scan");
        panel1.add(scanButton,
                   new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_HORIZONTAL,
                                       GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                       GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer5 = new Spacer();
        panel1.add(spacer5,
                   new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                                       GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JSeparator separator1 = new JSeparator();
        separator1.setBackground(new Color(-16777216));
        separator1.setEnabled(true);
        separator1.setForeground(new Color(-16777216));
        rootPanel.add(separator1,
                      new GridConstraints(4, 0, 1, 5, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                                          GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null,
                                          new Dimension(0, 12), null, 0, false));
        final JSeparator separator2 = new JSeparator();
        separator2.setBackground(new Color(-16777216));
        separator2.setForeground(new Color(-16777216));
        rootPanel.add(separator2,
                      new GridConstraints(15, 0, 1, 5, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                                          GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null,
                                          null, null, 0, false));
        final JSeparator separator3 = new JSeparator();
        separator3.setBackground(new Color(-16777216));
        separator3.setForeground(new Color(-16777216));
        rootPanel.add(separator3,
                      new GridConstraints(9, 0, 1, 5, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                                          GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null,
                                          null, null, 0, false));
        showWindowsJavaCheckBox = new JCheckBox();
        showWindowsJavaCheckBox.setSelected(true);
        showWindowsJavaCheckBox.setText("Show");
        rootPanel.add(showWindowsJavaCheckBox,
                      new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                                          GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                          GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        showWindowsNativeCheckBox = new JCheckBox();
        showWindowsNativeCheckBox.setSelected(true);
        showWindowsNativeCheckBox.setText("Show");
        rootPanel.add(showWindowsNativeCheckBox,
                      new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                                          GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                          GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        showMacCheckBox = new JCheckBox();
        showMacCheckBox.setSelected(true);
        showMacCheckBox.setText("Show");
        rootPanel.add(showMacCheckBox,
                      new GridConstraints(10, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                                          GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                          GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer6 = new Spacer();
        rootPanel.add(spacer6,
                      new GridConstraints(19, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                                          GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Linux Maps");
        rootPanel.add(label2, new GridConstraints(16, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                                                  GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED,
                                                  null, null, null, 0, false));
        showLinuxCheckBox = new JCheckBox();
        showLinuxCheckBox.setSelected(true);
        showLinuxCheckBox.setText("Show");
        rootPanel.add(showLinuxCheckBox,
                      new GridConstraints(16, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                                          GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                          GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        linuxMapPanel = new JPanel();
        linuxMapPanel.setLayout(new GridLayoutManager(3, 2, new Insets(0, 0, 0, 0), -1, -1));
        rootPanel.add(linuxMapPanel,
                      new GridConstraints(17, 0, 2, 5, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                                          GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                          GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                          null, null, null, 0, false));
        final JScrollPane scrollPane3 = new JScrollPane();
        linuxMapPanel.add(scrollPane3,
                          new GridConstraints(0, 0, 3, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                                              GridConstraints.SIZEPOLICY_CAN_SHRINK
                                              | GridConstraints.SIZEPOLICY_WANT_GROW,
                                              GridConstraints.SIZEPOLICY_CAN_SHRINK
                                              | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        linuxMapList = new JList();
        scrollPane3.setViewportView(linuxMapList);
        zipLinuxButton = new JButton();
        zipLinuxButton.setText("Zip...");
        linuxMapPanel.add(zipLinuxButton,
                          new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_HORIZONTAL,
                                              GridConstraints.SIZEPOLICY_CAN_SHRINK
                                              | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED,
                                              null, null, null, 0, false));
        unzipLinuxButton = new JButton();
        unzipLinuxButton.setText("Unzip...");
        linuxMapPanel.add(unzipLinuxButton,
                          new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_HORIZONTAL,
                                              GridConstraints.SIZEPOLICY_CAN_SHRINK
                                              | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED,
                                              null, null, null, 0, false));
        final Spacer spacer7 = new Spacer();
        linuxMapPanel.add(spacer7,
                          new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL,
                                              1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return rootPanel;
    }
}
