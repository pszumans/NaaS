import java.util.List;

public class RequestRelease extends RequestEvent {

    public RequestRelease(RequestArrival arrival) {
        super(arrival);
        request = arrival.request;
        lambda = 0.001;
        time += countTime();
    }

    private void release() {
        network.releaseRequest(request);
    }

    @Override
    public List<? extends RequestEvent> execute() {
        release();
        return super.execute();
    }
}