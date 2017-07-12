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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/*
 * The role of the id manager is to keep a truck of ids and always provide you with a new 
 * id. For the moment the way it works is that it has a table in the db that keeps truck 
 * of the most recent assigned id and gives you the next available when asked. 
 */
public class IDManager
{
    public IDManager(Connection connection)
    {
        // Have a table:
        // __IDMngr_Ids [id, name]
        // that keeps the next available id a string named `name`
        DB.executeStatement("if not exists "
                + "(SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME ='__IDMngr_Ids') \n BEGIN\n"
                + "create table __IDMngr_Ids (id INT not null, name VARCHAR(50) not null PRIMARY KEY)\n"
                + "create index __idsIndx on __IDMngr_Ids(name) \n" + "END", connection);
    }

    /* 
     * Gives you the next available id for the name string you are asking for
     * The ids start counting from 1 (Not from 0)
     */
    public int getNewId(String name, Connection connection)
    {
        int newId = 0;
        // retrieve from the Ids table the number of the tuple with that number.
        String queryStr = "select id from __IDMngr_Ids where name ='" + name + "'";
        ResultSet result = DB.executeQuery(queryStr, connection);
        
        try
        {
            if (result.next())
            {
                // if there is already such a number
                newId = result.getInt(1);
                String command = "update __IDMngr_Ids set id = id + 1 where name='" + name + "'";
                DB.executeStatement(command, connection);
            }
            else
            {
                // if not return value 1 and say that the next time that someone
                // will ask
                // for that value, to give him the value 2
                newId = 1;
                String command = "insert into __IDMngr_Ids values (2,'" + name + "')";
                DB.executeStatement(command, connection);
            }
            Statement stmt = result.getStatement();
            result.close();
            stmt.close();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            throw new RuntimeException("Should not happen");
        }
        return newId;
    }

    public void reset(Connection connection)
    {
        String command = "delete from __IDMngr_Ids";
        DB.executeStatement(command, connection);
        
    }

    
    
    
}
