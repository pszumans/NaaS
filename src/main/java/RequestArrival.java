import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class RequestArrival extends RequestEvent {

    public RequestArrival(Network network, double alpha, long seed) {
        super(network, alpha, seed);
    }

//        public RequestArrival(Network network, double lambda, long seed) {
//            super(network, lambda, seed);
//        }

    public RequestArrival(RequestArrival arrival) {
        super(arrival);
        request = Request.getRandom(numbersOfRouters, numbersOfLinks, seed);
        time += countTime();
    }

    public static List<? extends RequestEvent> initial(Network network, double alpha, long seed) {
        RequestArrival arrival = new RequestArrival(network, alpha, seed);
        arrival.request = Request.getRandom(numbersOfRouters, numbersOfLinks, seed);
        arrival.initRand(seed);
        return Arrays.asList(arrival, new RequestRelease(arrival));
    }

    private List<? extends RequestEvent> serve() {
//            Set<? extends RequestEvent> events = new TreeSet<>();
        network.serveRequest(request);
        RequestArrival newArrival = new RequestArrival(this);
        return Arrays.asList(newArrival, new RequestRelease(newArrival));
//            events.add(newArrival);
//            events.add(new RequestRelease(newArrival));
//        return new TreeSet<>(Arrays.asList(newArrival, new RequestRelease(newArrival)));
    }

    @Override
    public List<? extends RequestEvent> execute() {
        return serve();
    }
}
