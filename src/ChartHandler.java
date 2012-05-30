import info.monitorenter.gui.chart.Chart2D;
import info.monitorenter.gui.chart.ITrace2D;
import info.monitorenter.gui.chart.TracePoint2D;
import info.monitorenter.gui.chart.traces.Trace2DSimple;
import info.monitorenter.gui.chart.traces.painters.TracePainterDisc;

import java.awt.*;
import java.util.ArrayList;

public class ChartHandler {
    private Chart2D chart2D1;
    private Chart2D chart2D2;
    private HTMConfiguration cfg;
    Boolean showDistalSegmentsCount = false;
    Boolean perm = false;
    Boolean act = false;
    Boolean predict = false;
    Boolean learn = false;
    Boolean over = false;
    Boolean adc = false;
    Boolean mdc = false;
    Boolean odc = false;
    Boolean bst = false;
    Boolean inp = false;

    ITrace2D traceA = new Trace2DSimple("Activity");
    ITrace2D traceL = new Trace2DSimple("Learn");
    ITrace2D traceP = new Trace2DSimple("Predictive");
    ITrace2D traceD = new Trace2DSimple("Dendrite Segments");
    ITrace2D traceS = new Trace2DSimple("Permanences");
    ITrace2D traceO = new Trace2DSimple("Overlaps");
    ITrace2D traceADC = new Trace2DSimple("Active Duty Cycle");
    ITrace2D traceMDC = new Trace2DSimple("Min Duty Cycle");
    ITrace2D traceODC = new Trace2DSimple("Overlap Duty Cycle");
    ITrace2D traceBST = new Trace2DSimple("Column Boost");
    ITrace2D traceINP = new Trace2DSimple("Inputs Graphic");
    ITrace2D traceTMLN = new Trace2DSimple("Progress in Time");

    public ChartHandler(Chart2D chart1, Chart2D chart2, HTMConfiguration configuration) {
        this.chart2D1 = chart1;
        this.chart2D2 = chart2;
        this.cfg = configuration;
        showDistalSegmentsCount = cfg.showDendritesGraphCheckBox.isSelected();
        perm = cfg.showSynapsesPermanenceCheckBox.isSelected();
        act = cfg.showActiveCellsCheckBox.isSelected();
        predict = cfg.showPredictiveCellsCheckBox.isSelected();
        learn = cfg.showLearningCellsCheckBox.isSelected();
        over = cfg.showOverlapsCheckBox.isSelected();
        adc = cfg.showActiveDutyCycleCheckBox.isSelected();
        mdc = cfg.showMinDutyCycleCheckBox.isSelected();
        odc = cfg.showOverlapsDutyCycleCheckBox.isSelected();
        bst = cfg.showBoostCheckBox.isSelected();
        inp = cfg.inputsGraphicsCheckBox.isSelected();

        this.chart2D1.removeAllTraces();
        //this.chart2D2.removeAllTraces();
        if (act) {
            chart2D1.addTrace(traceA);
            traceA.setColor(Color.CYAN);
            traceA.setTracePainter(new TracePainterDisc(4));
        }
        if (learn) {
            chart2D1.addTrace(traceL);
            traceL.setColor(Color.MAGENTA);
            traceL.setTracePainter(new TracePainterDisc(4));
        }
        if (predict) {
            chart2D1.addTrace(traceP);
            traceP.setColor(Color.BLUE);
            traceP.setTracePainter(new TracePainterDisc(4));
        }
        if (showDistalSegmentsCount) {
            chart2D1.addTrace(traceD);
            traceD.setTracePainter(new TracePainterDisc(4));

            chart2D2.addTrace(traceTMLN);
        }
        if (perm) {
            chart2D1.addTrace(traceS);
            traceS.setColor(Color.BLUE);
            traceS.setTracePainter(new TracePainterDisc(4));
        }
        if (over) {
            chart2D1.addTrace(traceO);
            traceO.setColor(Color.RED);
            traceO.setTracePainter(new TracePainterDisc(4));
        }
        if (adc) {
            chart2D1.addTrace(traceADC);
            traceADC.setColor(Color.GREEN);
            traceADC.setTracePainter(new TracePainterDisc(4));
        }
        if (mdc) {
            chart2D1.addTrace(traceMDC);
            traceMDC.setColor(Color.GRAY);
            traceMDC.setTracePainter(new TracePainterDisc(4));
        }
        if (odc) {
            chart2D1.addTrace(traceODC);
            traceODC.setColor(Color.ORANGE);
            traceODC.setTracePainter(new TracePainterDisc(4));
        }
        if (bst) {
            chart2D1.addTrace(traceBST);
            traceBST.setColor(Color.DARK_GRAY);
            traceBST.setTracePainter(new TracePainterDisc(4));
        }
        if (inp) {
            chart2D1.addTrace(traceINP);
            traceINP.setColor(Color.RED);
            traceINP.setTracePainter(new TracePainterDisc(4));
        }
    }

