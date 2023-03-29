package com.example.cartphone;

import android.annotation.SuppressLint;
import android.os.StrictMode;
import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;

public class ConnectionHelper {

    Connection con;
    String ip = "192.168.254.108"; // home ip
//    String ip = "192.168.1.3";
//    String ip = "172.31.196.182";

    final String port = "1433";
    final String Classes = "net.sourceforge.jtds.jdbc.Driver";
    final String database = "MaTapat";
    final String username = "test"; // could be changed at the final.
    final String password = "test";

    @SuppressLint("NewApi")
    public Connection connectionClass(){


        String records = "",error="";

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        Connection connection = null;

        try{
            Class.forName(Classes);
            String url =  "jdbc:jtds:sqlserver://"+ ip + ":"+ port+";"+ "databasename="+ database+";user="+username+";password="+password+";";
            connection = DriverManager.getConnection(url);


        }catch (Exception ex){
            Log.e("Error", ex.getMessage());
        }
        return connection;
    }

}
