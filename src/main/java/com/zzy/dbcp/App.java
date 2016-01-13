package com.zzy.dbcp;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.zzy.db.DBServer;

/**
 * App.java 
 * @desc   oracle数据库连接池编写示例
 * @author zhengzy
 * @date   2016年1月13日下午4:27:27
 */
public class App {
	public static void main(String[] args) {
		DBServer dbServer = new DBServer("proxool.lytzats");
		try {
			ResultSet rs = dbServer.select("select 1 from dual");
			while (rs.next()) {
				System.out.println(rs.getString("1"));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
