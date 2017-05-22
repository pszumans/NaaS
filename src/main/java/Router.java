import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public abstract class Router {

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

	protected void locate(int... L) {
	}

	protected Router(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

//	public void setName(String name) {
//		this.name = name;
//	}

	public int getPower() {
		return power;
	}

	public void setPower(int power) {
		this.power = power;
	}

	public int getMemory() {
		return memory;
	}

	public void setMemory(int memory) {
		this.memory = memory;
	}

	public String getParam(int i) {
		StringBuilder sb = new StringBuilder(name + " ");
		if (i == 0)
			sb.append(power + "\n");
		else if (i == 1)
			sb.append(memory + "\n");
		return sb.toString();
	}

	@Override
	public String toString() {
		return String.format("%s(B=%d M=%d", name, power, memory);
	}

    protected String toOPL() {
		return String.format("<%s %d %d", name, power, memory);
    }
}
