import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Random;

//@JsonIgnoreProperties({"weight"})
public class vLink extends Link implements Comparable<vLink> {

    //    @JsonIgnore
//    double weight;
    private int requestIndex;
    public static Random random;

    public vLink() {
        super();
//        requestIndex = Request.getCount();
    }

    @Override
    public String getParam() {
        return requestIndex + " " + super.getParam();
    }

    public vLink(vRouter r1, vRouter r2, int capacity, Request request) {
        super(r1, r2, capacity, request);
        requestIndex = request.getIndex();
    }

    public vLink(vRouter r1, vRouter r2, int capacity, Request request, int index) {
        super(r1, r2, capacity, request);
        requestIndex = index;
    }

    public void setRandomCapacity() {
        int avg = 50;
        int std =
                12;
//				40;
//				10;
        int min = avg - std;
        int diff = 2 * std;
        int steps =
                9;
//				5;
//				2;
        int factor = diff / (steps - 1);
        capacity = ((random != null) ? random : new Random()).nextInt(steps)*factor + min;
    }

    @Override
    protected vRouter getTarget() {
        return (vRouter) super.getTarget();
    }

    @Override
    public vRouter getSource() {
        return (vRouter) super.getSource();
    }

    @Override
    public String toString() {
        return String.format("%s %s %d",
                getSource(),
                getTarget(),
                capacity);
    }

    @Override
    public int compareTo(vLink l) {
        return
//                l.getCapacity() - //malejąco
                getCapacity()
                        - l.getCapacity() //rosnąco
                ;
    }

    @JsonCreator
    public static vLink JsonParser(@JsonProperty("S") vRouter source, @JsonProperty("T") vRouter target, @JsonProperty("C") int capacity, @JsonProperty("R") int request) {
        vLink link = new vLink();
        link.setCapacity(capacity);
        link.assign(source, target);
        link.setName();
        link.setRequestIndex(request);
        return link;
    }

    private void setRequestIndex(int request) {
        requestIndex = request;
    }

    public void randomize() {
        setName();
        setRandomCapacity();
    }

    public static void setRandom(long seed) {
        random = new Random(seed);
    }
}
