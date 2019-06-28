package com.artnaseef.minecraft.localmaptool.gui;

import com.artnaseef.minecraft.localmaptool.MinecraftEnvironmentType;
import com.artnaseef.minecraft.localmaptool.scan.MapLocationScanner;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;

/**
 * Created by art on 6/28/2019.
 */
public class MainFormRunner {

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
        JFileChooser zipSourceChooser = new JFileChooser();
        zipSourceChooser.setMultiSelectionEnabled(false);
        FileNameExtensionFilter zipFilter = new FileNameExtensionFilter("ZIP files", "zip");

        zipSourceChooser.addChoosableFileFilter(zipFilter);
        zipSourceChooser.resetChoosableFileFilters();
        zipSourceChooser.setFileFilter(zipFilter);

        zipSourceChooser.setDialogTitle("Choose source ZIP file");
        int sourceResult = zipSourceChooser.showOpenDialog(this.rootPanel);

        if (sourceResult == JFileChooser.APPROVE_OPTION) {
            File sourceZip = zipSourceChooser.getSelectedFile();

            //
            // Choose the save location
            //
            JFileChooser saveFolderChooser = new JFileChooser(saveFolderPath);

            File suggestedDestination = new File(saveFolderPath, "NewImportedWorld");
            saveFolderChooser.setSelectedFile(suggestedDestination);
            saveFolderChooser.setMultiSelectionEnabled(false);
            saveFolderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            saveFolderChooser.setDialogTitle("Choose destination folder");
            int destinationResult = saveFolderChooser.showSaveDialog(this.rootPanel);

            if (destinationResult == JFileChooser.APPROVE_OPTION) {
                File destination = saveFolderChooser.getSelectedFile();

                boolean doUnzip = true;
                if (destination.exists()) {
                    String[] contents = destination.list();
                    if ((contents == null) || (contents.length == 0)) {

                    } else {
                        // TODO: Verification dialog instead of guaranteed rejection
                        System.out.println("UNZIP - rejected destination directory as it is not empty");
                    }
                } else {
                    boolean created = destination.mkdir();
                    if (created) {
                        doUnzip = true;
                    } else {
                        System.out.println("UNZIP - FAILED to mkdir for non-existent directory: " + destination);
                    }
                }

                if (doUnzip) {
                    ZipFile zipFile = new ZipFile(sourceZip);
//                    zipFile.getProgressMonitor();

                    try {
                        zipFile.extractAll(destination.getPath());

                        // TODO: refresh the map list
                        this.startScan();
                    } catch (ZipException exc) {
                        exc.printStackTrace();
                    }
                }
            }
        }
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

            ZipFile zipFile = new ZipFile(zipFilePath);

            ZipParameters zipParameters = new ZipParameters();
            zipParameters.setIncludeRootFolder(false);

            try {
                zipFile.addFolder(directory, zipParameters);
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
        rootPanel.setLayout(new GridLayoutManager(10, 4, new Insets(2, 2, 2, 2), -1, -1));
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
        windowsJavaMapList = new JList();
        rootPanel.add(windowsJavaMapList,
                      new GridConstraints(1, 0, 2, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                                          GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_WANT_GROW,
                                          null, new Dimension(150, 50), null, 0, false));
        windowsNativeMapsLabel = new JLabel();
        windowsNativeMapsLabel.setText("Windows Native Maps");
        rootPanel.add(windowsNativeMapsLabel,
                      new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                                          GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null,
                                          new Dimension(150, -1), null, 0, false));
        windowsNativeMapList = new JList();
        rootPanel.add(windowsNativeMapList,
                      new GridConstraints(4, 0, 2, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                                          GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_WANT_GROW,
                                          null, new Dimension(150, 50), null, 0, false));
        closeButton = new JButton();
        closeButton.setText("Close");
        rootPanel.add(closeButton,
                      new GridConstraints(9, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                                          GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                          GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        scanButton = new JButton();
        scanButton.setText("Scan");
        rootPanel.add(scanButton,
                      new GridConstraints(9, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                                          GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                          GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        rootPanel.add(spacer2,
                      new GridConstraints(9, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                                          GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Mac Maps");
        rootPanel.add(label1, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                                                  GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED,
                                                  null, null, null, 0, false));
        macMapList = new JList();
        rootPanel.add(macMapList,
                      new GridConstraints(7, 0, 2, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                                          GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_WANT_GROW,
                                          null, new Dimension(150, 50), null, 0, false));
        zipButton = new JButton();
        zipButton.setText("Zip...");
        zipButton.setToolTipText("Create a ZIP file for the selected map");
        rootPanel.add(zipButton,
                      new GridConstraints(7, 3, 1, 1, GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_HORIZONTAL,
                                          GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                          GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        unzipButton = new JButton();
        unzipButton.setText("Unzip...");
        unzipButton.setToolTipText("Install map from ZIP file into local saves (Mac)");
        rootPanel.add(unzipButton,
                      new GridConstraints(8, 3, 1, 1, GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_HORIZONTAL,
                                          GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                          GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        zipButton1 = new JButton();
        zipButton1.setText("Zip...");
        rootPanel.add(zipButton1,
                      new GridConstraints(4, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                                          GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                          GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        unzipButton1 = new JButton();
        unzipButton1.setText("Unzip...");
        rootPanel.add(unzipButton1,
                      new GridConstraints(5, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
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
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return rootPanel;
    }
}
