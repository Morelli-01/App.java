package MainProject.database;

import MainProject.Utils.DBManager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Map;

public class dbConn {
        Map Credentials;

    public Map getCredentials() {
        return Credentials;
    }

    public dbConn() {
            try {
                DBManager.setConnection(DBManager.JDBC_Driver_SQLite, DBManager.JDBC_URL_SQLite);
                Statement statement = DBManager.getConnection().createStatement();
                try {
                    statement.executeQuery("SELECT * FROM Credentials LIMIT 1");
                } catch (
                        SQLException e) {
                    statement.executeUpdate("DROP TABLE IF EXISTS Credentials");
                    statement.executeUpdate("CREATE TABLE Credentials (Username VARCHAR(50) not null," +
                            "Psw varchar(50) not null, " +
                            "primary key (Username,Psw))");
                    statement.executeUpdate("INSERT INTO Credentials VALUES ('Utente1', 'Utente1')");
                    statement.executeUpdate("INSERT INTO Credentials VALUES ('Utente2', 'Utente2')");
                    statement.executeUpdate("INSERT INTO Credentials VALUES ('Utente3', 'Utente3')");
                    statement.executeUpdate("INSERT INTO Credentials VALUES ('DebugUser', 'debug')");
                }
                ResultSet rs = statement.executeQuery("select * from Credentials");
                int i = 1;
                int userindex = rs.findColumn("Username");
                int pswindex = rs.findColumn("Psw");
                Map<String, String> map = new LinkedHashMap<String, String>();
                while (rs.next()) {
                    String usr = rs.getString(userindex);
                    String psw = rs.getString(pswindex);
                    map.put(usr, psw);
                }
                Credentials=map;
            }catch(SQLException e){
                System.out.println(e.getMessage());
            }
        }
}
