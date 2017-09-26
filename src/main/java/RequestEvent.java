import lombok.Getter;

import java.util.Collections;
import java.util.List;
import java.util.Random;

@Getter
public abstract class RequestEvent {

    private static Random RANDOM;//implements Comparable<RequestEvent> {

    protected static int numbersOfRouters = 5;
    protected static int numbersOfLinks = 7;

    protected double time;
    protected Random rand;
    protected long seed;

    protected final Network network;
    protected Request request;
    protected
//    final
    double lambda;

    protected List<? extends RequestEvent> execute() {
        return Collections.emptyList();
    }

    public RequestEvent(Network network, double lambda) {
        this.network = network;
        this.lambda = lambda;
        this.seed = Test.SEED;
        this.rand = RANDOM;
    }

    private RequestEvent(Network network, Request request, double lambda) {
        this.network = network;
        this.request = request;
        this.lambda = lambda;
        time += countTime();
    }

    protected RequestEvent(Network network, double lambda, long seed) {
        this.network = network;
        this.lambda = lambda;
        this.seed = seed;
    }

    protected RequestEvent(Network network, double lambda, Random rand) {
        this.network = network;
        this.lambda = lambda;
        this.rand = rand;
    }

    protected RequestEvent(RequestEvent event) {
        this(event.network, event.lambda, event.seed);
        rand = event.rand;
        time = event.time;
//        time += countTime();
    }

    protected void initRand(long seed) {
        rand = new Random(seed);
    }

    protected double countTime() {
        return -Math.log(1 - rand.nextDouble()) / lambda;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s %s", time, getClass().getName(), request);
    }

    public static void setRandom(Random random) {
        RANDOM = random;
    }
}
