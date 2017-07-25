import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.Axis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

@Getter
public class Chart extends Application {

//    public static void main(String[] args) {
//        launch(args);
//    }

//    @FXML
    private LineChart<Number, Number> chart;
    private XYChart.Series<Number, Number> series;

    @Override
    public void start(Stage primaryStage) {
        try {

            primaryStage.setTitle("Rate Chart");
            primaryStage.setResizable(false);
            primaryStage.setOnCloseRequest(e -> Platform.exit());

            Axis x = new NumberAxis();
            x.setLabel("Lambda");
            Axis y = new NumberAxis();
            y.setLabel("Rate");
            chart = new LineChart<>(x,y);
            chart.setCreateSymbols(false);

            List<XYChart.Series> ser = new ArrayList<>();

            Iterator<String> it = getParameters().getRaw().iterator();

            XYChart.Series s = null;

            while (it.hasNext()) {
                String time = it.next();
                if (time.startsWith("next")) {
                    s = new XYChart.Series();
                    s.setName(String.valueOf(time.split("_")[1]));
                    ser.add(s);
                    time = it.next();
                }
                String rate = it.next();
                XYChart.Data data = new XYChart.Data<>(Double.valueOf(time), Double.valueOf(rate));
                s.getData().add(data);
            }

            primaryStage.setScene(new Scene(chart, 3000, 3000));
            primaryStage.setResizable(true);
            ser.forEach(ss -> chart.getData().add(ss));
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
