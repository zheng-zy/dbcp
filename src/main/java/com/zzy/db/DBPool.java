package com.zzy.db;

import org.apache.log4j.Logger;

import com.zzy.utils.ClassUtil;

/**
 * 
 * DBPool.java 
 * @desc   数据库连接池
 * @author zhengzy
 * @date   2016年1月13日下午4:31:39
 */
public class DBPool {
	/**
	 * Logger for this class
	 */
	private static final Logger logger = Logger.getLogger(DBPool.class);

	private String poolPath;// 数据库连接池的配置文件路径

	private DBPool() {
	}

	/**
	 * @return
	 * @Description: 返回DBPool对象
	 */
	public static DBPool getDBPool() {
		return DBPoolDao.dbPool;
	}

	/**
	 * @Description: 静态内部类实现单例模式
	 * @Version:1.1.0
	 */
	private static class DBPoolDao {
		/**
		 * Logger for this class
		 */
		private static final Logger logger = Logger.getLogger(DBPoolDao.class);

		private static DBPool dbPool = new DBPool();
	}

	public String getPoolPath() {
		if (poolPath == null) {
			poolPath = ClassUtil.getClassRootPath(DBPool.class) + "proxool.xml";
			System.out.println("数据库文件路径>>" + poolPath);
		}
		return poolPath;
	}

	/**
	 * @param poolPath
	 * @Description: 设置数据库连接池的配置文件路径
	 */
	public void setPoolPath(String poolPath) {
		this.poolPath = poolPath;
	}

}
