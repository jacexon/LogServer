package Controllers;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.*;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Window;
import javafx.util.Callback;

import javax.swing.*;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ResourceBundle;

/**
 * @author Jacek Polak
 * @since 10.10.16r
 * @version 2
 */

public class logViewerController implements Initializable {

    //region components
    @FXML MenuButton menu_type;
    @FXML Button delete_button;
    @FXML Button show_button;
    @FXML TextField filter_textfield;
    @FXML TableView<ObservableList> tableview;
    private ObservableList<ObservableList> data;
    //endregion

    /**
     * Metoda inicjalizująca Log Viewer
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        getTypesFromDB();
        filter_textfield.textProperty().addListener(e -> {
                if(filter_textfield.textProperty().get().isEmpty()) {
                    tableview.setItems(data);
                    return;
                }
                ObservableList<ObservableList> tableItems = FXCollections.observableArrayList();
                ObservableList<TableColumn<ObservableList, ?>> cols = tableview.getColumns();
                for(int i=0; i<data.size(); i++) {
                    for(int j=0; j<cols.size(); j++) {
                        TableColumn col = cols.get(j);
                        String cellValue = col.getCellData(data.get(i)).toString();
                        cellValue = cellValue.toLowerCase();
                        if(cellValue.contains(filter_textfield.textProperty().get().toLowerCase())) {
                            tableItems.add(data.get(i));
                            break;
                        }
                    }
                tableview.setItems(tableItems);
            }
        });
    }

    /**
     * Metoda wpisująca dane do Log Viewera
     * @param table Nazwa tabeli w bazie danych, z której będą wpisywane dane do lokalnej tabeli
     */
    public void insertData(String table){
        try {
            tableview.getColumns().clear();
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/logdatabase","Jacek","password");
            Statement stat = conn.createStatement();
            ResultSet rs = stat.executeQuery("SELECT * FROM " + table);
            data = FXCollections.observableArrayList();

            for (int i = 0; i < rs.getMetaData().getColumnCount(); i++) {
                final int j = i;
                TableColumn col = new TableColumn(rs.getMetaData().getColumnName(i + 1));
                col.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ObservableList, String>, ObservableValue<String>>() {
                    public ObservableValue<String> call(TableColumn.CellDataFeatures<ObservableList, String> param) {
                        return new SimpleStringProperty(param.getValue().get(j).toString());
                    }
                });
                tableview.getColumns().addAll(col);
            }

            while(rs.next()){
                ObservableList<String> row = FXCollections.observableArrayList();
                for(int i=1 ; i<=rs.getMetaData().getColumnCount(); i++){
                    row.add(rs.getString(i));
                }
                data.add(row);
            }
            tableview.setItems(data);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    /**
     * Metoda kasująca wpis z danej tabeli w bazie danych
     * @param table Nazwa tabeli, z której chcemy usunąć wpis
     * @param id Id wiersza, za pomocą którego zostanie wykonane zapytanie usunięcia z bazy danych
     */
    public void deleteRowFromDB(String table, String id){
        try {
            Connection myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/logdatabase","Jacek","password");
            Statement stat = myConn.createStatement();
            stat.executeUpdate("DELETE FROM " + table + " WHERE id=" + "'" + id+ "'");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Metoda obsługująca przycisk usuwania
     * @param event
     */
    public void handleDeleteButton(ActionEvent event) {
        if(!data.isEmpty()) {
            Alert deleteAlert = new Alert(Alert.AlertType.WARNING, "Confirm", ButtonType.OK, ButtonType.CANCEL);
            Window owner = ((Node) event.getTarget()).getScene().getWindow();
            deleteAlert.setContentText("Are you sure?");
            deleteAlert.initModality(Modality.APPLICATION_MODAL);
            deleteAlert.initOwner(owner);
            deleteAlert.showAndWait();
            if(deleteAlert.getResult() == ButtonType.OK) {
                TablePosition pos = tableview.getSelectionModel().getSelectedCells().get(0);
                TableColumn col = pos.getTableColumn();
                int row = pos.getRow();
                ObservableList item = tableview.getItems().get(row);
                deleteRowFromDB(menu_type.getText(),(String) col.getCellObservableValue(item).getValue());
                data.removeAll(tableview.getSelectionModel().getSelectedItems());
                tableview.getSelectionModel().clearSelection();
                setAI();

            }
            else {
                deleteAlert.close();
            }
        }
    }

    /**
     * Metoda dodająca nowy przycisk do listy wyboru typów logów
     * @param table Nazwa typu
     */
    public void addItem(String table){
        MenuItem item = new MenuItem(table);
        item.setOnAction(e->{
            menu_type.setText(table);
            insertData(table);
        });
        menu_type.getItems().add(item);
    }


    /**
     * Metoda wpisująca wszystkie typy logów znajdujące się w bazie danych do menu wyboru
     */
    public void getTypesFromDB(){
        try {
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/logdatabase","Jacek","password");
            Statement stat = conn.createStatement();
            ResultSet rs = stat.executeQuery("SELECT table_name FROM information_schema.tables WHERE table_schema='logdatabase'");
             while(rs.next()){
                addItem(rs.getString(1));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Metoda resetująca licznik Primary Key, gdy tabela stanie się pusta
     */
    public void setAI(){
        try {
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/logdatabase","Jacek","password");
            Statement stat = conn.createStatement();
            ResultSet rs = stat.executeQuery("SELECT COUNT(*) FROM logdatabase." + menu_type.getText());
            rs.next();
            if (rs.getInt(1) == 0){
                stat.executeUpdate("ALTER TABLE logdatabase." + menu_type.getText() + " AUTO_INCREMENT = 1");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }





}
