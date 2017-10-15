import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@JsonIgnoreProperties(ignoreUnknown = true)
public class vRouter extends Router {

    private int L;
    public static int LOC_MAX;
    private static int COUNTER;

    private static Random RANDOM;

    @Getter
    private int reqIndex;
    private Set<Integer> locations;

    public vRouter(String name, int B, int M, Set<Integer> Lv) {
        super(name, B, M);
        locations = Lv;
        reqIndex = Request.getCount();
    }

    public vRouter(String name, int B, int M, Set<Integer> Lv, int index) {
        super(name, B, M);
        locations = Lv;
        reqIndex = index;
    }

    public vRouter(String name, int B, int M, int... L) {
        super(name, B, M, L);
    }

    public vRouter(String name) {
        super(name);
        locations = new HashSet<>();
        reqIndex = Request.getCount();
    }

    public vRouter(int req) {
        this();
        reqIndex = req;
    }

    public vRouter() {
        super(
                "V" + ++COUNTER,
                (int) Math.round(((RANDOM != null) ? RANDOM : new Random()).nextGaussian()*5 + 20),
                (int) Math.round(((RANDOM != null) ? RANDOM : new Random()).nextGaussian()*5 + 20)
        );
        while (power < 1 || power > 40)
            setPower((int) Math.round(((RANDOM != null) ? RANDOM : new Random()).nextGaussian()*5 + 20));
        while (memory < 1 || memory > 40)
            setMemory((int) Math.round(((RANDOM != null) ? RANDOM : new Random()).nextGaussian()*5 + 20));
        nearLocations();
    }

    public vRouter(int i, int loc) {
        super("V" + i,
                ((RANDOM != null) ? RANDOM : new Random()).nextInt(5)*5 + 40,
                ((RANDOM != null) ? RANDOM : new Random()).nextInt(5)*5 + 40);
        nearLocations(loc);
    }

    public vRouter(String name, int B, int M, int request, int... L) {
        super(name, B, M, L);
        reqIndex = request;
    }

    @Override
    public String getParam(int i) {
        List locations = new ArrayList(getLocations());
        StringBuilder sb = new StringBuilder(reqIndex + " " + super.getParam(i));
        if (i == 2)
            sb.append(locations.size() + "\n");
        if (i == 3) {
            final StringBuilder bs = new StringBuilder();
            IntStream.range(0, locations.size()).forEach(l -> bs.append(reqIndex + " " + name + " " + (l + 1) + " " + locations.get(l) + "\n"));
            sb = bs;
        }
        return sb.toString();
    }

    private static int count() {
        return ++COUNTER;
    }

    public static void resetCounter() {
        COUNTER = 0;
    }

    private void randomLocations() {
        locations = new HashSet<>();
//        L = ((RANDOM != null) ? RANDOM : new Random()).nextInt(LOC_MAX) + 1;
        L = Math.round((float) LOC_MAX / 3);
        int location;
        while (locations.size() < L) {
            location = ((RANDOM != null) ? RANDOM : new Random()).nextInt(LOC_MAX) + 1;
            locations.add(location);
        }
//        this.locations = new ArrayList<>(locations);
    }

    public Set<Integer> getNearLocations() {
        nearLocations();
        return locations;
    }

    private void nearLocations() {
        locations = new HashSet<>();
        L = (int) Math.ceil((float) LOC_MAX / 3);
        int location;
        while (locations.size() < L) {
            while (true) {
                location = ((RANDOM != null) ? RANDOM : new Random()).nextInt(LOC_MAX) + 1;
                final int temp = location;
                if (locations.isEmpty() || locations.stream().anyMatch(l -> (temp == LOC_MAX && (l == 1 || l == LOC_MAX - 1)) || l == temp - 1 || l == temp + 1))
                    break;
            }
            locations.add(location);
        }
    }

    private void nearLocations(int location) {
        L = Math.round(LOC_MAX / 3);
        for (int l = 0; l < L; l++) {
            locations.add(location);
            if (++location > LOC_MAX)
                location = 1;
        }
    }

    public void setLocationArray(int count) {
        L = count;
        locations = new HashSet<>(count);
        for (int i = 0; i < count; i++) {
            locations.add(-1);
        }
    }

    public void addLocation(int location) {
        locations.add(location);
    }

    public Set<Integer> getLocations() {
        return locations;
    }

    @Override
    protected void locate(int... L) {
        if (locations == null)
            locations = new HashSet<>();
        locations = Arrays.stream(L).boxed().collect(Collectors.toSet());
    }

    @Override
    public String toString() {
        return super.toString() + " " + locations + ")";
    }

    @JsonCreator
    public static vRouter JsonParser(@JsonProperty("name") String name, @JsonProperty("B") int B, @JsonProperty("M") int M, @JsonProperty("R") int request, @JsonProperty("L") int... L) {
        return new vRouter(name, B, M, request, L);
    }

    @Override
    public String toOPL() {
        return String.format("%s %s>\n", super.toOPL(), locations.toString().replace("["," { ").replace("]"," } ").replace(",",""));
    }

    public static void setRandom(long seed) {
        RANDOM = new Random(seed);
    }

    public static void setRandom(Random random) {
        RANDOM = random;
    }
}
