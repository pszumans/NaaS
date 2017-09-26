import java.util.*;

public class RequestArrival extends RequestEvent {

    public RequestArrival(Network network, double lambda, long seed) {
        super(network, lambda, seed);
    }

    public RequestArrival(Network network, double lambda, Random rand) {
        super(network, lambda, rand);
    }

    public RequestArrival(Network network, double lambda) {
        super(network, lambda);
    }

//        public RequestArrival(Network network, double lambda, long seed) {
//            super(network, lambda, seed);
//        }

    public RequestArrival(RequestArrival arrival) {
        super(arrival);
        if (Request.FULL_RANDOM)
            request = Request.getFullRandom(numbersOfRouters, numbersOfLinks, seed);
        else
            request = Request.getRandom(3, 3, seed);
        time += countTime();
    }

    public static List<? extends RequestEvent> initial(Network network, double lambda, long seed) {
        RequestArrival arrival = new RequestArrival(network, lambda);
        if (Request.FULL_RANDOM)
            arrival.request = Request.getFullRandom(numbersOfRouters, numbersOfLinks, seed);
        else
            arrival.request = Request.getRandom(3, 3, seed);//        arrival.initRand(seed);
        return Arrays.asList(arrival, new RequestRelease(arrival));
    }

    private List<? extends RequestEvent> serve() {
//        boolean isServed =
        network.serveRequest(request);
//        if (Network.IS_HEURISTIC && !isServed) {
//            network.setSolver(false);
//            SolverOPL sol = (SolverOPL)network.getSolver();
//            network.serveRequest(request);
//            sol.end();
//            network.setSolver(true);
//        }
        RequestArrival newArrival = new RequestArrival(this);
        return Arrays.asList(newArrival, new RequestRelease(newArrival));
    }

    @Override
    public List<? extends RequestEvent> execute() {
        return serve();
    }
}
