package xinth;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class XSPR extends Application
{
    private String rootPath = "DC/";
    private String[] targetTypes = {"SPR", "BTS", "MAP"};

    @Override public void start(Stage stage) throws Exception
    {
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        File rootFS = new File(rootPath);

        for (String targetType : targetTypes) {
            Tab tab = new Tab();
            tab.setText(targetType);
            tabPane.getTabs().add(tab);

            SplitPane splitPane = new SplitPane();
            ScrollPane scrollPane = new ScrollPane();
            tab.setContent(splitPane);

            ArrayList<File> discoveredFiles = new ArrayList<>();
            searchForFiles(rootFS, targetType, discoveredFiles);

            ObservableList<String> baseNameList = FXCollections.<String>observableArrayList();
            for (File file : discoveredFiles) {
                baseNameList.add(file.getPath().replace(rootPath, ""));
            }

            ListView<String> listView = new ListView<>(baseNameList);
            splitPane.getItems().addAll(listView, scrollPane);
            splitPane.setDividerPositions(0.25);

            listView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>()
            {
                public void changed(ObservableValue<? extends String> ov,
                final String oldvalue, final String newvalue)
                {
                    loadItem(scrollPane, targetType, newvalue);
                }
            });

        }

        Scene scene = new Scene(tabPane, 800, 600);
        stage.setScene(scene);
        stage.setTitle("Xinth Sprite Viewer");
        stage.show();
    }

    private void loadItem(ScrollPane scrollPane, String targetType, String targetFile)
    {
        try {
            byte[] fileData = readFileInputStream(rootPath + targetFile);
            FlowPane flowPane = new FlowPane();
            flowPane.setStyle("-fx-background-color:black;");
            switch (targetType) {
                case "SPR":
                    flowPane.prefWidthProperty().bind(scrollPane.widthProperty());
                    SPR spr = new SPR(fileData);
                    for (int f = 0; f < spr.getFrameCount(); f++) {
                        Canvas frame = spr.frameAsImage(f, false, false);
                        flowPane.getChildren().add(frame);
                    }
                    scrollPane.setContent(flowPane);
                break;
                case "BTS":
                    flowPane.setPrefWrapLength(320);
                    BTS bts = new BTS(fileData);
                    int lastFID = 299;
                    for (int f = 0; f < bts.getFrameCount(); f++) {
                        int curFID = bts.getFrameFID(f);
                        while (lastFID < (curFID - 1)) {
                            PixelCanvas filler = new PixelCanvas(32, 32, false);
                            Canvas frame = filler.asCanvas();
                            flowPane.getChildren().add(frame);
                            lastFID++;
                        }
                        lastFID = curFID;
                        Canvas frame = bts.frameAsCanvas(f, false);
                        flowPane.getChildren().add(frame);
                    }
                    scrollPane.setContent(flowPane);
                break;
                case "MAP":
                    byte[] scnData = readFileInputStream(rootPath + targetFile.replace(".MAP", ".SCN"));
                    String[] scnLines = (new String(scnData)).split("\\r\\n");
                    String btsFileName = scnLines[0].toUpperCase();
                    String mapName = scnLines[2];

                    byte[] btsData = readFileInputStream(rootPath + "SCENARIO/" + btsFileName);
                    BTS mapBTS = new BTS(btsData);

                    MAP map = new MAP(fileData, mapBTS);
                    Canvas mapCanvas = map.asCanvas();
                    scrollPane.setContent(mapCanvas);
                break;
            }
        } catch(IOException e) {
            System.out.println("File could not be read");
        }
    }

    public static void main(String[] args)
    {
        launch(args);
    }

    public static void searchForFiles(File root, String targetType, List<File> datOnly) {
        if(root == null || datOnly == null) return; //just for safety
        if(root.isDirectory()) {
            for(File file : root.listFiles()) {
                searchForFiles(file, targetType, datOnly);
            }
        } else if(root.isFile() && root.getName().toLowerCase().endsWith("." + targetType.toLowerCase())) {
            datOnly.add(root);
        }
    }

    private static byte[] readFileInputStream(String filename) throws IOException {
        byte [] buffer =null;
        File a_file = new File(filename);
        try
        {
            FileInputStream fis = new FileInputStream(filename);
            int length = (int)a_file.length();
            buffer = new byte [length];
            fis.read(buffer);
            fis.close();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        return buffer;
    }
}