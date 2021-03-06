import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.jgrapht.graph.SimpleWeightedGraph;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter @Setter
public class PLink extends Link implements Comparable<PLink>, Visualisable {

    private static int COUNTER = 0;
    private int index;

    private int substrateCapacity;
    private Map<Integer, Integer> requests;
    private Map<Integer, List<VLink>> vLinks;

    public PLink(PRouter r1, PRouter r2, int capacity, SimpleWeightedGraph graph) {
        super(r1, r2, capacity, graph);
        index = ++COUNTER;
        substrateCapacity = capacity;
        if (Heuristic.WEIGHTABLE_LINKS)
        graph.setEdgeWeight(this, (double) 1 / substrateCapacity); // waga = 1 / przepustowość
    }

    public void count() {
        index = ++COUNTER;
    }

    public static void resetCounter() {
        COUNTER = 0;
    }

    public PLink() {
        super();
    }

    public void addCapacity(int C) {
        substrateCapacity += C;
    }

    public void removeCapacity(int C) {
        substrateCapacity -= C;
        if (substrateCapacity < 0)
            try {
                throw new NegativeParameterException(getName() + " " + getVisualText());
            } catch (NegativeParameterException e) {
                e.printStackTrace();
                System.exit(1);
            }
    }

    public void updateWeight() {
        new SimpleWeightedGraph(this.getClass()).setEdgeWeight(this, (double) 1 / substrateCapacity);
    }

    @Override
    public int compareTo(PLink l) {
        return substrateCapacity - l.getSubstrateCapacity();
    }

    public void serveRequest(int request, VLink link) {
        if (requests == null)
            requests = new LinkedHashMap<>();
        if (vLinks == null)
            vLinks = new LinkedHashMap<>();
        int capacity = link.getCapacity();
        removeCapacity(capacity);
        if (requests.containsKey(request))
            capacity += requests.get(request);
        requests.put(request, capacity);
        if (!vLinks.containsKey(request))
            vLinks.put(request, new ArrayList<>());
        vLinks.get(request).add(link);
    }

    public int removeRequest(int request) {
        if (requests == null || !requests.containsKey(request)) return 0;
        int capacity = requests.get(request);
        addCapacity(capacity);
        requests.remove(request);
        vLinks.remove(request);
        return capacity;
    }

    @Override
    public String getParam() {
        return name + " " + substrateCapacity + "\n";
    }

    @Override
    public String toString() {
        return String.format("%s %s %d/%d %s", getSource(), getTarget(), substrateCapacity, capacity, (vLinks != null) ? vLinks : "");
    }

    @JsonCreator
    public static PLink JsonParser(@JsonProperty("S") PRouter source, @JsonProperty("T") PRouter target, @JsonProperty("C") int capacity, @JsonProperty("vLinks") Map<Integer, List<VLink>> vLinks) {
        PLink link = new PLink();
        link.setCapacity(capacity);
        link.assign(source, target);
        link.setName();
        link.setVLinks(vLinks);
        return link;
    }

    @Override
    public String getVisualText() {
        return String.format("%d/%d%s", substrateCapacity, capacity, (vLinks != null && !vLinks.isEmpty()) ? getVLinksText() : "");
    }

    private String getVLinksText() {
        StringBuilder sb = new StringBuilder("\n");
        sb.append("[");
        vLinks.entrySet().forEach(e -> {
                    sb.append(e.getKey()).append(":");
                    e.getValue().forEach(l -> sb
                            .append(l.getName().replace(" ", "-"))
                            .append("(")
                            .append(l.getCapacity())
                            .append("),\n"));
                }
        );
        sb.setLength(sb.length() - 2);
        sb.append("]");
        return sb.toString();
    }

    public String toOPL(boolean isFull) {
        return String.format("<%s %d>\n", name, isFull ? capacity : substrateCapacity);
    }
}
