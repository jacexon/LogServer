package Controllers;

import Net.LogServer;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

/**
 * Klasa kontrolera wszystkich komponentów znajdujących się na pierwszym pulpicie serwera
 */
public class StartingServerController {
    //region components
    @FXML
    private TextField port_textField;

    @FXML
    private TextField ip_textField;

    @FXML
    private Button stop_button;

    @FXML private Button logViewerButton;

    @FXML
    private Label running_label;

    @FXML
    private CheckBox localhost_checkbox;
//endregion

    /**
     * Obsługa przycisku uruchomienia serwera
     * @param event
     * @throws IOException
     */
    public void handleLaunchButton(ActionEvent event) throws IOException {
        if ((!(port_textField.getText().isEmpty())) && (!(ip_textField.getText().isEmpty()))) {
            if (Integer.parseInt(port_textField.getText()) > 1024) {
                LogServer server = new LogServer(ip_textField.getText(), Integer.parseInt(port_textField.getText()));
                running_label.setText("Server is running...");
            } else {
                running_label.setText("Invalid port! Try again.");
                ip_textField.clear();
                port_textField.clear();
            }
        } else {
            running_label.setText("Invalid port! Try again.");
            ip_textField.clear();
            port_textField.clear();

        }
    }

    /**
     * Obsługa przycisku zatrzymania serwera
     * @param event
     */
    public void handleStopButton(ActionEvent event){
        System.exit(0);
    }

    /**
     * Obsługa CheckBoxa
     * @param event
     */
    public void handleLocalhostCheckbox(ActionEvent event){
        if (localhost_checkbox.isSelected()){
            ip_textField.setText("127.0.0.1");
            port_textField.setText("1099");
            ip_textField.setEditable(false);
            port_textField.setEditable(false);
        }

        else {
            ip_textField.setEditable(true);
            port_textField.setEditable(true);
        }
    }

    /**
     * Obsługa przycisku uruchamiającego Log Viewera
     * @param event
     */
    public void handleViewerButton(ActionEvent event){
        try {
            Parent newTypeScreen = FXMLLoader.load(getClass().getResource("../Model/logViewer.fxml"));
            Scene newTypeScene = new Scene(newTypeScreen,600,450);
            Stage newTypeStage = new Stage();
            newTypeStage.setTitle("Log Viewer");
            newTypeStage.setScene(newTypeScene);
            newTypeStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
