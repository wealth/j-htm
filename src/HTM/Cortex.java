package HTM;

import applet.ExtensionGUI;

import java.util.LinkedList;
import java.util.Arrays;
import java.util.Random;

/**
 * Created by IntelliJ IDEA.
 * User: soil
 * Date: 3/4/12
 * Time: 5:26 PM
 * To change this template use File | Settings | File Templates.
 */
public class Cortex {
    public Integer time = 0;
    public Integer totalTime = 0;

    // Spatial
    public LinkedList<Column> columns = new LinkedList<Column>();
    public LinkedList<LinkedList<Synapse>> potentialSynapses = new LinkedList<LinkedList<Synapse>>();
    public LinkedList<Integer[][]> inputBits;
    public Double[] overlap;
    public Double[] activeDutyCycle;
    public Double[] overlapDutyCycle;
    public Double[] minDutyCycle;
    public Double[] boost;
    public LinkedList<LinkedList<Integer>> activeColumns;
    public Integer minOverlap;
    public Integer desiredLocalActivity;
    public Double inhibitionRadius;
    public Double connectedPerm;
    public Double permanenceInc;
    public Double permanenceDec;

    // Temporal
    public LinkedList<LinkedList<Cell>> cells = new LinkedList<LinkedList<Cell>>();
    public LinkedList<LinkedList<LinkedList<Segment>>> dendriteSegments = new LinkedList<LinkedList<LinkedList<Segment>>>();
    public LinkedList<LinkedList<LinkedList<Boolean>>> activeState = new LinkedList<LinkedList<LinkedList<Boolean>>>();
    public LinkedList<LinkedList<LinkedList<Boolean>>> predictiveState = new LinkedList<LinkedList<LinkedList<Boolean>>>();
    public LinkedList<LinkedList<LinkedList<Boolean>>> learnState = new LinkedList<LinkedList<LinkedList<Boolean>>>();
    public Integer cellsPerColumn;
    public Integer activationThreshold;
    public Double learningRadius;
    public Double initialPerm;
    public Integer minThreshold;
    public Integer newSynapseCount;
    public LinkedList<LinkedList<LinkedList<segmentUpdate>>> segmentUpdateList =
            new LinkedList<LinkedList<LinkedList<segmentUpdate>>>();

    public class segmentUpdate {
        public Integer[] segmentIndex;
        public LinkedList<Synapse> activeSynapses;
        public Boolean sequenceSegment = false;

        public segmentUpdate(Integer[] segmentIndex, LinkedList<Synapse> activeSynapses) {
            this.segmentIndex = segmentIndex;
            this.activeSynapses = activeSynapses;
        }
    }

    public enum State {
        active,
        learn
    }
    
    public Integer input(Integer t, Integer j, Integer k) {
        //return Math.sin(j+k+totalTime) > 0 ? 1 : 0;
        //return rnd.nextInt(2);
        if (ExtensionGUI.Input == null)
            return t % 2 > 0 ? rnd.nextInt(2) : Math.sin(j+k+totalTime) > 0 ? 1 : 0;
        else {
            byte[] buffer = ExtensionGUI.Input;
            int l = buffer.length;
            int width = l / xDimension;
            int height = 256 / yDimension;
            int amount = 0;
            for (int i = j * width; i < (j+1)*width; i++) {
                if ((k+1)*height - 128 < buffer[j] && buffer[j] > k*height - 128)
                    amount ++;
            }
            // System.out.println("by x:" + j * width + " " + (j+1)*width + " by y: " + (k+1)*height + " " + k*height + " is " + amount);
            return amount > width / 10 ? 1 : 0;
        }
    }

    public LinkedList<Integer> neighbours(Integer c) {
        LinkedList<Integer> result = new LinkedList<Integer>();
        Column currentColumn = this.columns.get(c);
        for(int i=0;i<this.columns.size();i++) {
            if ((Math.abs(columns.get(i).x - currentColumn.x) < inhibitionRadius) &&
                    (Math.abs(columns.get(i).y - currentColumn.y) < inhibitionRadius))
                result.add(i);
        }
        return result;
    }

    public LinkedList<Synapse> connectedSynapses(Integer c) {
        LinkedList<Synapse> result = new LinkedList<Synapse>();
        for(Synapse synapse: this.potentialSynapses.get(c)) {
            if (synapse.permanence > this.connectedPerm)
                result.add(synapse);
        }
        return result;
    }

