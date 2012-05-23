
import JMyron.*;
import info.monitorenter.gui.chart.Chart2D;
import info.monitorenter.gui.chart.ITrace2D;
import info.monitorenter.gui.chart.traces.Trace2DSimple;
import info.monitorenter.gui.chart.traces.painters.TracePainterDisc;

import javax.sound.sampled.*;
import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.File;


/**
 * Created by IntelliJ IDEA.
 * User: soil
 * Date: 3/4/12
 * Time: 5:21 PM
 * To change this template use File | Settings | File Templates.
 */
public class HTMConfiguration {
    private JTextField textField1;
    private JTextField textField2;
    private JPanel mainPanel;
    private JButton runCortexButton;
    private JTextField textField3;
    private JTextField textField4;
    private JTextField textField5;
    private JTextField textField6;
    private JTextField textField7;
    private JTextField textField8;
    private JTextField textField9;
    private JTextField textField10;
    private JTextField textField11;
    private JTextField textField12;
    public JTextPane textPane1;
    private Chart2D chart2D1;
    private JButton stopCortexButton;
    public JCheckBox showDendritesGraphCheckBox;
    public JCheckBox showSynapsesPermanenceCheckBox;
    public JCheckBox showActiveCellsCheckBox;
    public JCheckBox showPredictiveCellsCheckBox;
    public JCheckBox showLearningCellsCheckBox;
    public JCheckBox showOverlapsCheckBox;
    public JCheckBox showActiveDutyCycleCheckBox;
    public JCheckBox showMinDutyCycleCheckBox;
    public JCheckBox showBoostCheckBox;
    public JCheckBox showOverlapsDutyCycleCheckBox;
    private JButton makeStepButton;
    private JButton showExtendedGUIButton;
    public JCheckBox inputsGraphicsCheckBox;
    private JTabbedPane tabbedPane1;

    CortexThread crtx = new CortexThread();
    static HTMConfiguration panel;

    public HTMConfiguration () {
        runCortexButton.addActionListener(new RunCortexButtonListener());
        stopCortexButton.addActionListener(new StopCortexButtonListener());
        makeStepButton.addActionListener(new MakeStepButtonListener());
        showExtendedGUIButton.addActionListener(new ShowExtendedGUIListener());
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("HTMConfiguration");
        panel = new HTMConfiguration();
        frame.setContentPane(panel.mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    public void InitCortex() {
        crtx = new CortexThread();
        try {
            crtx.region.desiredLocalActivity = new Integer(textField1.getText());
            crtx.region.minOverlap = new Integer(textField2.getText());
            crtx.region.connectedPerm = new Double(textField3.getText());
            crtx.region.permanenceInc = new Double(textField4.getText());
            crtx.region.permanenceDec = new Double(textField5.getText());
            crtx.region.cellsPerColumn = new Integer(textField6.getText());
            crtx.region.activationThreshold = new Integer(textField7.getText());
            crtx.region.initialPerm = new Double(textField8.getText());
            crtx.region.minThreshold = new Integer(textField9.getText());
            crtx.region.newSynapseCount = new Integer(textField10.getText());
            crtx.region.xDimension = new Integer(textField11.getText());
            crtx.region.yDimension = new Integer(textField12.getText());
        } catch (Exception e) {
            System.out.print(e.getMessage());
        }
        crtx.Init(chart2D1, panel);
    }

    public class RunCortexButtonListener implements ActionListener {
        public void actionPerformed (ActionEvent event) {
            if (!crtx.isRunning()) {
                InitCortex();
                crtx.start();
            }
            else
                crtx.Continue();
        }
    }

    private class StopCortexButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            crtx.Quit();
        }
    }

    private class MakeStepButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (crtx.isRunning())
                crtx.MakeStep();
            else {
                InitCortex();
                crtx.MakeStep();
            }
        }
    }

    private class ShowExtendedGUIListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            JFrame frame = new JFrame("Extended GUI");
            ExtensionGUI panel = new ExtensionGUI();
            frame.setContentPane(panel.extensionGUI);
            frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
            frame.pack();
            frame.setVisible(true);
        }
    }
}
