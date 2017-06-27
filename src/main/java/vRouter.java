import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@JsonIgnoreProperties(ignoreUnknown = true)
public class vRouter extends Router {

//    @JsonProperty("L")
    private int L;
    private static int LOC_MAX = 6;
    private static int COUNTER = 0;

    private List<Integer> locations;
    private int requestIndex;

    public vRouter(String name, int B, int M, List<Integer> Lv) {
        super(name, B, M);
        locations = Lv;
        requestIndex = Request.getCount();
    }

    public vRouter(String name, int B, int M, List<Integer> Lv, int index) {
        super(name, B, M);
        locations = Lv;
        requestIndex = index;
    }

    public vRouter(String name, int B, int M, int... L) {
        super(name, B, M, L);
    }

    public vRouter(String name) {
        super(name);
        locations = new ArrayList<>();
        requestIndex = Request.getCount();
    }

    public vRouter() {
        super(
                "VV" + count(),
                new Random().nextInt(5)*5 + 40,
                new Random().nextInt(5)*5 + 40
        );
        randomLocations();
    }

    public vRouter(int i) {
        super("VV" + i,
                new Random().nextInt(5)*5 + 40,
                new Random().nextInt(5)*5 + 40);
        randomLocations();
    }

    public vRouter(int i, int loc) {
        super("V" + i,
                new Random().nextInt(5)*5 + 40,
                new Random().nextInt(5)*5 + 40);
        nearLocations(loc);
    }

    public vRouter(String name, int B, int M, int request, int... L) {
        super(name, B, M, L);
        requestIndex = request;
    }

    @Override
    public String getParam(int i) {
        StringBuilder sb = new StringBuilder(requestIndex + " " + super.getParam(i));
        if (i == 2)
            sb.append(locations.size() + "\n");
        if (i == 3) {
            final StringBuilder bs = new StringBuilder();
            IntStream.range(0, locations.size()).forEach(l -> bs.append(requestIndex + " " + name + " " + (l + 1) + " " + locations.get(l) + "\n"));
            sb = bs;
        }
        return sb.toString();
    }

    private static int count() {
        return ++COUNTER;
    }

    private void randomLocations() {
        Set<Integer> locations = new HashSet<>();
        L = new Random().nextInt(LOC_MAX) + 1;
        int location;
        while (locations.size() < L) {
            location = new Random().nextInt(LOC_MAX) + 1;
            locations.add(location);
        }
        this.locations = new ArrayList<>(locations);
    }

    private void nearLocations(int location) {
        locations = new ArrayList<>();
        L = new Random().nextInt(LOC_MAX) + 1;
        for (int l = 0; l < L; l++) {
            locations.add(location);
            if (++location > LOC_MAX)
                location = 1;
        }
    }

    public void setLocation(int index, int location) {
        //while (index > locations.size())
        //locations.add(-1);
        locations.set(index - 1, location);
    }

    public void setLocationArray(int count) {
        L = count;
        locations = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            locations.add(-1);
        }
    }

    public void addLocation(int location) {
        locations.add(location);
    }

    public List<Integer> getLocations() {
        return locations;
    }

    @Override
    protected void locate(int... L) {
        if (locations == null)
            locations = new ArrayList<>();
        locations = Arrays.stream(L).boxed().collect(Collectors.toList());
//        for (int l : L)
//            locations.add(new Integer(l));
    }

    @Override
    public String toString() {
        return super.toString() + " L=" + locations + ")";
    }

    @JsonCreator
    public static vRouter JsonParser(@JsonProperty("name") String name, @JsonProperty("B") int B, @JsonProperty("M") int M, @JsonProperty("R") int request, @JsonProperty("L") int... L) {
        return new vRouter(name, B, M, request, L);
    }

    private void setRequestIndex(int request) {
        requestIndex = request;
    }

    @Override
    public String toOPL() {
        return String.format("%s %s>\n", super.toOPL(), locations.toString().replace("["," { ").replace("]"," } ").replace(",",""));
    }
}
