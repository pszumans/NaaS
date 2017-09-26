import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.jgrapht.ListenableGraph;
import org.jgrapht.VertexFactory;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.event.GraphListener;
import org.jgrapht.event.VertexSetListener;
import org.jgrapht.generate.GnmRandomGraphGenerator;
import org.jgrapht.graph.SimpleGraph;

import java.lang.reflect.Array;
import java.util.*;

@Getter
@Setter
public class Request extends SimpleGraph implements ListenableGraph {

    public static boolean FULL_RANDOM = false;

    private static int COUNTER;
    private String name;
    private int index;
    private boolean isServed;
    private Set<vLink> links;

    private double duration;

    private static Random RANDOM;
    public static Map<List, GnmRandomGraphGenerator<vRouter, vLink>> randomGraphGenerators;

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

    public static Request getFullRandom(int numbersOfRouters, int numbersOfLinks, long seed) {

        int r, l;

        if (numbersOfRouters == 5 && numbersOfLinks == 7) {
            int[][] rForL = {{2}, {3}, {3, 4}, {4, 5}, {4, 5}, {4, 5}, {5}};

            l = (RANDOM != null ? RANDOM : (RANDOM != null ? RANDOM : new Random())).nextInt(numbersOfLinks);
            r = (RANDOM != null ? RANDOM : (RANDOM != null ? RANDOM : new Random())).nextInt(rForL[l].length);
            r = rForL[l][r];
            l = l + 1;

        } else {
//            int r = (RANDOM != null ? RANDOM : (RANDOM != null ? RANDOM : new Random())).nextInt(numbersOfRouters - 2) + 3;
            r = (RANDOM != null ? RANDOM : (RANDOM != null ? RANDOM : new Random())).nextInt(numbersOfRouters - 1) + 2;
            int bound = getComb(r);

            while ((l = (RANDOM != null ? RANDOM : new Random()).nextInt(bound > numbersOfLinks ? numbersOfLinks : bound) + 1) < r - 1)
                ;
        }
//        System.out.println("k = " + r + " l = " + l);
//        new Scanner(System.in).nextLine();
        return getRandom(r, l, seed);
    }

    public static Request getRandom(int numberOfRouters, int numberOfLinks) {
        return getRandom(numberOfRouters, numberOfLinks, 0);
    }

    public static Request getRandom(int numberOfRouters, int numberOfLinks, long seed) {
        if (randomGraphGenerators == null)
            randomGraphGenerators = new HashMap<>();
        return generateRandom(numberOfRouters, numberOfLinks, seed);
    }

    private static Request generateRandom(int numberOfRouters, int numberOfLinks, long seed) {
//        if (numberOfLinks == 3 && numberOfRouters == 3)
//            System.out.println();
        Request request = new Request(vLink.class);
        GnmRandomGraphGenerator<vRouter, vLink> randomGraphGenerator;
        List key = Arrays.asList(numberOfRouters, numberOfLinks, seed);
        if (randomGraphGenerators.containsKey(key)) {
            randomGraphGenerator = randomGraphGenerators.get(key);
        } else {
            if (seed == 0) {
                randomGraphGenerator = new GnmRandomGraphGenerator<>(numberOfRouters, numberOfLinks);
            } else {
                randomGraphGenerator = new GnmRandomGraphGenerator<>(numberOfRouters, numberOfLinks, seed);
            }
            randomGraphGenerators.put(key, randomGraphGenerator);
        }
        final Set<Set<Integer>> set = new HashSet<>();
        final int max = vRouter.LOC_MAX;
//        System.out.println(max);
//        new Scanner(System.in).nextLine();
        final int reqIndex = request.getIndex();
        VertexFactory<vRouter> vertexFactory = (() -> {
            int size = set.size();
            vRouter router = new vRouter(reqIndex);
            if (size == max) {
                return router;
            }
            while (size == set.size()) {
                set.add(router.getNearLocations());
            }
            return router;
        });
        ConnectivityInspector<vRouter, vLink> cI; //= new ConnectivityInspector<vRouter, vLink>(request);
        do {
            randomGraphGenerator.generateGraph(request, vertexFactory, null);
            cI = new ConnectivityInspector<vRouter, vLink>(request);
            if (!cI.isGraphConnected()) {
                request = new Request(vLink.class, reqIndex);
            }
        } while (!cI.isGraphConnected());
        request.edgeSet().forEach(l -> ((vLink) l).randomize(reqIndex));
        request.setLinks(request.edgeSet());
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
        return String.format("%s -> %5b %s %s", name, isServed, duration, links);
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

    private static int getFact(int x) {
        int res = 1;
        for (int i = 2; i <= x; i++) {
            res *= i;
        }
        return res;
    }

    private static int getComb(int x) {
//        return (x == 1) ? 1 : getFact(x) / (2 * getFact(x - 2));
        return (x == 1) ? 1 : x * (x - 1) / 2;
    }

    @Override
    public void addGraphListener(GraphListener graphListener) {

    }

    @Override
    public void addVertexSetListener(VertexSetListener vertexSetListener) {

    }

    @Override
    public void removeGraphListener(GraphListener graphListener) {

    }

    @Override
    public void removeVertexSetListener(VertexSetListener vertexSetListener) {

    }

    public static void setRandom(long seed) {
        RANDOM = new Random(seed);
    }

    public static void setRandom(Random random) {
        RANDOM = random;
    }
}
