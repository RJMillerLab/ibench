/*
 *
 * Copyright 2016 Big Data Curation Lab, University of Toronto,
 * 		   	  	  	   				 Patricia Arocena,
 *   								 Boris Glavic,
 *  								 Renee J. Miller
 *
 * This software also contains code derived from STBenchmark as described in
 * with the permission of the authors:
 *
 * Bogdan Alexe, Wang-Chiew Tan, Yannis Velegrakis
 *
 * This code was originally described in:
 *
 * STBenchmark: Towards a Benchmark for Mapping Systems
 * Alexe, Bogdan and Tan, Wang-Chiew and Velegrakis, Yannis
 * PVLDB: Proceedings of the VLDB Endowment archive
 * 2008, vol. 1, no. 1, pp. 230-244
 *
 * The copyright of the ToxGene (included as a jar file: toxgene.jar) belongs to
 * Denilson Barbosa. The iBench distribution contains this jar file with the
 * permission of the author of ToxGene
 * (http://www.cs.toronto.edu/tox/toxgene/index.html)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package vtools.dbms;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;

/* 
 * The role of this class is to provide a database connection to the database. The class is not 
 * actually needed that is why all the methods are static
 */
public class DB
{
	static Logger log = Logger.getLogger(DB.class);
	
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
//                DriverManager.registerDriver(new com.microsoft.jdbc.sqlserver.SQLServerDriver());
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
            // if (log.isDebugEnabled()) {log.debug();};
            // if (log.isDebugEnabled()) {log.debug("Successfully connected");};
            // if (log.isDebugEnabled()) {log.debug();};
            // Meta data
            // DatabaseMetaData meta = _connection.getMetaData();
            // if (log.isDebugEnabled()) {log.debug("\nDriver Information");};
            // if (log.isDebugEnabled()) {log.debug("Driver Name: " + meta.getDriverName());};
            // if (log.isDebugEnabled()) {log.debug("Driver Version: " +
            // meta.getDriverVersion());};
            // if (log.isDebugEnabled()) {log.debug("\nDatabase Information ");};
            // if (log.isDebugEnabled()) {log.debug("Database Name: " +
            // meta.getDatabaseProductName());};
            // if (log.isDebugEnabled()) {log.debug("Database Version: " +
            // meta.getDatabaseProductVersion());};
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
            if (log.isDebugEnabled()) {log.debug("Statement: \n" + q);};
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
    // if (log.isDebugEnabled()) {log.debug("Statement: \n" + q);};
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
