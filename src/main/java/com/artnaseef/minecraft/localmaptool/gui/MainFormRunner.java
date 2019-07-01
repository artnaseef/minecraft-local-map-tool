package com.artnaseef.minecraft.localmaptool.gui;

import com.artnaseef.minecraft.localmaptool.MinecraftEnvironmentType;
import com.artnaseef.minecraft.localmaptool.scan.MapLocationScanner;
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
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * TODO: add some form of progress display
 * TODO: asynchronous operation of zip/unzip (so display is not frozen)
 *
 * Created by art on 6/28/2019.
 */
public class MainFormRunner {

    public static final String DEFAULT_LOG_FILE_NAME = "minecraft-local-map-tool.log";

    private static Logger DEFAULT_LOGGER = LoggerFactory.getLogger(MainFormRunner.class);

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
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Minecraft Local Map Tool");

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
        this.startZipGeneric(this.windowsNativeMapList);
    }

    private void startZipMac() {
        this.startZipGeneric(this.macMapList);
    }

    private void startZipWinJava() {
        this.startZipGeneric(this.windowsJavaMapList);
    }

    private void startZipWinNative() {
        this.startZipGeneric(this.windowsNativeMapList);
    }

    private void startZipGeneric(JList listControl) {
        ListModel model = listControl.getModel();
        if ((model != null) && (model.getSize() > 0)) {
            int[] selectedIndexes = listControl.getSelectedIndices();

            if ((selectedIndexes != null) && (selectedIndexes.length > 0)) {
                for (int oneSelectedIndex : selectedIndexes) {
                    String entry = (String) model.getElementAt(oneSelectedIndex);
                    this.startZipPath(entry);
                }
            } else {
                System.out.println("PLEASE SELECT AN ENTRY FIRST");
            }
        } else {
            // TODO: popup - "please scan for maps and select an existing map first"
            System.out.println("EMPTY LIST");
        }
    }

    private void startUnzipLinux() {
        File directory = this.mapLocationScanner.getLinuxJavaSaveFolder();
        this.startUnzipGeneric(directory);
    }

    private void startUnzipMac() {
        File directory = this.mapLocationScanner.getMacJavaSaveFolder();
        this.startUnzipGeneric(directory);

    }

    private void startUnzipWinJava() {
        File directory = this.mapLocationScanner.getWindowsJavaSaveFolder();
        this.startUnzipGeneric(directory);
    }

    private void startUnzipWinNative() {
        File directory = this.mapLocationScanner.getWindowsNativeSaveFolder();
        this.startUnzipGeneric(directory);
    }

    private void startUnzipGeneric(File saveFolderPath) {

        //
        // Choose the ZIP source
        //
        File sourceZip = this.chooseSource();

        if (sourceZip != null) {
            //
            // Choose the save location
            //
            File destination = this.chooseDestinationFolder(saveFolderPath);

            if (destination != null) {
                boolean doUnzip = false;

                if (destination.exists()) {
                    String[] contents = destination.list();
                    if ((contents == null) || (contents.length == 0)) {
                        // Empty destination; all good
                        doUnzip = true;
                    } else {
                        // TODO: Verification dialog instead of guaranteed rejection
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

                            // TODO: refresh the map list
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

    private File chooseSource() {
        JFileChooser zipSourceChooser = new JFileChooser();
        zipSourceChooser.setMultiSelectionEnabled(false);
        FileNameExtensionFilter zipFilter = new FileNameExtensionFilter("ZIP files", "zip");

        zipSourceChooser.addChoosableFileFilter(zipFilter);
        zipSourceChooser.resetChoosableFileFilters();
        zipSourceChooser.setFileFilter(zipFilter);

        zipSourceChooser.setDialogTitle("Choose source ZIP file");
        int sourceResult = zipSourceChooser.showOpenDialog(this.rootPanel);

        if (sourceResult == JFileChooser.APPROVE_OPTION) {
            return zipSourceChooser.getSelectedFile();
        }

        return null;
    }

    private File chooseDestinationFolder(File startingSaveFolderPath) {
        JFileChooser saveFolderChooser = new JFileChooser(startingSaveFolderPath);

        File suggestedDestination = new File(startingSaveFolderPath, "NewImportedWorld");
        saveFolderChooser.setSelectedFile(suggestedDestination);
        saveFolderChooser.setMultiSelectionEnabled(false);
        saveFolderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        saveFolderChooser.setDialogTitle("Choose destination folder");
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

    private void startZipPath(String path) {
        File directory = new File(path);
        String name = directory.getName();

        // TODO: prompt the user for the output location
        File zipFilePath = new File(directory.getParent(), name + ".zip");

        JFileChooser fileChooser = new JFileChooser(directory);
        fileChooser.setSelectedFile(zipFilePath);
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
        rootPanel.setLayout(new GridLayoutManager(14, 4, new Insets(2, 2, 2, 2), -1, -1));
        rootPanel.setMinimumSize(new Dimension(600, 400));
        rootPanel.setPreferredSize(new Dimension(600, 400));
        windowsJavaMapsLabel = new JLabel();
        windowsJavaMapsLabel.setText("Windows Java Maps");
        rootPanel.add(windowsJavaMapsLabel,
                      new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                                          GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null,
                                          new Dimension(150, -1), null, 0, false));
        final Spacer spacer1 = new Spacer();
        rootPanel.add(spacer1,
                      new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                                          GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        windowsNativeMapsLabel = new JLabel();
        windowsNativeMapsLabel.setText("Windows Native Maps");
        rootPanel.add(windowsNativeMapsLabel,
                      new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                                          GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null,
                                          new Dimension(150, -1), null, 0, false));
        closeButton = new JButton();
        closeButton.setText("Close");
        rootPanel.add(closeButton,
                      new GridConstraints(13, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                                          GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                          GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        scanButton = new JButton();
        scanButton.setText("Scan");
        rootPanel.add(scanButton,
                      new GridConstraints(13, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                                          GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                          GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        rootPanel.add(spacer2,
                      new GridConstraints(13, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                                          GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Mac Maps");
        rootPanel.add(label1, new GridConstraints(8, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                                                  GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED,
                                                  null, null, null, 0, false));
        zipButton1 = new JButton();
        zipButton1.setText("Zip...");
        rootPanel.add(zipButton1,
                      new GridConstraints(5, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                                          GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                          GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        unzipButton1 = new JButton();
        unzipButton1.setText("Unzip...");
        rootPanel.add(unzipButton1,
                      new GridConstraints(6, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                                          GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                          GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        zipButton2 = new JButton();
        zipButton2.setText("Zip...");
        rootPanel.add(zipButton2,
                      new GridConstraints(1, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                                          GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                          GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        unzipButton2 = new JButton();
        unzipButton2.setText("Unzip...");
        rootPanel.add(unzipButton2,
                      new GridConstraints(2, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                                          GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                          GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer3 = new Spacer();
        rootPanel.add(spacer3,
                      new GridConstraints(7, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1,
                                          GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final Spacer spacer4 = new Spacer();
        rootPanel.add(spacer4,
                      new GridConstraints(3, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1,
                                          GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        rootPanel.add(scrollPane1,
                      new GridConstraints(9, 0, 4, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                                          GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW,
                                          GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW,
                                          null, null, null, 0, false));
        macMapList = new JList();
        scrollPane1.setViewportView(macMapList);
        final JScrollPane scrollPane2 = new JScrollPane();
        rootPanel.add(scrollPane2,
                      new GridConstraints(5, 0, 3, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                                          GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW,
                                          GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW,
                                          null, null, null, 0, false));
        windowsNativeMapList = new JList();
        scrollPane2.setViewportView(windowsNativeMapList);
        final JScrollPane scrollPane3 = new JScrollPane();
        rootPanel.add(scrollPane3,
                      new GridConstraints(1, 0, 3, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                                          GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW,
                                          GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW,
                                          null, null, null, 0, false));
        windowsJavaMapList = new JList();
        scrollPane3.setViewportView(windowsJavaMapList);
        zipButton = new JButton();
        zipButton.setText("Zip...");
        zipButton.setToolTipText("Create a ZIP file for the selected map");
        rootPanel.add(zipButton,
                      new GridConstraints(9, 3, 1, 1, GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_HORIZONTAL,
                                          GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                          GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        unzipButton = new JButton();
        unzipButton.setText("Unzip...");
        unzipButton.setToolTipText("Install map from ZIP file into local saves (Mac)");
        rootPanel.add(unzipButton,
                      new GridConstraints(10, 3, 1, 1, GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_HORIZONTAL,
                                          GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                          GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer5 = new Spacer();
        rootPanel.add(spacer5,
                      new GridConstraints(11, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1,
                                          GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return rootPanel;
    }
}
