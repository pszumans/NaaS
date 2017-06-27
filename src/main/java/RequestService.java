import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class RequestService {

    private Request request;
    private int reqIndex;
    private String reqName;
    private boolean input;

    @JsonCreator
    public RequestService(@JsonProperty("request") Request request) {
        this.request = request;
        input = true;
    }

    @JsonCreator
    public RequestService(@JsonProperty("numbers") int[] numbers) {
        this.request = Request.getRandomRequest(numbers[0], numbers[1]);
        input = true;
    }

    @JsonCreator
    public RequestService(@JsonProperty("index") int reqIndex) {
        this.reqIndex = reqIndex;
        input = false;
    }

    @JsonCreator
    public RequestService(@JsonProperty("name") String reqName) {
        this.reqName = reqName;
        this.input = false;
    }

    public Request getRequest() {
        return request;
    }

    public void setRequest(Request request) {
        this.request = request;
    }

    public int getReqIndex() {
        return reqIndex;
    }

    public void setReqIndex(int reqIndex) {
        this.reqIndex = reqIndex;
    }

    public String getReqName() {
        return reqName;
    }

    public void setReqName(String reqName) {
        this.reqName = reqName;
    }

    public boolean isInput() {
        return input;
    }

    public void setInput(boolean input) {
        this.input = input;
    }
}
