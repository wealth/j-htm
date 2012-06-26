package applet;

import edu.emory.mathcs.jtransforms.fft.DoubleFFT_2D;
import info.monitorenter.gui.chart.Chart2D;
import info.monitorenter.gui.chart.events.Trace2DActionAddErrorBarPolicy;
import info.monitorenter.gui.chart.traces.Trace2DSimple;
import info.monitorenter.gui.chart.traces.painters.TracePainterDisc;

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;

public class ExtensionGUI {
    public JPanel extensionGUI;
    private JButton camButton;
    private JButton feedVideoButton;
    private JButton feedExtraButton;
    private Chart2D chart2D1;
    private Boolean runs = false;
    public static byte[] Input;

    public ExtensionGUI() {
        camButton.addActionListener(new CamButtonListener());
        feedExtraButton.addActionListener(new ExtraButtonListener());
    }

    private class CamButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            ( new Thread() {
                public void run() {
                    AudioFormat format = getAudioFormat();
                    TargetDataLine line;
                    DataLine.Info info = new DataLine.Info(TargetDataLine.class, format); // format is an AudioFormat object
                    if (!AudioSystem.isLineSupported(info)) {
                        System.out.print("Audiostream not supported!\r\n");
                    }
                    // Obtain and open the line.
                    try {
                        line = (TargetDataLine) AudioSystem.getLine(info);
                        line.open(format);
                        // new CaptureThread().start();
                        // Assume that the TargetDataLine, line, has already
                        // been obtained and opened.
                        ByteArrayOutputStream out  = new ByteArrayOutputStream();
                        int numBytesRead = 0;
                        byte[] data = new byte[line.getBufferSize() / 5];

                        System.out.println("Audio running!");
                        // Begin audio capture.
                        line.start();

                        runs = true;

                        Trace2DSimple g = new Trace2DSimple("bytes");
                        //g.setTracePainter(new TracePainterDisc(1));
                        chart2D1.addTrace(g);

                        // Here, stopped is a global boolean set by another thread.
                        while (runs) {
                            // Read the next chunk of data from the TargetDataLine.
                            numBytesRead =  line.read(data, 0, data.length);
                            // Save this chunk of data.
                            // out.write(data, 0, numBytesRead);
                            try {
                                g.removeAllPoints();
                                int i = new AudioInputStream(line).read(data);
                                ExtensionGUI.Input = data;
                                for (int j = 0; j < numBytesRead; j++)
                                    if (j % 100 == 0)
                                        g.addPoint(j/(float)i, data[j]);
                            } catch (Exception ex) {
                                System.out.print(ex.getMessage());
                            }
                        }
                        ExtensionGUI.Input = null;
                        chart2D1.removeAllTraces();
                        line.close();
                        System.out.println("Audio finished!");
                    } catch (LineUnavailableException ex) {
                        System.out.print(ex.getMessage());
                    }
                }
            }).start();
        }
    }

    private AudioFormat getAudioFormat(){
        float sampleRate = 8000.0F;
        //8000,11025,16000,22050,44100
        int sampleSizeInBits = 16;
        //8,16
        int channels = 1;
        //1,2
        boolean signed = true;
        //true,false
        boolean bigEndian = false;
        //true,false
        return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
    }

    private class ExtraButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            runs = !runs;
        }
    }
}
