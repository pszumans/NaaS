import java.io.FileNotFoundException;
import java.util.*;
import java.util.stream.IntStream;

public class Test {

private String filename;

    public Test(String filename) {
        this.filename = filename;
    }

    public void run() throws FileNotFoundException {
        Network network = new ParserOPL(filename).parse().getNetwork();
        network.serveRequests();
//        }));
//        network.addRequestService(rs);
//        network.serveRequests();
//        network.addRequest(Request.getRandom(3,3));
//        network.serveRequests();
        GraphVisualisation gV = new GraphVisualisation(network);
        gV.start();
//        new Scanner(System.in).nextLine();
//        gV.saveToFile("C:\\Users\\Szuman\\Desktop\\graph.png");
        System.out.println(network.getUsedCapacity());
        System.out.println(network.getMaxSubstrateCapacity());
        System.out.println(network.getLocations());
    }

    public void seq() throws FileNotFoundException {
        Network network = new ParserOPL(filename).parse().getNetwork();
//        Request.resetCounter();
        network.getRequests().replaceAll(r -> Request.getRandom(3,3, 1));
        network.serveRequests();
        GraphVisualisation gV = new GraphVisualisation(network);
        gV.start();
//        gV.saveToFile("C:\\Users\\Szuman\\Desktop\\graph.png");
        System.out.println(network.getUsedCapacity());
        System.out.println(network.getMaxSubstrateCapacity());
        System.out.println(network.getLocations());
    }

    public void lambda() throws FileNotFoundException {
        Network network = new ParserOPL(filename).parse().getNetwork();
        network.getRequests().clear();
        Request.resetCounter();
//        double lambda = 0.5;
        long seed = 1;
//        double[] lambdas = {0.1, 0.3, 0.5, 0.7, 0.9};
        double[] lambdas = {
                0.04,
                0.08,
                0.16, 0.32, 0.64
        };
        List<String> args = new ArrayList<>();
        args.add("next_BooM");
        for (int i = 0; i < lambdas.length; i++) {
            singleLambda(lambdas[i], seed, args);
        }
//            new GraphVisualisation(network).start();
        String[] bundle = new String[args.size()];
        IntStream.range(0, args.size()).forEach(i -> bundle[i] = args.get(i));
        new Thread(() -> javafx.application.Application.launch(Chart.class, bundle)).start();
    }

    private void singleLambda(double lambda, long seed, List<String> bundle) throws FileNotFoundException {
        Network network = new ParserOPL(filename).parse().getNetwork();
        network.getRequests().clear();
        network.setSolver(true);
        Request.resetCounter();
        PriorityQueue<? super RequestEvent> eventsQueue = new PriorityQueue<>((e1, e2) -> e1.time >= e2.time ? 1 : -1);
        vRouter.setRandom(seed);
        vLink.setRandom(seed);
//        for (int i = 0; i < 50; i++)
        eventsQueue.addAll(RequestArrival.initial(network, lambda, seed));
        bundle.add("next_" + lambda);
        List<Double> data = new ArrayList<>();
//        while (network.getServiceRate() > criterion && Request.getCount() != 5000) {
        while (shouldLast(data, network.getServiceRate())) {
//        for (int i = 0; i < 50000; i++) {
            RequestEvent actualEvent = (RequestEvent) eventsQueue.poll();
            List<? extends RequestEvent> newEvents = actualEvent.execute();
            bundle.add(String.valueOf(actualEvent.time));
            bundle.add(String.valueOf(network.getServiceRate()));
            System.out.println("RATE = " + network.getServiceRate() + " --> " + actualEvent);
            eventsQueue.addAll(newEvents);
        }
        System.out.println(network.getServedRequests());
        System.out.println(network.getRequests());
//        bundle.add(String.valueOf(lambda));
//        bundle.add(String.valueOf(network.getServiceRate()));
//        bundle.add(String.valueOf(network.getReqCount()));
        new GraphVisualisation(network).start();
        Request.resetCounter();
    }

    private boolean shouldLast(List<Double> data, double newData) {
        data.add(newData);
        if (data.size() > 10000) {
            data.remove(0);
            double max = data.stream().min(Double::compareTo).get();
            double min = data.stream().max(Double::compareTo).get();
            return max - min > 0.0001;
        }
        return true;
    }

    public void singleLambda() throws FileNotFoundException {
        double lambda = 0.04;
        long seed = 1;
        List<String> args = new ArrayList<>();
        singleLambda(lambda, seed, args);
        String[] bundle = new String[args.size()];
        IntStream.range(0, args.size()).forEach(i -> bundle[i] = args.get(i));
        new Thread(() -> javafx.application.Application.launch(Chart.class, bundle)).start();
    }
}