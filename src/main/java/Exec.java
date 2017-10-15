import lombok.Getter;

import java.io.FileNotFoundException;
import java.util.*;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;

public class Exec {

    public static String[] Pattern = {"Simulation Time", "RATE", "Solution Time", "Total Time", "Requests Allocated", "Used Capacity", "Request Status", "Max Min Capacity", "CLoad\tPLoad\tMload\tFullLoad"};
    public static String CSV = Log.DIR + "%s_%s_CSV.csv";
    public static String LOG = Log.DIR + "%s_%s_LOG.csv";
    private String filename;
    private String name;
    private List<Result> results;

    private String properties;

    private int LIMITS[][] = {
            {2000, 1000, 1000, 1000}, // ok
            {597, 1000, 1015, 1027}, //  ok   BIG
            {705, 498, 1000, 1050}, // nie ok
//            {2000, 736, 1000, 1000}, //
//            {2000, 1000, 1000, 1000}, // SMALL OK
//            {1000, 498, 1027, 1037},//nie ok 25 - 1000
//            {1000, 1000, 1000, 1000}, //
//            {2000, 1000, 1000, 1000}, // SMALL FULL
//            {2000, 1000, 1136, 1037}, //
    };

    private int getLimit(double lambda) {
        int i, j;
        if (SEED == 1)
            i = 0;
        else if (SEED == 11)
            i = 1;
        else if (SEED == 111)
            i = 2;
        else
            i = 3;

        if (lambda == 0.2)
            j = 0;
        else if (lambda == 0.25)
            j = 1;
        else if (lambda == 0.30)
            j = 2;
        else if (lambda == 0.35)
            j = 3;
        else j = 4;

        return LIMITS[i][j];
    }


    public static long SEED;

    public Exec(String filename) {
        this.filename = filename;
        name = filename.replace(SolverOPL.DATADIR, "").replace(".dat", "");
        properties = ">>> seed = " + Exec.SEED + ", topology = " + ((Request.FULL_RANDOM) ? "random" : "3x3") + ", variance = " + ((vLink.BIG_VARIANCE) ? "big" : "small") + ((Network.IS_HEURISTIC) ? ", HEURISTIC" : ", CPLEX") + " <<<";
        setRandom(SEED);
    }

    public static void setRandom(long seed) {
        setRandom(new Random(seed));
    }

    private static void setRandom(Random random) {
        Request.setRandom(random);
        vRouter.setRandom(random);
        vLink.setRandom(random);
        RequestEvent.setRandom(random);
    }

    public void run() throws FileNotFoundException {
//        Network network = new ParserOPL(filename).parse().getNetwork();
//        Network network = new ParserOPL(filename).parse().getNetwork();
//        Network network = new ParserOPL("D:\\NaaS\\NaaS.dat").parse().getNetwork();
        Network network = new ParserOPL("D:\\NaaS\\error.dat").parse().getNetwork();
        network.serveRequests();
        System.out.println("DONE");
        new GraphVisualisation(network).start();
        new Scanner(System.in).nextLine();
        GraphVisualisation gV = new GraphVisualisation(network);
        gV.start();
    }

    public void seq() throws FileNotFoundException {
        Network network = new ParserOPL(filename).parse().getNetwork();
//        Request.resetCounter();
        network.getRequests().replaceAll(r -> Request.getRandom(3,3, 1));
        network.serveRequests();
        GraphVisualisation gV = new GraphVisualisation(network);
        gV.start();
//        gV.saveToFile("C:\\Users\\Szuman\\Desktop\\graph.png");
        Log.log(network.getUsedCapacity());
        Log.log(network.getMaxMinSubstrateCapacity());
        Log.log(network.getLocations());
    }