    public void CollectData() {
        for(ITrace2D trace2D: chart2D1.getTraces()) {
            trace2D.removeAllPoints();
        }
        Integer time = cfg.crtx.region.time - 1 > 0 ? cfg.crtx.region.time - 1 : 0;
        if (inp) {
            for (int i = 0; i < cfg.crtx.region.xDimension; i++) {
                for(int j = 0; j < cfg.crtx.region.xDimension; j++) {
                    traceINP.addPoint(i+j, cfg.crtx.region.input(time, i, j));
                }
            }
        }
        String buf = "";
        int overalDSCount = 0;
        buf += "Cells Activity: \r\n" + "Timestep: " + cfg.crtx.region.totalTime + "\r\n";
        buf += "Inhibition Radius: " + cfg.crtx.region.inhibitionRadius + "\r\n";
        if (cfg.crtx.region.activeColumns.size() > 0)
            buf += "Active Columns: " + cfg.crtx.region.activeColumns.
                    get(cfg.crtx.region.time-1 > 0 ? cfg.crtx.region.time-1 : 0).size() + "\r\n";
//                            textPane1.setText(buf + region.dendriteSegments.toString() + "\r\n");
//                            textPane1.setText(buf + region.learnState.get(region.time).toString() + "\r\n");
//                            for(int i=0;i<region.xDimension*region.yDimension;i++)
//                                buf += region.overlap[i] + " ";
        for(int c = 0; c < cfg.crtx.region.xDimension*cfg.crtx.region.yDimension;c++) {
            if (over) {
                traceO.addPoint(c, cfg.crtx.region.overlap[c]);
            }
            if (adc) {
                traceADC.addPoint(c, cfg.crtx.region.activeDutyCycle[c]);
            }
            if (mdc) {
                traceMDC.addPoint(c, cfg.crtx.region.minDutyCycle[c]);
            }
            if (odc) {
                traceODC.addPoint(c, cfg.crtx.region.overlapDutyCycle[c]);
            }
            if (bst) {
                traceBST.addPoint(c, cfg.crtx.region.boost[c]);
            }
            for (int i = 0; i < cfg.crtx.region.cellsPerColumn; i++) {
                Boolean val;
                if (act) {
                    val = cfg.crtx.region.activeState.get(time).get(c).get(i);
                    traceA.addPoint(c, val ? i+1 * 1.0: 0.0);
                }
                if (learn) {
                    val = cfg.crtx.region.learnState.get(time).get(c).get(i);
                    traceL.addPoint(c, val ? i+1 * 1.0: 0.0);
                }
                if (predict) {
                    val = cfg.crtx.region.predictiveState.get(time).get(c).get(i);
                    traceP.addPoint(c, val ? i+1 * 1.0: 0.0);
                }

                if (showDistalSegmentsCount) {
                    Integer size = cfg.crtx.region.dendriteSegments.get(c).get(i).size();
                    traceD.addPoint(c, i+1 * size);
                    buf += "C: " + c + " I: " + i + " N: " + size + " L: " +
                            cfg.crtx.region.learnState.get(time).get(c).get(i) + " # ";
                    overalDSCount += size;
                }
            }
            if (perm) {
                Integer activeSynapses = 0;
                for (int s=0;s<cfg.crtx.region.xDimension*cfg.crtx.region.yDimension;s++) {
                    activeSynapses += cfg.crtx.region.potentialSynapses.get(c).get(s).permanence >
                            cfg.crtx.region.connectedPerm ? 1: 0;
                }
                traceS.addPoint(c, activeSynapses);
            }
            buf += "\r\n";
        }
        if (showDistalSegmentsCount)
            traceTMLN.addPoint(cfg.crtx.region.totalTime, overalDSCount);
        buf += "Overall Dendrite Segments Count: " + overalDSCount + "\r\n";
        cfg.textPane1.setText(buf);
    }
}
