package com.summonelec.inspectiongenie.DB

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

class DatabaseConnection {
//        private  val JDBC_URL = "jdbc:mysql://inspection-genie-db-do-user-14512858-0.b.db.ondigitalocean.com:25060/inspectiongeniedb"
//        private  val USERNAME = "doadmin"
//        private  val PASSWORD = "AVNS_-C3wVjylNuQ3gaTKEFL"
    companion object {
        private const val JDBC_URL = "jdbc:mysql://inspection-genie-db-do-user-14512858-0.b.db.ondigitalocean.com:25060/inspectiongeniedbNew"
        private const val USERNAME = "doadmin"
        private const val PASSWORD = "AVNS_-C3wVjylNuQ3gaTKEFL"

        suspend fun connectToDatabase(): Connection? = withContext(Dispatchers.IO) {
            try {
                Class.forName("com.mysql.jdbc.Driver")
                return@withContext DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)
            } catch (e: ClassNotFoundException) {
                e.printStackTrace()
                return@withContext null
            } catch (e: SQLException) {
                e.printStackTrace()
                return@withContext null
            }
        }
    }
}

/*    fun connect(): Connection? {
        var connection: Connection? = null
        val connectionProps = Properties()
        connectionProps["user"] = USERNAME
        connectionProps["password"] = PASSWORD
        try {
            GlobalScope.launch(Dispatchers.IO) {
                Class.forName("com.mysql.jdbc.Driver")
                connection = DriverManager.getConnection(JDBC_URL, connectionProps)
            }

        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        } catch (e: SQLException) {
            e.printStackTrace()
        }
        return connection
    }

    fun isConnected(connection: Connection?): Boolean {
        return try {
            connection?.isValid(5) == true
        } catch (e: SQLException) {
            false
        }
    }
}*/