    public void singleLambda(double lambda) throws FileNotFoundException {
        Log.consoleLog = String.format(Log.consoleLog, name, (int) (lambda * 100));
        SolverOPL.DEBUG = String.format(SolverOPL.DEBUG, name, (int) (lambda * 100));
        Network network = new ParserOPL(filename).parse().getNetwork();
        network.getRequests().clear();
        network.setSolver();
        Request.resetCounter();
        PriorityQueue<? super RequestEvent> eventsQueue = new PriorityQueue<>((e1, e2) -> e1.time >= e2.time ? 1 : -1);
        eventsQueue.addAll(RequestArrival.initial(network, lambda, SEED));
        results = new ArrayList<>();
//        Log.log(Log.DIR);
        Log.log("### NAME = " + name + " ###");
        Log.log(properties);
        Log.log("*** LAMBDA = " + lambda + " ***");

//        int[] reqsPerRCount = new int[4];//{0, 0, 0, 0}; // 2,3,4,5
//        int[] reqsPerLCount = new int[7];//{0, 0, 0, 0, 0, 0, 0}; // 1,2,3,4,5,6,7

        while (shouldLast(network, lambda)) {
            RequestEvent actualEvent = (RequestEvent) eventsQueue.poll();
            List<? extends RequestEvent> newEvents = actualEvent.execute();
            Log.log("RATE = " + network.getServiceRate() + " --> " + actualEvent);
            eventsQueue.addAll(newEvents);
            if (actualEvent instanceof RequestArrival) {

                if (!actualEvent.getRequest().isServed()) {
                    break;
                }

                Result result = new Result(actualEvent, network);
                Log.toCSV(String.format(CSV, name, (int) (lambda * 100)), result.printCSV());
//                        actualEvent.getTime(), network.getServiceRate(), network.getSolver().getTime(), network.getSolver().getFullTime(), network.getRequests().size(), network.getUsedCapacity(), actualEvent.getRequest().isServed(), network.getMaxMinSubstrateCapacity(), network.getLoadRate());

                if (network.getReqCount() > 100) results.remove(0);
                results.add(result);

//                reqsPerRCount[actualEvent.request.vertexSet().size() - 2] = reqsPerRCount[actualEvent.request.vertexSet().size() - 2] + 1;
//                reqsPerLCount[actualEvent.request.edgeSet().size() - 1] = reqsPerLCount[actualEvent.request.edgeSet().size() - 1] + 1;

                if (actualEvent.getRequest().edgeSet().size() != actualEvent.getRequest().getLinks().size()) {
                    System.out.println("BANG");
                    new Scanner(System.in).nextLine();
                }

                if ((int)network.getRouters().stream().filter(r -> r.getVRouters() != null && r.getVRouters().containsKey(actualEvent.getRequest().getIndex())).count() != actualEvent.getRequest().vertexSet().size() && actualEvent.getRequest().isServed()) {
                    System.out.println("BANG2");
                    System.out.println(network.getRouters().stream().filter(r -> r.getVRouters() != null && r.getVRouters().containsKey(actualEvent.getRequest().getIndex())).count());
                    System.out.println(actualEvent.getRequest().vertexSet().size());
                    new Scanner(System.in).nextLine();
                }

            }
        }
        Log.log(network.getServedRequests());
        Log.log(network.getRequests());
//        Log.log("REQUESTS SERVED TILL BLOCK = " + (network.getReqCount()-1));
//        results.remove(results.size()-1);
        Log.toResults(name, lambda, new Result(results).printResult());
//        new GraphVisualisation(network).start();
        Request.resetCounter();

//        double[] routersCountRate = new double[reqsPerRCount.length];
//        double[] linksCountRate = new double[reqsPerLCount.length];
//        for (int i = 0; i < reqsPerRCount.length; i++) {
//            routersCountRate[i] = (double) reqsPerRCount[i] / network.getReqCount();
//        }
//        for (int i = 0; i < reqsPerLCount.length; i++) {
//            linksCountRate[i] = (double) reqsPerLCount[i] / network.getReqCount();
//        }
//        Log.log("ROUTERS COUNT RATE {2,3,4,5} = " + Arrays.toString(routersCountRate));
//        Log.log("LINKS COUNT RATE {1,2,3,4,5,6,7} = " + Arrays.toString(linksCountRate));
//
        if (network.getSolver() instanceof SolverOPL)
            ((SolverOPL)network.getSolver()).end();
    }


