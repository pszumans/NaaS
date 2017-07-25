import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.jgrapht.VertexFactory;
import org.jgrapht.generate.GnmRandomGraphGenerator;
import org.jgrapht.graph.SimpleGraph;

import java.util.Set;
import java.util.TreeSet;

@Getter @Setter
public class Request extends SimpleGraph {

    private static int COUNTER = 0;
    private String name;
    private int index;
    private boolean isServed;
    private Set<vLink> links;

    public Request(Request request) {
        this(vLink.class, request.getIndex());
        links = request.getLinks();
    }

    public Request(Class<? extends vLink> edgeClass) {
//        this(edgeClass, 0);
        super(edgeClass);
        index = ++COUNTER;
        setName(null);
    }

    public Request(Class<? extends vLink> edgeClass, int i) {
        super(edgeClass);
        index = i;
        setName(name);
//                (i != 0) ? i :
//                        ++COUNTER;
//        name = "R" + index;
    }

    public Request(Class<? extends vLink> edgeClass, String name) {
        super(edgeClass);
        index = ++COUNTER;
        setName(name);
    }

    public static Request getRandom(int numberOfRouters, int numberOfLinks, long seed) {
//        int loc = new Random().nextInt(Router.LOC_MAX) + 1;
//        int cnt = 0;
        Request request = new Request(vLink.class);
        GnmRandomGraphGenerator<vRouter, vLink> randomGraphGenerator
                = (seed == 0)
                ? new GnmRandomGraphGenerator<>(numberOfRouters, numberOfLinks)
                : new GnmRandomGraphGenerator<>(numberOfRouters, numberOfLinks, seed);
        VertexFactory<vRouter> vertexFactory = (() -> {
            vRouter router = new vRouter();
            return router;
        });
        randomGraphGenerator.generateGraph(request, vertexFactory, null);
        request.edgeSet().forEach(l -> ((vLink) l).randomize());
        request.setLinks(new TreeSet<>((Set<vLink>)request.edgeSet()));
//        request.setLinks(request.edgeSet());
        return request;
    }

    public void addLink(vLink link) {
        if (links == null)
            links = new TreeSet<>();
        links.add(link);
    }

    public void setName(String name) {
        StringBuilder sb = new StringBuilder();
        sb.append("#" + index);
        if (name != null)
            sb.append("(" + name + ")");
        this.name = sb.toString();
    }

    public static int getCount() {
        return COUNTER;
    }

    public static int count() {
        return ++COUNTER;
    }

    public static void resetCounter() {
        COUNTER = 0;
        vRouter.resetCounter();
    }

    public String getRoutersParam(int i) {
        StringBuilder sb = new StringBuilder();
        vertexSet().forEach(r -> sb.append(((vRouter) r).getParam(i)));
        return sb.toString();
    }

    public String getLinksParam() {
        StringBuilder sb = new StringBuilder();
        edgeSet().forEach(l -> sb.append(((vLink) l).getParam()));
        return sb.toString();
    }

    @Override
    public String toString() {
        return String.format("%s -> %5b %s", name, isServed, links);
    }

    @JsonCreator
    public static Request JsonParser(@JsonProperty("name") String name, @JsonProperty("links") TreeSet<vLink> links) {
        Request request = new Request(vLink.class, name);
        links.forEach(l -> {
            request.assignLink(l);
        });
        request.links =
//                (TreeSet<vLink>)
                        links;
//        request.index = index;//++COUNTER;
        return request;
    }

    private void assignLink(vLink link) {
        vRouter r1 = link.getSource();
        vRouter r2 = link.getTarget();
        addVertex(r1);
        addVertex(r2);
        addEdge(r1, r2, link);
    }

    public String getRoutersNames() {
        StringBuilder sb = new StringBuilder();
        vertexSet().forEach(r -> sb.append(" ").append(((vRouter) r).getName()));
        return sb.toString();
    }

    public String getLinksNames() {
        StringBuilder sb = new StringBuilder();
        edgeSet().forEach(l -> sb.append(" ").append(((vLink) l).getSigned()));
        return sb.toString();
    }
}
