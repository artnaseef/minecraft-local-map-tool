package com.artnaseef.minecraft.localmaptool.gui;

import com.artnaseef.minecraft.localmaptool.MinecraftEnvironmentType;
import com.artnaseef.minecraft.localmaptool.scan.MapLocationScanner;

import javax.swing.*;
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
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Minecraft Local Map Tool");
        frame.setContentPane(new MainFormRunner().rootPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
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
        this.fillMapList(this.windowsNativeMapList, scanner.getMatchedForEnvironment(MinecraftEnvironmentType.WIN_NATIVE));
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

    public void setData(WindowsJavaMapsList data) {
        windowsJavaMapsLabel.setText(data.getWindowsJavaMapsLabel());
        windowsNativeMapsLabel.setText(data.getWindowsNativeMapsLabel());
    }

    public void getData(WindowsJavaMapsList data) {
        data.setWindowsJavaMapsLabel(windowsJavaMapsLabel.getText());
        data.setWindowsNativeMapsLabel(windowsNativeMapsLabel.getText());
    }

    public boolean isModified(WindowsJavaMapsList data) {
        if (windowsJavaMapsLabel.getText() != null ? !windowsJavaMapsLabel.getText().equals(data.getWindowsJavaMapsLabel()) : data.getWindowsJavaMapsLabel() != null)
            return true;
        if (windowsNativeMapsLabel.getText() != null ? !windowsNativeMapsLabel.getText().equals(data.getWindowsNativeMapsLabel()) : data.getWindowsNativeMapsLabel() != null)
            return true;
        return false;
    }
}