    public Double kthScore(LinkedList<Integer> cols, Integer k){
        Double[] overlaps = new Double[cols.size()];
        for(int i=0; i<cols.size(); i++) {
            overlaps[i] = overlap[cols.get(i)];
        }
        Arrays.sort(overlaps);
        return overlaps[overlaps.length-k];
    }

    public Double updateActiveDutyCycle(Integer c) {
        Double value = 0.0;
        for(Integer idx: activeColumns.get(time))
            if (c.equals(idx)) {
                value = 1.0;
                break;
            }
        return (value + totalTime.floatValue() * activeDutyCycle[c]) / (totalTime.floatValue() + 1.0);
    }

    public Double updateOverlapDutyCycle(Integer c) {
        Double value = 0.0;
        if (overlap[c] > this.minOverlap) {
            value = 1.0;
        }
        return (value + totalTime * overlapDutyCycle[c]) / (totalTime + 1);
    }

    // TODO: debug this value
    public Double averageReceptiveFieldSize() {
        Double xDistance;
        Double yDistance;
        Double result = 0.0;
        for(int i=0;i<this.columns.size();i++) {
            xDistance = 0.0;
            yDistance = 0.0;
            for(Synapse synapse : this.potentialSynapses.get(i)) {
                if (synapse.permanence > this.connectedPerm) {
                    Double xCalculated = Math.abs(this.columns.get(i).x.doubleValue() - synapse.c.doubleValue());
                    Double yCalculated = Math.abs(this.columns.get(i).y.doubleValue() - synapse.i.doubleValue());
                    xDistance = xDistance > xCalculated ? xDistance : xCalculated;
                    yDistance = yDistance > yCalculated ? yDistance : yCalculated;
                }
            }
            result = (Math.sqrt(xDistance*xDistance + yDistance*yDistance) + i * result ) / (i+1);
        }
        return result;
    }

    public Double maxDutyCycle(LinkedList<Integer> cols) {
        Double max = 0.0;
        for(Integer col: cols) {
            if (max < activeDutyCycle[col])
                max = activeDutyCycle[col];
        }
        return max;
    }
    
    public void increasePermanences(Integer c, Double s) {
        for(Synapse syn: potentialSynapses.get(c)) {
            syn.permanence = Math.min(syn.permanence + syn.permanence * s, 1.0) ;
        }
    }
    
    public Double boostFunction(Double aDC, Double mDC) {
        return aDC > mDC ? 1.0 : (1.0 + mDC*100);
    }
    
    public Boolean segmentActive(Segment s, Integer t, State state) {
        LinkedList<LinkedList<Boolean>> list = state.equals(State.active) ?
                activeState.get(t) : learnState.get(t);
        Integer counter = 0;
        for(Synapse syn: s.synapses) {
            if(list.get(syn.c).get(syn.i) && syn.permanence > connectedPerm) {
                counter++;
            }
        }
        return counter > activationThreshold;
    }

    public Integer[] getActiveSegment(Integer c, Integer i, Integer t, State state) {
        LinkedList<Segment> activeSegments = new LinkedList<Segment>();
        for(Segment segment: dendriteSegments.get(c).get(i)) {
            if (segmentActive(segment, t, state))
                activeSegments.add(segment);
        }
        if (activeSegments.size() == 1) {
            return new Integer[]{c, i, dendriteSegments.get(c).get(i).indexOf(activeSegments.get(0))};
        } else {
            for(Segment segment: activeSegments) {
                if (segment.sequenceSegment)
                    return new Integer[]{c, i, dendriteSegments.get(c).get(i).indexOf(segment)};
            }
            LinkedList<LinkedList<Boolean>> list = state == State.active ?
                    activeState.get(t) : learnState.get(t);
            Integer maxActivity = 0;
            Integer result = -1;
            for(int j = 0; j < activeSegments.size(); j++) {
                Integer counter = 0;
                for(Synapse syn: activeSegments.get(j).synapses) {
                    if(list.get(syn.c).get(syn.i) && syn.permanence > connectedPerm) {
                        counter++;
                    }
                }
                if (maxActivity < counter) {
                    maxActivity = counter;
                    result = j;
                }
            }
            return new Integer[]{c, i, result};
        }
    }

