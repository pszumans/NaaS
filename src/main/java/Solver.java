import lombok.Getter;

@Getter
public abstract class Solver {

    protected final Network network;
    protected double time;

    protected Solver(Network network) {
        this.network = network;
    }

    protected boolean serveRequest(Request request) {
        request.setServed(true);
        network.addServedRequest(request);
        if (!network.isRequestInService(request))
            network.addRequest(request);
//        network.getRequests().add(request);
//        network.getRequests().remove(request);
        System.out.println(request);
        return true;
    }

    protected void releaseRequest(Request request) {
        network.removeRequest(request);
    }

    protected void solve() {}

}
