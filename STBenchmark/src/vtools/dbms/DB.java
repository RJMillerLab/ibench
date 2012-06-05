package vtools.dbms;

import java.sql.Connection;  
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/* 
 * The role of this class is to provide a database connection to the database. The class is not 
 * actually needed that is why all the methods are static
 */
public class DB
{
    public static final int SQL_SERVER = 1;

    public static final int MYSQL = 2;

    public DB()
    {
        ;
    }

    /*
     * Connects to the database and returns a connection pointer.
     */
    public static Connection connect(int dbms, String host, int port, String dbName, String user, String passwd)
    {
        Connection dbConnection = null;
        try
        {
            String dbString = null;
            switch (dbms)
            {
            case DB.SQL_SERVER:
                DriverManager.registerDriver(new com.microsoft.jdbc.sqlserver.SQLServerDriver());
                dbString = "jdbc:microsoft:sqlserver://" + host + ":" + port + ";DatabaseName=" + dbName;
                break;
            case DB.MYSQL:
                Class.forName("com.mysql.jdbc.Driver").newInstance();
                dbString = "jdbc:mysql://" + host + "/" + dbName;
                break;
            default:
                throw new RuntimeException("Not supported DBMS");
            }
           
            dbConnection = DriverManager.getConnection(dbString, user, passwd);

            // if (dbConnection != null)
            // {
            // ;
            // System.out.println();
            // System.out.println("Successfully connected");
            // System.out.println();
            // Meta data
            // DatabaseMetaData meta = _connection.getMetaData();
            // System.out.println("\nDriver Information");
            // System.out.println("Driver Name: " + meta.getDriverName());
            // System.out.println("Driver Version: " +
            // meta.getDriverVersion());
            // System.out.println("\nDatabase Information ");
            // System.out.println("Database Name: " +
            // meta.getDatabaseProductName());
            // System.out.println("Database Version: " +
            // meta.getDatabaseProductVersion());
            // }
        }
        catch (Exception e)
        {
            System.err.println("Failed to connect with the DB");
            e.printStackTrace();
        }

        return dbConnection;
    }

    public static void executeStatementUnconditionally(String query, Connection con)
    {
        executeStatement(query, true, con);
    }

    public static void executeStatement(String query, Connection con)
    {
        executeStatement(query, false, con);
    }

    private static void executeStatement(String q, boolean unconditionally, Connection con)
    {
        try
        {
            Statement stmt = con.createStatement();
            stmt.execute(q);
            stmt.close();
        }
        catch (SQLException e)
        {
            if (unconditionally)
                return;
            System.out.println("Statement: \n" + q);
            e.printStackTrace();
            throw new RuntimeException("Query " + q);
        }
    }

    public static void executeQueryInToTable(String query, String table, Connection con)
    {
        executeStatementUnconditionally("drop table " + table, con);
        query = query.replaceFirst("from", "into " + table + " from");
        // executeStatement("select * into " + table + " from (" + query + ") AS
        // Q", con);
        executeStatement(query, con);
    }

    public static ResultSet executeQuery(String query, Connection con)
    {
        try
        {
            Statement stmt = con.createStatement();
            ResultSet results = stmt.executeQuery(query);
            return results;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    // public static void executeStatementUnconditionally(String query,
    // Statement con)
    // {
    // executeStatement(query, true, con);
    // }
    //
    // public static void executeStatement(String query, Statement con)
    // {
    // executeStatement(query, false, con);
    // }
    //
    // private static void executeStatement(String q, boolean unconditionally,
    // Statement con)
    // {
    // try
    // {
    // con.execute(q);
    // }
    // catch (SQLException e)
    // {
    // if (unconditionally)
    // return;
    // System.out.println("Statement: \n" + q);
    // e.printStackTrace();
    // throw new RuntimeException();
    // }
    // }

    /*
     * Deletes all the tables of the database and all the views
     */
    public static void wipeOutDBContents(String dbName, Connection con)
    {
        // first drop all the views
        String vdropCom = "select TABLE_NAME from INFORMATION_SCHEMA.views where TABLE_SCHEMA='" + dbName + "'";
        try
        {
            Statement stmt = con.createStatement();
            ResultSet results = stmt.executeQuery(vdropCom);
            while (results.next())
            {
                String tableName = results.getString(1);
                DB.executeStatement("drop view " + tableName, con);
            }
            results.close();
            stmt.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new RuntimeException();
        }

        // then read all the tables
        String command = "SELECT table_name FROM INFORMATION_SCHEMA.TABLES  where TABLE_TYPE='BASE TABLE' and TABLE_SCHEMA='"
                + dbName + "'";
        try
        {
            Statement stmt = con.createStatement();
            ResultSet results = stmt.executeQuery(command);
            while (results.next())
            {
                String tableName = results.getString(1);
                DB.executeStatement("drop table " + tableName, con);
            }
            results.close();
            stmt.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

}