    public Integer[] getBestMatchingSegment(Integer c, Integer i, Integer t) {
        LinkedList<LinkedList<Boolean>> list = activeState.get(t);
        Integer maxActivity = 0;
        Integer result = -1;
        for(int j = 0; j < dendriteSegments.get(c).get(i).size(); j++) {
            Integer counter = 0;
            Segment segment = dendriteSegments.get(c).get(i).get(j);
            for(Synapse syn: segment.synapses) {
                if(list.get(syn.c).get(syn.i)) {
                    counter++;
                }
            }
            if (maxActivity < counter) {
                maxActivity = counter;
                result = j;
            }
        }
        return maxActivity > minThreshold ? new Integer[]{c, i, result} : new Integer[]{c, i, -1};
    }

    public Integer[] getBestMatchingCell(Integer c, Integer t) {
        Integer minSegments = null;
        Integer cellIndex = -1;
        Integer minSegmentsCellIndex = -1;

        LinkedList<LinkedList<Boolean>> list = activeState.get(t);
        Integer maxActivity = 0;
        Integer result = -1;

        for(int i=0;i<cellsPerColumn;i++) {
            for(int j = 0; j < dendriteSegments.get(c).get(i).size(); j++) {
                Integer counter = 0;
                Segment segment = dendriteSegments.get(c).get(i).get(j);
                for(Synapse syn: segment.synapses) {
                    if(list.get(syn.c).get(syn.i)) {
                        counter++;
                    }
                }
                if (maxActivity < counter) {
                    maxActivity = counter;
                    result = j;
                    cellIndex = i;
                }
            }
            if (minSegments == null || minSegments > dendriteSegments.get(c).get(i).size()) {
                minSegments = dendriteSegments.get(c).get(i).size();
                minSegmentsCellIndex = i;
            }
        }
        return maxActivity > minThreshold ? new Integer[]{c, cellIndex, result} : new Integer[]{c, minSegmentsCellIndex, -1};
    }

    public segmentUpdate getSegmentActiveSynapses(Integer c, Integer i, Integer t, Integer s, Boolean newSynapses) {
        LinkedList<Synapse> activeSynapses = new LinkedList<Synapse>();
        if (s >= 0) {
            for(Synapse syn: dendriteSegments.get(c).get(i).get(s).synapses) {
                if (activeState.get(t).get(syn.c).get(syn.i)) {
                    activeSynapses.add(syn);
                }
            }
        }
        if (newSynapses) {
            Random r = new Random();
            LinkedList<Integer[]> learningCells = new LinkedList<Integer[]>();
            for (int j = 0; j < learnState.get(t).size();j++) {
                for (int k = 0; k < learnState.get(t).get(j).size();k++) {
                    if (learnState.get(t).get(j).get(k) && !(c.equals(j) && i.equals(k))) {
                        learningCells.add(new Integer[]{j, k});
                    }
                }
            }
            for (int k=0; k < newSynapseCount - activeSynapses.size(); k++) {
                Integer[] idx;
                idx = learningCells.get(r.nextInt(learningCells.size()));
                activeSynapses.add(new Synapse(idx[0], idx[1], initialPerm));
            }
        }
        return new segmentUpdate(new Integer[]{c, i, s}, activeSynapses);
    }

    public void adaptSegments(LinkedList<segmentUpdate> segmentList, Boolean positiveReinforcement) {
        for(segmentUpdate segUpd: segmentList) {
            // System.out.print(segUpd.segmentIndex[2] + "\r\n");
            if (segUpd.segmentIndex[2] < 0) {
                Segment newSegment = new Segment();
                for(Synapse syn: segUpd.activeSynapses) {
                    newSegment.synapses.add(syn);
                }
                newSegment.sequenceSegment = segUpd.sequenceSegment;
                dendriteSegments.get(segUpd.segmentIndex[0]).get(segUpd.segmentIndex[1]).add(newSegment);
            } else {
                Segment seg =  dendriteSegments.get(segUpd.segmentIndex[0]).get(segUpd.segmentIndex[1])
                        .get(segUpd.segmentIndex[2]);
                seg.sequenceSegment = segUpd.sequenceSegment;
                for(Synapse syn: seg.synapses) {
                    if (segUpd.activeSynapses.contains(syn)) {
                        if (positiveReinforcement)
                            syn.permanence += permanenceInc;
                        else
                            syn.permanence -= permanenceDec;
                    } else {
                        if (positiveReinforcement)
                            syn.permanence -= permanenceDec;
                        else
                            syn.permanence += permanenceInc;
                    }
                }
                for(Synapse syn: segUpd.activeSynapses) {
                    if (!seg.synapses.contains(syn)) {
                        seg.synapses.add(syn);
                    }
                }
            }
        }
    }

    Random rnd = new Random();
    public Integer xDimension;
    public Integer yDimension;
    
