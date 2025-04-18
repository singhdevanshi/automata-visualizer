package com.devanshi.automatavisualizer.view;

import com.devanshi.automatavisualizer.model.DFA;
import com.devanshi.automatavisualizer.util.DFAVisualizer;
import com.devanshi.automatavisualizer.util.LanguageParser;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import com.brunomnsilva.smartgraph.graph.Graph;
import com.brunomnsilva.smartgraph.graph.GraphEdgeList;
//firebase imports
// import com.google.auth.oauth2.GoogleCredentials;
// import com.google.cloud.firestore.Firestore;
// import com.google.firebase.cloud.FirestoreClient;
// import com.google.firebase.FirebaseApp;
// import com.google.firebase.FirebaseOptions;

public class Main extends Application {

    private ImageView dfaImageView;
    private TextField patternField;
    private ComboBox<String> patternTypeComboBox;
    private TextField testInputField;
    private Label resultLabel;
    private TextArea transitionTableArea;
    private DFA currentDfa;
    private Path tempImagePath;

    @Override
    public void start(Stage primaryStage) {
        try {
            // Create a temporary file for storing the DFA image
            tempImagePath = Files.createTempFile("dfa_", ".png");
            tempImagePath.toFile().deleteOnExit();
        } catch (IOException e) {
            showError("Initialization Error", "Failed to create temporary file: " + e.getMessage());
            return;
        }

        BorderPane root = new BorderPane();

        // Controls section
        VBox controls = new VBox(10);
        controls.setPadding(new Insets(10));

        Label patternLabel = new Label("Pattern:");
        patternField = new TextField();

        Label typeLabel = new Label("Pattern Type:");
        patternTypeComboBox = new ComboBox<>();
        patternTypeComboBox.getItems().addAll("Ends With", "Contains");
        patternTypeComboBox.setValue("Ends With");

        Button generateButton = new Button("Generate DFA");
        generateButton.setOnAction(e -> generateDFA());

        Label testLabel = new Label("Test String:");
        testInputField = new TextField();

        Button testButton = new Button("Test Input");
        testButton.setOnAction(e -> testInput()); // ✅ Fixed undefined method issue

        resultLabel = new Label();

        transitionTableArea = new TextArea();
        transitionTableArea.setEditable(false);
        transitionTableArea.setWrapText(true);
        transitionTableArea.setPrefRowCount(10);

        controls.getChildren().addAll(patternLabel, patternField, typeLabel,
                patternTypeComboBox, generateButton,
                testLabel, testInputField, testButton, resultLabel,
                transitionTableArea);

        dfaImageView = new ImageView();
        dfaImageView.setPreserveRatio(true);
        dfaImageView.setFitWidth(700);

        root.setLeft(controls);
        root.setCenter(dfaImageView);

        Graph<String, String> g = new GraphEdgeList<>();

        g.insertVertex("A");
        g.insertVertex("B");
        g.insertVertex("C");
        g.insertVertex("D");
        g.insertVertex("E");
        g.insertVertex("F");
        g.insertVertex("G");

        g.insertEdge("A", "B", "1");
        g.insertEdge("A", "C", "2");
        g.insertEdge("A", "D", "3");
        g.insertEdge("A", "E", "4");
        g.insertEdge("A", "F", "5");
        g.insertEdge("A", "G", "6");

        g.insertVertex("H");
        g.insertVertex("I");
        g.insertVertex("J");
        g.insertVertex("K");
        g.insertVertex("L");
        g.insertVertex("M");
        g.insertVertex("N");

        g.insertEdge("H", "I", "7");
        g.insertEdge("H", "J", "8");
        g.insertEdge("H", "K", "9");
        g.insertEdge("H", "L", "10");
        g.insertEdge("H", "M", "11");
        g.insertEdge("H", "N", "12");

        g.insertEdge("A", "H", "0");

        Scene scene = new Scene(root, 1000, 600);
        primaryStage.setTitle("Automata Visualizer");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void generateDFA() {
        String pattern = patternField.getText().trim();
        if (pattern.isEmpty()) {
            showError("Input Error", "Please enter a valid pattern.");
            return;
        }

        try {
            System.out.println("Generating DFA for pattern: " + pattern);

            // Generate DFA based on selected type
            if (patternTypeComboBox.getValue().equals("Ends With")) {
                currentDfa = LanguageParser.createEndsWith(pattern);
            } else {
                currentDfa = LanguageParser.createContains(pattern);
            }

            // ✅ Handle case where DFA generation fails
            if (currentDfa == null) {
                showError("DFA Error", "DFA generation failed: No valid DFA was returned.");
                return;
            }

            // Visualize DFA and save as an image
            DFAVisualizer.visualize(currentDfa, tempImagePath.toString());

            // ✅ Ensure image file is actually created
            if (!Files.exists(tempImagePath)) {
                showError("Visualization Error", "Failed to generate DFA image.");
                return;
            }

            // Display DFA image
            dfaImageView.setImage(new Image(tempImagePath.toUri().toString()));

            // Generate transition table and reorder it
            String table = DFAVisualizer.generateTransitionTable(currentDfa);
            transitionTableArea.setText(reorderTransitionTable(table));

            resultLabel.setText("DFA generated successfully.");
        } catch (Exception e) {
            showError("Generation Error", "Failed to generate DFA: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ✅ Fixed missing testInput() method
    private void testInput() {
        if (currentDfa == null) {
            showError("Test Error", "Please generate a DFA first.");
            return;
        }

        String input = testInputField.getText().trim();
        if (input.isEmpty()) {
            showError("Input Error", "Please enter a string to test.");
            return;
        }

        try {
            boolean accepted = currentDfa.accepts(input);
            resultLabel.setText("Input \"" + input + "\" is " +
                    (accepted ? "ACCEPTED" : "REJECTED") + " by the DFA");
        } catch (Exception e) {
            showError("Test Error", "Test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String reorderTransitionTable(String table) {
        String[] lines = table.split("\n");
        if (lines.length == 0)
            return table;

        String header = lines[0];
        List<String> rows = new ArrayList<>();

        for (int i = 1; i < lines.length; i++) {
            rows.add(lines[i]);
        }

        // Sort rows based on state number (q0, q1, q2...)
        rows.sort(Comparator.comparingInt(row -> {
            String[] parts = row.split("\\s+");
            String state = parts[0];

            if (state.matches("q\\d+")) {
                return Integer.parseInt(state.substring(1));
            }
            return Integer.MAX_VALUE;
        }));

        StringBuilder reorderedTable = new StringBuilder();
        reorderedTable.append(header).append("\n");
        for (String row : rows) {
            reorderedTable.append(row).append("\n");
        }

        return reorderedTable.toString();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @Override
    public void stop() {
        try {
            Files.deleteIfExists(tempImagePath);
        } catch (IOException e) {
            System.err.println("Failed to delete temporary file: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
