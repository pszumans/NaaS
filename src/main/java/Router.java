import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Getter @Setter
public abstract class Router implements Serializable {

	protected String name;
	protected int power; // residual processing power
	protected int memory; // residual memory

	protected Router(String name, int B, int M) {
		this.name = name;
		power = B;
		memory = M;
	}

	protected Router(String name, int B, int M, int... L) {
		this.name = name;
		power = B;
		memory = M;
		locate(L);
	}

	protected Router(String name) {
		this.name = name;
	}

	public String getParam(int i) {
		StringBuilder sb = new StringBuilder(name + " ");
		if (i == 0)
			sb.append(power + "\n");
		else if (i == 1)
			sb.append(memory + "\n");
		return sb.toString();
	}

	protected void locate(int... L) {
	}

	@Override
	public String toString() {
		return String.format("%s(%d %d", name, power, memory);
	}

    protected String toOPL() {
		return String.format("<%s %d %d", name, power, memory);
    }
}