    // main phases
    public void SInitialization() {
        for (int i=0;i<xDimension;i++) {
            for (int j=0;j<yDimension;j++) {
                columns.add(new Column(i, j));
                potentialSynapses.add(new LinkedList<Synapse>());
            }
        }
        
        overlap = new Double[xDimension*yDimension];
        Arrays.fill(overlap, 0.0);
        minDutyCycle = new Double[xDimension*yDimension];
        Arrays.fill(minDutyCycle, 0.0);
        activeDutyCycle = new Double[xDimension*yDimension];
        Arrays.fill(activeDutyCycle, 0.0);
        overlapDutyCycle = new Double[xDimension*yDimension];
        Arrays.fill(overlapDutyCycle, 0.0);
        boost = new Double[xDimension*yDimension];
        Arrays.fill(boost, 1.0);
        activeColumns = new LinkedList<LinkedList<Integer>>();
        inputBits = new LinkedList<Integer[][]>();
        inputBits.add(new Integer[xDimension][yDimension]);
        
        for(int k = 0;k<potentialSynapses.size();k++) {
            Column column = columns.get(k);
            LinkedList<Synapse> list = potentialSynapses.get(k);
            // TODO: Region's dimensions used to initialize proximal synapses - may be incorrect
            for(int i=0;i<xDimension*yDimension;i++) {
                Integer dimX = rnd.nextInt(xDimension);
                Integer dimY = rnd.nextInt(yDimension);
                Double perm = connectedPerm + connectedPerm / 2.0 - (rnd.nextDouble()/10.0);
                Double adjustment = Math.sqrt((((column.x - dimX))^2 +((column.y - dimY))^2)/(xDimension+yDimension));

                list.add(new Synapse(dimX, dimY, Math.max(perm - adjustment, 0.0)));
            }
        }

        activeState.add(new LinkedList<LinkedList<Boolean>>());
        learnState.add(new LinkedList<LinkedList<Boolean>>());
        predictiveState.add(new LinkedList<LinkedList<Boolean>>());        
        for(int c=0;c<xDimension*yDimension;c++) {
            dendriteSegments.add(new LinkedList<LinkedList<Segment>>());
            activeState.get(time).add(new LinkedList<Boolean>());
            learnState.get(time).add(new LinkedList<Boolean>());
            predictiveState.get(time).add(new LinkedList<Boolean>());
            segmentUpdateList.add(new LinkedList<LinkedList<segmentUpdate>>());
            for(int i=0;i<cellsPerColumn;i++) {
                activeState.get(time).get(c).add(false);
                learnState.get(time).get(c).add(false);
                predictiveState.get(time).get(c).add(false);
                dendriteSegments.get(c).add(new LinkedList<Segment>());
                segmentUpdateList.get(c).add(new LinkedList<segmentUpdate>());
            }
        }

        inhibitionRadius = averageReceptiveFieldSize();
        overlap = new Double[xDimension*yDimension];
        Arrays.fill(overlap, 0.0);
    }

    public void SOverlap() {
        for(int i=0;i<xDimension*yDimension;i++) {
            overlap[i] = 0.0;
            for(Synapse synapse: connectedSynapses(i)) {
                overlap[i] += input(time, synapse.c, synapse.i);
            }
            if (overlap[i] < minOverlap)
                overlap[i] = 0.0;
            else
                overlap[i] *= boost[i];
        }
    }

    public void SInhibition() {
        activeColumns.add(new LinkedList<Integer>());
        for(int i=0;i<xDimension*yDimension;i++) {
            Double minLocalActivity = kthScore(neighbours(i), desiredLocalActivity);

            if (overlap[i] > 0.0 && overlap[i] >= minLocalActivity) {
                activeColumns.get(time).add(i);
            }
        }
    }

    public void SLearning() {
        for(Integer c: activeColumns.get(time)) {
            for(Synapse s: potentialSynapses.get(c)) {
                if (input(time, s.c, s.i) > 0) {
                    s.permanence += permanenceInc;
                    s.permanence = Math.min(s.permanence, 1.0);
                } else {
                    s.permanence -= permanenceDec;
                    s.permanence = Math.max(s.permanence, 0.0);
                }
            }
        }

        for(int i=0;i<xDimension*yDimension;i++) {
            minDutyCycle[i] = 0.01 * maxDutyCycle(neighbours(i));
            activeDutyCycle[i] = updateActiveDutyCycle(i);
            boost[i] = boostFunction(activeDutyCycle[i], minDutyCycle[i]);
            overlapDutyCycle[i] = updateOverlapDutyCycle(i);

            if (overlapDutyCycle[i] < minDutyCycle[i]) {
                increasePermanences(i, 0.1*connectedPerm);
            }
        }

        inhibitionRadius = averageReceptiveFieldSize();
    }

