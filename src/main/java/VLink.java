import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Setter;

import java.util.Random;

public class VLink extends Link implements Comparable<VLink> {

    public static boolean BIG_VARIANCE = false;

    @Setter
    private int reqIndex;
    private static Random RANDOM;

    public VLink() {
        super();
//        reqIndex = Request.getCount();
    }

    @Override
    public String getParam() {
        return reqIndex + " " + super.getParam();
    }

    public VLink(VRouter r1, VRouter r2, int capacity, Request request) {
        super(r1, r2, capacity, request);
        reqIndex = request.getIndex();
    }

    public VLink(VRouter r1, VRouter r2, int capacity, Request request, int index) {
        super(r1, r2, capacity, request);
        reqIndex = index;
    }

    public void setRandomCapacity() {
        int variance = BIG_VARIANCE ? 10 : 2;
        do {
            capacity = (int) Math.round(((RANDOM != null) ? RANDOM : new Random()).nextGaussian() * variance + 10);
        } while (capacity < 1 || capacity > 20);
    }

    @Override
    protected VRouter getTarget() {
        return (VRouter) super.getTarget();
    }

    @Override
    public VRouter getSource() {
        return (VRouter) super.getSource();
    }

    @Override
    public String toString() {
        return String.format("%s %s %d",
                getSource(),
                getTarget(),
                capacity);
    }

    @Override
    public int compareTo(VLink l) {
        return getCapacity() < l.getCapacity() ? 1 : -1;
    }

    @JsonCreator
    public static VLink JsonParser(@JsonProperty("S") VRouter source, @JsonProperty("T") VRouter target, @JsonProperty("C") int capacity, @JsonProperty("R") int request) {
        VLink link = new VLink();
        link.setCapacity(capacity);
        link.assign(source, target);
        link.setName();
        link.setReqIndex(request);
        return link;
    }

    public void randomize(int req) {
        reqIndex = req;
        randomize();
    }

    public void randomize() {
        setName();
        setRandomCapacity();
    }

    public static void setRandom(long seed) {
        RANDOM = new Random(seed);
    }

    public static void setRandom(Random random) {
        RANDOM = random;
    }
}
