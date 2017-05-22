import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.swing.mxGraphComponent;
import org.jgrapht.Graph;
import org.jgrapht.ListenableGraph;
import org.jgrapht.ext.JGraphXAdapter;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Szuman on 01.04.2017.
 */
public class GraphVisualisation extends JApplet {

    private static final long serialVersionUID = 2202072534703043194L;
    private static final Dimension DEFAULT_SIZE = new Dimension(530, 320);

//    private JGraphXAdapter<String, DefaultEdge> jgxAdapter;
    private Graph graph;

    public GraphVisualisation(ListenableGraph graph) throws HeadlessException {
        this.graph = graph;
        init();
    }

    @Override
    public void start()
    {
        JFrame frame = new JFrame();
        frame.getContentPane().add(this);
        frame.setTitle("Network Visualisation");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init()
    {
        // create a visualization using JGraph, via an adapter
        JGraphXAdapter jgxAdapter = new JGraphXAdapter(graph) {
            @Override
            public String convertValueToString(Object cell) {
                Object value = ((mxCell) cell).getValue();
                if (value instanceof Visualisable) {
                    return ((Visualisable) value).getVisualText();
                }
                return super.convertValueToString(cell);
            }
        };

//        System.out.println(jgxAdapter.getCellStyle(jgxAdapter.getCellToVertexMap()));
//        graph.vertexSet().forEach(v ->
//        jgxAdapter.setCellStyle("fillColor=#CCCC00", jgxAdapter.getCellToVertexMap().keySet().toArray());
//        );
//        System.out.println(jgxAdapter.getCellStyle(jgxAdapter.getCellToVertexMap()));

        getContentPane().add(new mxGraphComponent(jgxAdapter));
        resize(DEFAULT_SIZE);

        // positioning via jgraphx layouts
        mxHierarchicalLayout layout = new mxHierarchicalLayout(jgxAdapter,  SwingConstants.WEST);
        layout.setInterRankCellSpacing(100);
        layout.setIntraCellSpacing(100);
        layout.setIntraCellSpacing(100);
//        layout.setParallelEdgeSpacing(100);
//        mxFastOrganicLayout layout = new mxFastOrganicLayout(jgxAdapter);
//        layout.setResetEdges(false);
//        layout.setOrientation(SwingConstants.VERTICAL);
        layout.execute(jgxAdapter.getDefaultParent());

        // that's all there is to it!...
    }
}
