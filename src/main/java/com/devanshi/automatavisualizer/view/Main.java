package com.devanshi.automatavisualizer.view;

import com.devanshi.automatavisualizer.model.DFA;
import com.devanshi.automatavisualizer.util.DFAVisualizer;
import com.devanshi.automatavisualizer.util.LanguageParser;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Main extends Application {

    private ImageView dfaImageView;
    private TextField patternField;
    private ComboBox<String> patternTypeComboBox;
    private TextField testInputField;
    private Label resultLabel;
    private TextArea transitionTableArea;
    private DFA currentDfa;
    private Path tempImagePath;
    private TabPane visualizationTabPane;
    private Label statusLabel;

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
        root.setStyle("-fx-font-family: 'Segoe UI', Arial, sans-serif; -fx-background-color: #f5f5f7;");
        
        // Add header with title
        HBox header = createHeader();
        root.setTop(header);

        // Main content area with split panes
        SplitPane mainContent = new SplitPane();
        
        // Left side - Controls
        VBox controlPanel = createControlPanel();
        
        // Right side - Visualization with tabs
        visualizationTabPane = createVisualizationPane();
        
        mainContent.getItems().addAll(controlPanel, visualizationTabPane);
        mainContent.setDividerPositions(0.3);
        
        root.setCenter(mainContent);
        
        // Status bar at bottom
        HBox statusBar = createStatusBar();
        root.setBottom(statusBar);
        
        Scene scene = new Scene(root, 1200, 800);
        
        // Apply inline styles rather than external CSS file
        applyStyles();
        
        primaryStage.setTitle("Automata Visualizer");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void applyStyles() {
        // Apply styles directly to elements instead of using CSS file
        // This is done in the specific creation methods
    }

    private HBox createHeader() {
        HBox header = new HBox();
        header.setStyle("-fx-background-color: #3a86ff; -fx-padding: 15px; -fx-background-radius: 0 0 10 10;");
        
        Label title = new Label("Automata Visualizer");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 24px; -fx-font-weight: bold;");
        
        header.getChildren().add(title);
        header.setAlignment(Pos.CENTER_LEFT);
        
        return header;
    }

    private VBox createControlPanel() {
        VBox controlPanel = new VBox(15);
        controlPanel.setStyle("-fx-background-color: white; -fx-background-radius: 8px; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 3); -fx-padding: 15px; -fx-spacing: 10px;");
        controlPanel.setPadding(new Insets(20));
        
        // Pattern Input Section
        VBox patternSection = new VBox(10);
        Label patternTitle = new Label("Define Pattern");
        patternTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #3a86ff;");
        
        Label patternLabel = new Label("Pattern:");
        patternField = new TextField();
        patternField.setPromptText("Enter pattern...");
        patternField.setStyle("-fx-background-color: #f0f0f0; -fx-background-radius: 5px; " +
                "-fx-border-radius: 5px; -fx-border-color: #e0e0e0; -fx-padding: 8px;");
        
        Label typeLabel = new Label("Pattern Type:");
        patternTypeComboBox = new ComboBox<>();
        patternTypeComboBox.getItems().addAll("Ends With", "Contains");
        patternTypeComboBox.setValue("Ends With");
        patternTypeComboBox.setMaxWidth(Double.MAX_VALUE);
        patternTypeComboBox.setStyle("-fx-background-color: #f0f0f0; -fx-background-radius: 5px; " +
                "-fx-border-radius: 5px; -fx-border-color: #e0e0e0; -fx-padding: 8px;");
        
        Button generateButton = new Button("Generate DFA");
        generateButton.setMaxWidth(Double.MAX_VALUE);
        generateButton.setOnAction(e -> generateDFA());
        generateButton.setStyle("-fx-background-color: #3a86ff; -fx-text-fill: white; -fx-font-weight: bold; " +
                "-fx-background-radius: 5px; -fx-padding: 10px 20px; -fx-cursor: hand;");
        
        patternSection.getChildren().addAll(patternTitle, patternLabel, patternField, typeLabel, patternTypeComboBox, generateButton);
        
        // Test Input Section
        VBox testSection = new VBox(10);
        Label testTitle = new Label("Test String");
        testTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #3a86ff;");
        
        Label testLabel = new Label("Input String:");
        testInputField = new TextField();
        testInputField.setPromptText("Enter test string...");
        testInputField.setStyle("-fx-background-color: #f0f0f0; -fx-background-radius: 5px; " +
                "-fx-border-radius: 5px; -fx-border-color: #e0e0e0; -fx-padding: 8px;");
        
        Button testButton = new Button("Test Input");
        testButton.setStyle("-fx-background-color: #8ecae6; -fx-text-fill: #023047; -fx-font-weight: bold; " +
                "-fx-background-radius: 5px; -fx-padding: 10px 20px; -fx-cursor: hand;");
        testButton.setMaxWidth(Double.MAX_VALUE);
        testButton.setOnAction(e -> testInput());
        
        resultLabel = new Label();
        resultLabel.setWrapText(true);
        
        testSection.getChildren().addAll(testTitle, testLabel, testInputField, testButton, resultLabel);
        
        // Add sections to control panel
        controlPanel.getChildren().addAll(patternSection, new Separator(), testSection);
        
        return controlPanel;
    }

    private TabPane createVisualizationPane() {
        TabPane tabPane = new TabPane();
        
        // DFA Visualization Tab
        Tab visualizationTab = new Tab("DFA Visualization");
        visualizationTab.setClosable(false);
        
        StackPane visualizationPane = new StackPane();
        visualizationPane.setStyle("-fx-background-color: white; -fx-background-radius: 8px; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 1); -fx-padding: 15px;");
        
        dfaImageView = new ImageView();
        dfaImageView.setPreserveRatio(true);
        dfaImageView.setFitWidth(800);
        
        Label placeholderLabel = new Label("Generate a DFA to see visualization");
        placeholderLabel.setStyle("-fx-text-fill: #808080; -fx-font-style: italic;");
        
        visualizationPane.getChildren().addAll(placeholderLabel, dfaImageView);
        visualizationTab.setContent(visualizationPane);
        
        // Transition Table Tab
        Tab tableTab = new Tab("Transition Table");
        tableTab.setClosable(false);
        
        VBox tableContainer = new VBox(10);
        tableContainer.setPadding(new Insets(20));
        tableContainer.setStyle("-fx-background-color: white; -fx-background-radius: 8px; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 3); -fx-padding: 15px;");
        
        transitionTableArea = new TextArea();
        transitionTableArea.setEditable(false);
        transitionTableArea.setWrapText(true);
        transitionTableArea.setPrefRowCount(15);
        transitionTableArea.setStyle("-fx-font-family: 'Courier New', monospace; -fx-font-size: 14px; " +
                "-fx-background-color: #f0f0f0; -fx-background-radius: 5px; " +
                "-fx-border-radius: 5px; -fx-border-color: #e0e0e0; -fx-padding: 8px;");
        
        tableContainer.getChildren().add(transitionTableArea);
        tableTab.setContent(tableContainer);
        
        // Add tabs to tab pane
        tabPane.getTabs().addAll(visualizationTab, tableTab);
        
        return tabPane;
    }

    private HBox createStatusBar() {
        HBox statusBar = new HBox();
        statusBar.setPadding(new Insets(5, 10, 5, 10));
        statusBar.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #e0e0e0; -fx-border-width: 1 0 0 0;");
        
        statusLabel = new Label("Ready");
        statusBar.getChildren().add(statusLabel);
        
        return statusBar;
    }

    private void generateDFA() {
        String pattern = patternField.getText().trim();
        if (pattern.isEmpty()) {
            showError("Input Error", "Please enter a valid pattern.");
            updateStatus("Error: No pattern provided", true);
            return;
        }

        try {
            System.out.println("Generating DFA for pattern: " + pattern);
            updateStatus("Generating DFA for: " + pattern, false);

            // Generate DFA based on selected type
            if (patternTypeComboBox.getValue().equals("Ends With")) {
                currentDfa = LanguageParser.createEndsWith(pattern);
            } else {
                currentDfa = LanguageParser.createContains(pattern);
            }

            if (currentDfa == null) {
                showError("DFA Error", "DFA generation failed: No valid DFA was returned.");
                updateStatus("Error: DFA generation failed", true);
                return;
            }

            // Visualize DFA and save as an image
            DFAVisualizer.visualize(currentDfa, tempImagePath.toString());

            if (!Files.exists(tempImagePath)) {
                showError("Visualization Error", "Failed to generate DFA image.");
                updateStatus("Error: Failed to generate visualization", true);
                return;
            }

            // Display DFA image
            dfaImageView.setImage(new Image(tempImagePath.toUri().toString()));

            // Generate transition table and reorder it
            String table = DFAVisualizer.generateTransitionTable(currentDfa);
            transitionTableArea.setText(reorderTransitionTable(table));

            // Switch to visualization tab
            visualizationTabPane.getSelectionModel().select(0);
            
            updateStatus("DFA generated successfully", false);
            resultLabel.setText("");
        } catch (Exception e) {
            showError("Generation Error", "Failed to generate DFA: " + e.getMessage());
            updateStatus("Error: " + e.getMessage(), true);
            e.printStackTrace();
        }
    }

    private void testInput() {
        if (currentDfa == null) {
            showError("Test Error", "Please generate a DFA first.");
            updateStatus("Error: No DFA generated", true);
            return;
        }

        String input = testInputField.getText().trim();
        if (input.isEmpty()) {
            showError("Input Error", "Please enter a string to test.");
            updateStatus("Error: No test string provided", true);
            return;
        }

        try {
            boolean accepted = currentDfa.accepts(input);
            String result = "Input \"" + input + "\" is " + (accepted ? "ACCEPTED ✓" : "REJECTED ✗") + " by the DFA";
            resultLabel.setText(result);
            resultLabel.setTextFill(accepted ? Color.GREEN : Color.RED);
            resultLabel.setStyle(accepted ? "-fx-font-weight: bold; -fx-text-fill: #38b000;" : 
                                          "-fx-font-weight: bold; -fx-text-fill: #d90429;");
            
            updateStatus("Test completed: " + (accepted ? "String accepted" : "String rejected"), false);
        } catch (Exception e) {
            showError("Test Error", "Test failed: " + e.getMessage());
            updateStatus("Error during testing: " + e.getMessage(), true);
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
            if (!lines[i].trim().isEmpty()) {
                rows.add(lines[i]);
            }
        }

        // Sort rows based on state number (q0, q1, q2...)
        rows.sort(Comparator.comparingInt(row -> {
            String[] parts = row.split("\\s+", 2);
            String state = parts[0].trim();

            if (state.matches("q\\d+")) {
                return Integer.parseInt(state.substring(1));
            }
            return Integer.MAX_VALUE;
        }));

        StringBuilder reorderedTable = new StringBuilder();
        reorderedTable.append(header).append("\n");
        
        // Create a nicer separator
        String separator = "-".repeat(header.length()) + "\n";
        reorderedTable.append(separator);
        
        for (String row : rows) {
            reorderedTable.append(row).append("\n");
        }

        return reorderedTable.toString();
    }

    private void updateStatus(String message, boolean isError) {
        statusLabel.setText(message);
        statusLabel.setTextFill(isError ? Color.RED : Color.BLACK);
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