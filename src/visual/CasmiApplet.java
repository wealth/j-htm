package visual;

import HTM.Column;
import HTM.Cortex;
import casmi.Applet;
import casmi.AppletRunner;
import casmi.KeyEvent;
import casmi.MouseButton;
import casmi.MouseEvent;
import casmi.graphics.Graphics;
import casmi.graphics.color.RGBColor;
import casmi.graphics.element.Circle;
import casmi.graphics.element.Sphere;
import casmi.graphics.element.Triangle;

import java.util.ArrayList;

public class CasmiApplet extends Applet {
    private static Cortex crtx;

    public static void launch(Cortex cortex)
    {
        crtx = cortex;
        AppletRunner.run("visual.CasmiApplet", "3D View");
    }

    ArrayList<Circle> cells = new ArrayList<Circle>();

    @Override
    public void setup() {
        setFPS(60);
        setSize(800, 600);
        for (int i = 0; i < crtx.xDimension/2; i++) {
            for (int j = 0; j < crtx.yDimension/2; j++) {
                for (int k = 0; k < crtx.cellsPerColumn/2; k++) {
                    Circle s = new Circle(30);
                    s.setFillColor(new RGBColor(0.25, 0.25, 0.9, 0.7));
                    s.setStrokeColor(new RGBColor(0.25, 0.9, 0.25, 1.0));
                    s.setPosition((i*30)+200, (j*30)+150, (k*30));
                    addObject(s);
                    cells.add(s);
                }
            }
        }
    }

    @Override
    public void update() {
        setRotation(crtx.totalTime, 1, 0, 0);
    }

   @Override
    public void mouseEvent(MouseEvent e, MouseButton b) {
        boolean mouseFlag = false;

        switch (e) {
            case CLICKED:
                //System.out.println("Clicked!!");
                mouseFlag = true;
                break;
            case PRESSED:
                //System.out.println("Pressed!!");
                mouseFlag = true;
                break;
            case RELEASED:
                //System.out.println("Released!!");
                mouseFlag = true;
                break;
        }

        if (mouseFlag) {
            switch (b) {
                case LEFT:
                    //System.out.println(": Left");
                    break;
                case RIGHT:
                    //System.out.println(": Right");
                    break;
            }
        }
    }

    @Override
    public void keyEvent(KeyEvent e) {}
}