    public void TCellStates() {
        for(Integer c: activeColumns.get(time)) {
            Boolean buPredicted = false;
            Boolean lcChosen = false;            

            for(int i = 0; i < cellsPerColumn; i++) {
                if (predictiveState.get(time-1 > 0 ? time-1 : 0).get(c).get(i)) {
                    Integer[] s = getActiveSegment(c, i, time-1 > 0 ? time-1 : 0, State.active);
                    if (s[2] >= 0 && dendriteSegments.get(s[0]).get(s[1]).get(s[2]).sequenceSegment) {
                        buPredicted = true;
                        activeState.get(time).get(c).set(i, true);
                        if (segmentActive(dendriteSegments.get(s[0]).get(s[1]).get(s[2]), time-1 > 0 ? time-1 : 0, State.learn)) {
                            lcChosen = true;
                            learnState.get(time).get(c).set(i, true);
                        }
                    }
                }
            }

            if (!buPredicted) {
                for(int i = 0; i < cellsPerColumn; i++) {
                    activeState.get(time).get(c).set(i, true);
                }
            }

            if (!lcChosen) {
                Integer[] lc = getBestMatchingCell(c, time-1 > 0 ? time-1 : 0);
                learnState.get(time).get(c).set(lc[1], true);
                if (time-1 >= 0) {
                    segmentUpdate sUpdate = getSegmentActiveSynapses(c, lc[1], time-1, lc[2], true);
                    sUpdate.sequenceSegment = true;
                    segmentUpdateList.get(c).get(lc[1]).add(sUpdate);
                }
            }
        }
    }

    public void TPredictiveStates() {
        for(int c = 0; c < xDimension*yDimension; c++) {
            for(int i = 0; i < cellsPerColumn; i++)
                for(int s = 0; s < dendriteSegments.get(c).get(i).size();s++) {
                    if (segmentActive(dendriteSegments.get(c).get(i).get(s), time, State.active)) {
                        predictiveState.get(time).get(c).set(i, true);
                        segmentUpdate activeUpdate = getSegmentActiveSynapses(c, i, time, s, false);
                        segmentUpdateList.get(c).get(i).add(activeUpdate);

                        Integer[] predSegment = getBestMatchingSegment(c, i, time-1 > 0 ? time-1 : 0);
                        segmentUpdate predUpdate = getSegmentActiveSynapses(c, i, time-1 > 0 ? time-1 : 0, predSegment[2], true);
                        segmentUpdateList.get(c).get(i).add(predUpdate);
                    }

                }
        }
    }

    public void TLearning() {
        for(int c = 0; c < xDimension*yDimension; c++) {
            for(int i = 0; i < cellsPerColumn; i++) {
                if(learnState.get(time).get(c).get(i)) {
                    adaptSegments(segmentUpdateList.get(c).get(i), true);
                    // segmentUpdateList.get(c).get(i).clear();
                } else if (!predictiveState.get(time).get(c).get(i) && predictiveState.get(time-1 > 0 ? time-1 : 0).get(c).get(i)) {
                    adaptSegments(segmentUpdateList.get(c).get(i), false);
                    // segmentUpdateList.get(c).get(i).clear();
                }
                segmentUpdateList.get(c).get(i).clear();
            }
        }
    }

    public void timestep() {
        time++;
        totalTime++;
        activeState.add(new LinkedList<LinkedList<Boolean>>());
        learnState.add(new LinkedList<LinkedList<Boolean>>());
        predictiveState.add(new LinkedList<LinkedList<Boolean>>());

        if (totalTime > 2) {
            time--;
            activeColumns.remove(time-2);
            activeState.remove(time-2);
            learnState.remove(time-2);
            predictiveState.remove(time-2);
        }
        for(int c=0;c<xDimension*yDimension;c++) {
            activeState.get(time).add(new LinkedList<Boolean>());
            learnState.get(time).add(new LinkedList<Boolean>());
            predictiveState.get(time).add(new LinkedList<Boolean>());
            for(int i=0;i<cellsPerColumn;i++) {
                activeState.get(time).get(c).add(false);
                learnState.get(time).get(c).add(false);
                predictiveState.get(time).get(c).add(false);
            }
        }
    }
}
