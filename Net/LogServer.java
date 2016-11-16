package Net;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.sql.*;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Jacek Polak
 * @since 10.10.16r
 * @version 2.0
 */
public class LogServer extends UnicastRemoteObject implements LogInterface {

    /**
     * Konstruktor klasy Serwera(Agenta)
     * @param ip Adres sieciowy serwera
     * @param port Port, na którym serwer uruchamia nasłuch
     * @throws RemoteException
     */
    public LogServer(String ip, int port) throws RemoteException {
        super(port);

        try{
            LocateRegistry.createRegistry(port);
            Naming.rebind("rmi://" + ip + ":"+ port +"/LogServer", this);
            System.err.println("Server is created on port: " + port);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Metoda dodająca nowy wpis w bazie danych.
     * @param table Nazwa typu (tabeli), do której zostanie dodany wpis
     * @param data Nazwy kolumn danej tabeli
     */
    public void insertIntoDB(String table, String[] data){
        try{
            String datam = reformatQuery(data);
            java.util.Date dt = new java.util.Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String currentTime = sdf.format(dt);
            String ip = InetAddress.getLocalHost().getHostAddress();
            Connection myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/logdatabase","Jacek","password");
            String query = "INSERT INTO LogDatabase." + table + " VALUES(NULL, " + "'"+ip+"'" +", " + "'"+currentTime+"'" + ", " +
                    datam + ")";
            Statement newStat = myConn.createStatement();
            newStat.executeUpdate(query);
        }
        catch (Exception e){
            System.out.println("Nie udało się zapisać!");
            System.err.println(e.getMessage());
        }

    }

    /**
     * Metoda tworząca nowy typ logu w bazie danych
     * @param name Nazwa typu logu
     * @param data Nazwy kolumn
     */
    public void createNewTypeDB(String name, String... data){
        try{
            Connection myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/logdatabase","Jacek","password");
            String query = "CREATE TABLE logdatabase." + name + "(id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, ip VARCHAR(50)," +
                    " addingDate DATETIME)";
            Statement newStat = myConn.createStatement();
            newStat.executeUpdate(query);
            for(int i = 0; i<data.length;i++){
                newStat.executeUpdate("ALTER TABLE "+ name + " ADD " + data[i] + " VARCHAR(60)");
            }
        }
        catch (Exception e){
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
    }

    /**
     * Funkcja przekształcająca tablicę Stringów do formatu potrzebnego do wysłania zapytania do bazy danych
     * @param data Tablica stringów
     * @return Przekształcona tablica
     */
    public static String reformatQuery(String[] data){
        StringBuilder builder = new StringBuilder();
        for(String s : data) {
            builder.append("'");
            builder.append(s);
            builder.append("'");
            builder.append(",");
        }
        String datam = builder.substring(0,builder.length()-1);
        return datam;
    }


}
