import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jgrapht.graph.SimpleWeightedGraph;

import java.util.LinkedHashMap;
import java.util.Map;

public class pLink extends Link implements Comparable<pLink>, Visualisable {

    private static int COUNTER = 0;

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    private int index;

    public pLink(pRouter r1, pRouter r2, int capacity, SimpleWeightedGraph graph) {
        super(r1, r2, capacity, graph);
        index = ++COUNTER;
        substrateCapacity = capacity;
        graph.setEdgeWeight(this, (double) 1 / substrateCapacity);
    }

    private int substrateCapacity;
    private Map<Integer, Integer> requests;

    public void count() {
        index = ++COUNTER;
    }

    public static void resetCounter() {
        COUNTER = 0;
    }

    public void setVLinks(Map<Integer, vLink> vLinks) {
        this.vLinks = vLinks;
    }

    public Map<Integer, vLink> getVLinks() {
        return vLinks;
    }

    private Map<Integer, vLink> vLinks;

    public pLink() {
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

    public void setSubstrateCapacity(int substrateCapacity) {
        this.substrateCapacity = substrateCapacity;
    }

    public int getSubstrateCapacity() {
        return substrateCapacity;
    }

    public void updateWeight() {
        new SimpleWeightedGraph(this.getClass()).setEdgeWeight(this, (double) 1 / substrateCapacity);
    }

    @Override
    public int compareTo(pLink l) {
        return substrateCapacity - l.getSubstrateCapacity();
    }

    public void serveRequest(int request, vLink link) {
        if (requests == null)
            requests = new LinkedHashMap<>();
        if (vLinks == null)
            vLinks = new LinkedHashMap<>();
        int capacity = link.getCapacity();
        removeCapacity(capacity);
        requests.put(request, capacity);
        vLinks.put(request, link);
    }

    public int removeRequest(int request) {
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
        return String.format("%s %s %d %s", getSource(), getTarget(), substrateCapacity, (vLinks != null) ? vLinks : "");
    }

    @JsonCreator
    public static pLink JsonParser(@JsonProperty("S") pRouter source, @JsonProperty("T") pRouter target, @JsonProperty("C") int capacity, @JsonProperty("vLinks") Map<Integer, vLink> vLinks) {
        pLink link = new pLink();
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
        vLinks.entrySet().forEach(e ->
                sb.append(e.getKey())
                        .append(":")
                        .append(e.getValue().getName().replace(" ", "-"))
                        .append("(")
                        .append(e.getValue().getCapacity())
                        .append("),\n"));
        sb.setLength(sb.length() - 2);
//        sb.deleteCharAt(sb.length() - 2);
        sb.append("]");
        return sb.toString();
    }
}
