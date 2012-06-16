package HTM;

/**
 * Created by IntelliJ IDEA.
 * User: soil
 * Date: 3/7/12
 * Time: 10:39 PM
 * To change this template use File | Settings | File Templates.
 */
public class Synapse {
    public Double permanence;

    // X and Y for Spatial and C and I for Temporal

    public Integer c;
    public Integer i;
    
    public Synapse(Integer c, Integer i, Double permanence) {
        this.permanence = permanence;
        this.c = c;
        this.i = i;
    }
}
