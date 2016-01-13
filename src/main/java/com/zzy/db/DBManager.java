package com.zzy.db;  

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.logicalcobwebs.proxool.configuration.JAXPConfigurator;

/**
 * DBManager.java 
 * @desc   数据库连接管理类
 * @author zhengzy
 * @date   2016年1月13日下午4:29:07
 */
public class DBManager {
	
	private DBManager(){
		try {
			//数据库连接池配置文件
			JAXPConfigurator.configure(DBPool.getDBPool().getPoolPath(), false);
			//数据库加载驱动类
			Class.forName("org.logicalcobwebs.proxool.ProxoolDriver");
		} catch (Exception e) {
			// TODO Auto-generated catch block  
			e.printStackTrace();
		}
	}
	
	/**
	 * @param poolName
	 * @return
	 * @throws SQLException
	 * @Description: 获取数据库连接
	 */
	public Connection getConnection(String poolName) throws SQLException {
		return DriverManager.getConnection(poolName);
	}
	
	/**
	 *@Description: 内部静态类实现单例模式 
	 *@Version:1.1.0
	 */
	private static class DBManagerDao {
		private static DBManager dbManager = new DBManager();
	}
	
	/**
	 * @return
	 * @Description: 返回数据库连接池管理类
	 */
	public static DBManager getDBManager() {
		return DBManagerDao.dbManager;
	}

}
