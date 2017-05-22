import ilog.concert.IloException;
import ilog.cplex.IloCplex;
import ilog.opl.*;

/**
 * Created by Szuman on 13.05.2017.
 */
public class SolverOPL {

    private String DATADIR = "C:\\Users\\Szuman\\Desktop\\NaaS";

    public SolverOPL() throws IloException {

        IloOplFactory.setDebugMode(true);
        IloOplFactory oplF = new IloOplFactory();
        IloOplErrorHandler errHandler = oplF.createOplErrorHandler();
        IloOplSettings settings = oplF.createOplSettings(errHandler);

        // make master model
        IloCplex masterCplex = oplF.createCplex();
        masterCplex.setOut(null);

        IloOplRunConfiguration masterRC0 = oplF.createOplRunConfiguration(
                DATADIR +
                "/NaaS.mod", DATADIR +
                "/NaaS.dat");
        masterRC0.setCplex(masterCplex);
        IloOplDataElements masterDataElements = masterRC0.getOplModel()
                .makeDataElements();

        masterCplex.clearModel();

        IloOplRunConfiguration masterRC = oplF.createOplRunConfiguration(
                masterRC0.getOplModel().getModelDefinition(),
                masterDataElements);
        masterRC.setCplex(masterCplex);
        masterRC.getOplModel().generate();

        int status;

        System.out.println("Solve master.");
        if (masterCplex.solve()) {
            double curr = masterCplex.getObjValue();
            System.out.println("OBJECTIVE: " + curr);
            status = 0;
        } else {
            System.out.println("No solution!");
            status = 1;
        }

    }
}
