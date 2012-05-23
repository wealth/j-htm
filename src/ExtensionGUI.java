import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;

public class ExtensionGUI {
    public JPanel extensionGUI;
    private JButton camButton;
    private Boolean runs = false;

    public ExtensionGUI() {
        camButton.addActionListener(new CamButtonListener());
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
                        int numBytesRead;
                        byte[] data = new byte[line.getBufferSize() / 5];

                        // Begin audio capture.
                        line.start();

                        // Here, stopped is a global boolean set by another thread.
                        while (runs) {
                            // Read the next chunk of data from the TargetDataLine.
                            // numBytesRead =  line.read(data, 0, data.length);
                            // Save this chunk of data.
                            // out.write(data, 0, numBytesRead);
                            try {
                                int i = new AudioInputStream(line).read(data);
                                System.out.print(data[data.length-1] + "\r\n");
                            } catch (Exception ex) {
                                System.out.print(ex.getMessage());
                            }
                        }
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
}
