import java.util.*;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mxgraph.model.mxIGraphModel;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.graph.SimpleWeightedGraph;

public abstract class Link extends DefaultWeightedEdge {

	/**
	 * 
	 */
	protected static final long serialVersionUID = 1L;

	protected String name;
	protected int capacity;

	public Link(Router r1, Router r2, int C, Graph<? super Router, ? super Link> graph) {
		super();
		capacity = C;
		name = r1.getName() + " " + r2.getName();
		graph.addEdge(r1, r2, this);
	}

	public Link() {
		super();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setName() {
		this.name = getSource().getName() + " " + getTarget().getName();
	}

	public int getCapacity() {
		return capacity;
	}

	public String getParam() {
		return name + " " + capacity + "\n";
	}

	public String getSigned() {
		return "(" + getSource().getName() + ", " + getTarget().getName() + ")";
	}

	@Override
	protected double getWeight() {
		return super.getWeight();
	}

	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}

	@Override
	public Router getSource() {
		return (Router) super.getSource();
	}

	@Override
	protected Router getTarget() {
		return (Router) super.getTarget();
	}

//	public String writeCapacity() {
//		return routers.get(0) + " " + routers.get(1) + " " + capacity;
//	}

	public String toOPL() {
		return String.format("<%s %d>\n", name, capacity);
	}

	@Override
	public String toString() {
		return "(" + getSource() + ", " + getTarget() + ", " + capacity + ")";
	}

	public boolean containsRouter(Router router) {
		return router.equals(getSource()) || router.equals(getTarget());
	}

	protected void assign(Router source, Router target) {
		SimpleGraph graph = new SimpleGraph(vLink.class);
		graph.addVertex(source);
		graph.addVertex(target);
		graph.addEdge(source, target, this);
	}
}