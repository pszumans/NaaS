import lombok.Getter;

@Getter
public abstract class Solver {

    protected Network network;
    protected double time;
    protected double fullTime;

    protected Solver(Network network) {
        this.network = network;
    }

    protected boolean serveRequest(Request request) {
        request.setServed(true);
        network.addServedRequest(request);
        if (!network.isRequestInService(request))
            network.addRequest(request);
        network.countLoadRates();
        Logger.log(request);
        return true;
    }

    protected void releaseRequest(Request request) {
        network.removeRequest(request);
    }

    protected void solve() {}

}
