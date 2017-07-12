import org.jgrapht.graph.SimpleGraph;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Created by Szuman on 11.05.2017.
 */
public class ParserOPL {

    private final String[] PHYSICAL = {"Vw", "Ee"};
    private final String[] VIRTUAL = {"Vv", "Ed"};

    private Scanner sc;

    public Network getNetwork() {
        return network;
    }

    private Network network;
    private List<Request> requests;

    public ParserOPL(String filename) throws FileNotFoundException {
        sc = new Scanner(new BufferedReader(new FileReader(new File(filename))));
    }

    public ParserOPL parse() {
        requests = new ArrayList<>();
        network = new Network(requests);

//        sc.skip("=|\\}|<|>");
        sc.useDelimiter("\\s*=\\s*|" +
                "\\s*\\s*\\{\\s*|" +
                "\\s*<\\s*|" +
                "\\s*>\\s*|" +
                "\\s*\\[\\s*|" +
                "\\s*]\\s*|" +
                "\\s");
//        System.out.println(sc.delimiter());

        String text;
        while (
//                sc.hasNextLine()) {
//            if (arePaths)
//                break;
//            if (
                sc.hasNext()) {
            text = sc.next();
//                System.out.println("TEXT: " + text);
            if (text.equals("Vw"))
                parseRouters();
            else if (text.equals("Ee"))
                parseLinks();
            else if (text.equals("Vv"))
                parseVRouters();
            else if (text.equals("Ed"))
                parseVLinks();
            else if (sc.hasNextLine())
//                    System.out.println("LINE: " +
                sc.nextLine()
                        ;
//                    );
        }
        sc.close();
        pLink.resetCounter();
        return this;
    }

    private void parseRouters() {
//        System.out.println("SPACE: " +
        sc.next()
        ;
//        );
        while (!sc.next().equals("}")) {
            String name = sc.next();
            int power = sc.nextInt();
            int memory = sc.nextInt();
            int location = sc.nextInt();
            network.addVertex(new pRouter(name, power, memory, location));
            network.addLocation(location, power + memory);
        }
    }

    private void parseLinks() {
        sc.next();
        while (!sc.next().equals("}")) {
            String r1 = sc.next();
            String r2 = sc.next();
            pRouter router1 = (pRouter) getRouterByName(network, r1);
            pRouter router2 = (pRouter) getRouterByName(network, r2);
            int capacity = sc.nextInt();
            new pLink(router1, router2, capacity, network);
        }
    }

    private void parseVRouters() {
        Request request = new Request(vLink.class);
//        requests.add(request);
        sc.skip("\\s*=\\s*\\[\\s*\\{\\s*<");
        while (true) {
            String name = sc.next();
//            System.out.println(name);
            int power = sc.nextInt();
            int memory = sc.nextInt();
            List<Integer> locations = new ArrayList<>();
            String location;// = sc.next();
            while (!(location = sc.next()).equals("}"))
                locations.add(Integer.valueOf(location));
            request.addVertex(new vRouter(name, power, memory, locations));
//            System.out.println(name + power + memory + locations);
            if (sc.next().equals("}")) {
                requests.add(request);
                if (sc.next().equals(";")) {
                    break;
                }
                request = new Request(vLink.class);
            }
        }
    }

    private void parseVLinks() {
        int i = 0;
        Request request = requests.get(i);
        sc.skip("\\s*=\\s*\\[\\s*\\{\\s*<");
        while (true) {
            String r1 = sc.next();
            String r2 = sc.next();
//            System.out.println(r1 + r2);
            vRouter router1 = (vRouter) getRouterByName(request, r1);
            vRouter router2 = (vRouter) getRouterByName(request, r2);
            int capacity = sc.nextInt();
            request.addLink(new vLink(router1, router2, capacity, request));
            if (sc.next().equals("}")) {
                if (sc.next().equals(";")) {
                    break;
                }
                request = requests.get(++i);
            }
        }
//        requests.forEach(r -> System.out.println(r.getLinks()));
    }

    public static Router getRouterByName(SimpleGraph<? extends Router, ? extends Link> graph, String routerName) {
        return graph.vertexSet().stream().filter(r -> r.getName().equals(routerName)).findAny().orElse(null);
    }

}