    private boolean shouldLast(Network network, double lambda) {
//        int limit = 2000 - 1;
        int limit = 1000 - 1;
//        int limit = getLimit(lambda) - 1;
        double criterion = network.getReqCount() < 1000 ? 0.001 : 0.01;
        if (network.getReqCount() > limit) {
            return false;
        }
//        if (Network.IS_HEURISTIC) {
//            results.remove(0);
//            return true;
//        }
        if (network.getReqCount() > 99) {
            double max = results.stream().mapToDouble(Result::getServiceRate).max().getAsDouble();
            double min = results.stream().mapToDouble(Result::getServiceRate).min().getAsDouble();
            Log.log("MAX: " + max + " vs MIN: " + min + " = " + (max - min));
//            boolean result = (max == min) ? true : max - min > criterion;
//            return result;
        }
        return true;
    }

    public void singleLambda() throws FileNotFoundException {
        double lambda = 0.04;
        long seed = 1;
        singleLambda(lambda);
    }

    @Getter
    private class Result {

        private double eventTime;
        private double serviceRate;
        private double solverTime;
        private double solverFullTime;
        private double requestsSize;
        private int usedCapacity;
        private boolean eventStatus;
        private double maxMinCapacity;
        private double capacityLoadRate;
        private double powerLoadRate;
        private double memoryLoadRate;
        private double loadRate;


        private Result(List<Result> results) {
            serviceRate = getAverage(results, Result::getServiceRate);
            solverTime = getAverage(results, Result::getSolverTime);
            requestsSize = getAverage(results, Result::getRequestsSize);
            usedCapacity = getAverage(results, Result::getUsedCapacity);
            maxMinCapacity = getAverage(results, Result::getMaxMinCapacity);
            capacityLoadRate = getAverage(results, Result::getCapacityLoadRate);
            powerLoadRate = getAverage(results, Result::getPowerLoadRate);
            memoryLoadRate = getAverage(results, Result::getMemoryLoadRate);
            loadRate = getAverage(results, Result::getLoadRate);
        }

        private Result(RequestEvent actualEvent, Network network) {
            eventTime = actualEvent.getTime();
            serviceRate = network.getServiceRate();
            solverTime = network.getSolver().getTime();
            solverFullTime = network.getSolver().getFullTime();
            requestsSize = network.getRequests().size();
            usedCapacity = network.getUsedCapacity();
            eventStatus = actualEvent.getRequest().isServed();
            maxMinCapacity = network.getMaxMinSubstrateCapacity();
            capacityLoadRate = network.getCapacityLoadRate();
            powerLoadRate = network.getPowerLoadRate();
            memoryLoadRate = network.getMemoryLoadRate();
            loadRate = (capacityLoadRate + powerLoadRate + memoryLoadRate) / 3;
        }

        private double getAverage(List<Result> results, ToDoubleFunction<? super Result> function) {
            return results.stream().mapToDouble(function).sum() / results.size();
        }

        private int getAverage(List<Result> results, ToIntFunction<? super Result> function) {
            return results.stream().mapToInt(function).sum() / results.size();
        }

        private String printResult() {
            return serviceRate + "\t"
                    + solverTime + "\t"
                    + requestsSize + "\t"
                    + usedCapacity + "\t"
                    + maxMinCapacity + "\t"
                    + capacityLoadRate + "\t"
                    + powerLoadRate + "\t"
                    + memoryLoadRate + "\t"
                    + loadRate;
        }

        private String printCSV() {
            return eventTime + "\t"
                    + serviceRate + "\t"
                    + solverTime + "\t"
                    + solverFullTime + "\t"
                    + requestsSize + "\t"
                    + usedCapacity + "\t"
                    + eventStatus + "\t"
                    + maxMinCapacity + "\t"
                    + capacityLoadRate + "\t"
                    + powerLoadRate + "\t"
                    + memoryLoadRate + "\t"
                    + loadRate;
        }

    }
}