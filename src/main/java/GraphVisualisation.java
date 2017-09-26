import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.swing.mxGraphComponent;
import org.jgrapht.Graph;
import org.jgrapht.ListenableGraph;
import org.jgrapht.ext.JGraphXAdapter;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Created by Szuman on 01.04.2017.
 */
public class GraphVisualisation extends JApplet {

    private static final long serialVersionUID = 2202072534703043194L;
    private static final Dimension DEFAULT_SIZE = new Dimension(530, 320);

//    private JGraphXAdapter<String, DefaultEdge> jgxAdapter;
    private Graph graph;
    private String text;

    public GraphVisualisation(ListenableGraph graph) throws HeadlessException {
        this.graph = graph;
        init();
    }

    public GraphVisualisation(ListenableGraph graph, String text) throws HeadlessException {
        this.graph = graph;
        this.text = text;
        init();
    }

    @Override
    public void start()
    {
        JFrame frame = new JFrame();
        frame.getContentPane().add(this);
        if (graph instanceof Network)
            frame.setTitle(String.format("Network Visualisation %s [Y=%d, Zmin=%d, t=%ss, rate=%s]", (text != null) ? text : "", ((Network) graph).getUsedCapacity(), ((Network) graph).getMaxMinSubstrateCapacity(), ((Network) graph).getSolver().getFullTime(), ((Network) graph).getServiceRate()));
        if (graph instanceof Request)
            frame.setTitle(((Request) graph).getName());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    public void saveToFile(String filename) {
//        setSize(getPreferredSize());
//        layoutComponent(this);
//        BufferedImage img = new BufferedImage(getWidth(), getHeight(), BufferedImage.TRANSLUCENT);
//        CellRendererPane crp = new CellRendererPane();
//        crp.add(this);
//        crp.paintComponent(img.createGraphics(), this, crp, getBounds());

        BufferedImage img = new BufferedImage(getWidth(), getHeight(), BufferedImage.TRANSLUCENT);
        Graphics2D g2d = (Graphics2D) img.getGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        paintAll(g2d);

        try {
            ImageIO.write(img, "png", new File(filename));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private static void layoutComponent(Component c) {
        synchronized (c.getTreeLock()) {
            c.doLayout();
            if (c instanceof Container) {
                for (Component child : ((Container) c).getComponents()) {
                    layoutComponent(child);
                }
            }
        }
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
