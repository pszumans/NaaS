import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public class RequestService {

    private Request request;
    private int reqIndex;
    private String reqName;
    private boolean input;

    public RequestService(Request request) {
        this.request = request;
        input = true;
    }

    public RequestService(int... numbers) {
        this.request = Request.getRandomRequest(numbers[0], numbers[1]);
        input = true;
    }

    public RequestService(int reqIndex) {
        this.reqIndex = reqIndex;
        input = false;
    }

    public RequestService(String reqName) {
        this.reqName = reqName;
        this.input = false;
    }

    @JsonCreator
    public static RequestService JsonParser(@JsonProperty("input") boolean input, @JsonProperty("data") Object data) {
        if (input) {
            if (data instanceof LinkedHashMap) {
                return new RequestService(new ObjectMapper().convertValue(data, Request.class));
            } else if (data instanceof ArrayList)
                return new RequestService(Request.getRandomRequest(((ArrayList<Integer>) data).get(0), ((ArrayList<Integer>) data).get(1)));
        }
        else
            if (data instanceof Integer) {
                return new RequestService((int) data);
            } else if (data instanceof String) {
                return new RequestService(data.toString());
            }
            return null;
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
