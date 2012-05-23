import info.monitorenter.gui.chart.Chart2D;

public class CortexThread extends Thread{
    public HTMCortex region = new HTMCortex();
    public ChartHandler chartHandler;
    private Boolean runs = false;
    private Boolean pause = false;
    private Boolean makeStep = false;

    public CortexThread () {

    }

    public void Init(Chart2D chart, HTMConfiguration configuration) {
        region.SInitialization();
        chartHandler = new ChartHandler(chart, configuration);
    }

    public void run() {
        this.runs = true;
        while (runs) {
            if (!pause) {
                region.SOverlap();
                region.SInhibition();
                region.SLearning();
                region.TCellStates();
                region.TPredictiveStates();
                region.TLearning();
                chartHandler.CollectData();
                region.timestep();
                if (makeStep) {
                    pause = true;
                    makeStep = false;
                }
            }
        }
    }

    public Boolean isRunning() {
        return runs;
    }
    public Boolean isPaused() {
        return pause;
    }

    public void MakeStep() {
        if (!runs)
            this.start();
        this.pause = false;
        this.makeStep = true;
    }

    public void Continue() {
        this.pause = false;
    }

    public void Quit() {
        this.runs = false;
    }
}
