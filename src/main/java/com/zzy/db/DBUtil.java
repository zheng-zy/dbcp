package com.zzy.db;

import java.lang.reflect.Method;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import oracle.jdbc.internal.OracleResultSet;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

/**
 * DBUtil.java 
 * @desc   数据库操作类
 * @author zhengzy
 * @date   2016年1月13日下午4:33:44
 */
public class DBUtil {
	/**
	 * Logger for this class
	 */
	private static final Logger logger = Logger.getLogger(DBUtil.class);
	//数据库连接池的别名
	private static final String POOLNAME = "proxool.lytzats";
	
	private static DBUtil me;
	private DBManager dm;
	
	public static DBUtil getInstance() {
		if (me == null) {
			me = new DBUtil();
		}
		return me;
	}
	
	public Connection getConnection() {
		Connection dbConn = null;
		try {
			if (dm == null)
				dm = DBManager.getDBManager();
			dbConn = dm.getConnection(POOLNAME);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}

		return dbConn;
	}

	/**
	 * 查询
	 * 
	 * @param sql
	 * @return
	 * @throws SQLException
	 */
	public List<HashMap<String, String>> executeQuery(String sql)
			throws SQLException {
		List<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
		ResultSet rs = null;
		Statement st = null;
		Connection conn = null;
		logger.info("executeQuery:" + sql);
		try {
			conn = this.getConnection();
			st = conn.createStatement();
			rs = st.executeQuery(sql);
			ResultSetMetaData rsmd = rs.getMetaData();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			while (rs.next()) {
				HashMap<String, String> colMap = new HashMap<String, String>();
				for (int i = 1; i <= rsmd.getColumnCount(); i++) {
					String columName = rsmd.getColumnName(i);
					int columnType = rsmd.getColumnType(i);
					String value = convOracleStr(rs, columName, columnType, sdf);
					
//					String value = rs.getString(columName);
					
					
					// columName = columName.toUpperCase();
					colMap.put(columName.toLowerCase(), value);
				}
				list.add(colMap);
			}
		} catch (SQLException ex) {
			// logger.debug(ex.getMessage());
			logger.error("executeQuery", ex);
			// ex.printStackTrace();
			throw ex;
		} finally {
			freeDBResource(rs, st, conn);
		}
		return list;
	}

	/**
	 * 更新
	 * 
	 * @param sql
	 * @return
	 * @throws SQLException
	 */
	public boolean executeUpdate(String sql) throws SQLException {
		boolean flag = false;
		Statement st = null;
		Connection conn = null;
		logger.info("executeUpdate:" + sql);
		try {
			conn = this.getConnection();
			st = conn.createStatement();
			st.executeUpdate(sql);
			flag = true;
		} catch (SQLException ex) {
			flag = false;
			// logger.debug(ex.getMessage());
			// logger.debug("错误SQL:"+sql);
			// ex.printStackTrace();
			logger.error("executeUpdate", ex);
			throw ex;
		} finally {
			freeDBResource(null, st, conn);
		}
		return flag;
	}

	// 从sql中得到查询的唯一数据
	public String getValueFromSql(String sql) throws Exception {
		logger.info("getValueFromSql:" + sql);
		String result = "";
		Connection dbConn = getConnection();
		Statement dbStatement = null;
		ResultSet rs = null;
		try {
			dbStatement = dbConn.createStatement();
			rs = dbStatement.executeQuery(sql);
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			ResultSetMetaData rsmd = rs.getMetaData();
			String columName = rsmd.getColumnName(1);
			int columnType = rsmd.getColumnType(1);
			if (rs.next()) {
//				result = rs.getString(1);
				result = convOracleStr(rs, columName, columnType, sdf);
			}
			return result;
		} catch (Exception ex) {
			logger.error("getValueFromSql", ex);
			throw ex;
		} finally {
			freeDBResource(rs, dbStatement, dbConn);
		}

	}

	// 执行sql,得到列表
	public List<String> getValueList(String sql) throws Exception {
		List<String> lists = new ArrayList<String>();
		logger.info("getValueList:" + sql);
		Connection dbConn = getConnection();
		Statement dbStatement = null;
		ResultSet rs = null;
		try {
			dbStatement = dbConn.createStatement();
			rs = dbStatement.executeQuery(sql);
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			ResultSetMetaData rsmd = rs.getMetaData();
			String columName = rsmd.getColumnName(1);
			int columnType = rsmd.getColumnType(1);
			while (rs.next()) {
//				lists.add(rs.getString(1));
				String result = convOracleStr(rs, columName, columnType, sdf);
				lists.add(result);
			}
			return lists;
		} catch (Exception ex) {
			logger.error("getValueList", ex);
			throw ex;
		} finally {
			freeDBResource(rs, dbStatement, dbConn);
		}

	}

	// 执行sql,得到列表
	public List<String> getRowValueList(String sql) throws Exception {
		List<String> lists = new ArrayList<String>();
		logger.info("getRowValueList:" + sql);
		Connection dbConn = getConnection();
		Statement dbStatement = null;
		ResultSet rs = null;
		try {
			dbStatement = dbConn.createStatement();
			rs = dbStatement.executeQuery(sql);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			ResultSetMetaData rsmd = rs.getMetaData();
			
			int rscount = rs.getMetaData().getColumnCount();
			if (rs.next()) {
				for (int i = 1; i <= rscount; i++){
					String columName = rsmd.getColumnName(i);
					int columnType = rsmd.getColumnType(i);
					String result = convOracleStr(rs, columName, columnType, sdf);
					lists.add(result);
//					lists.add(rs.getString(i));
				}
			}
			return lists;
		} catch (Exception ex) {
			logger.error("getRowValueList", ex);
			throw ex;
		} finally {
			freeDBResource(rs, dbStatement, dbConn);
		}

	}

	// //////////////////////////类型和sql的转换

	// //更新存储过程
	public void callProcess(String sql) throws Exception {
		logger.info("callProcess sql = " + sql);
		Connection dbConn = getConnection();
		CallableStatement dbStatement = null;
		try {
			dbStatement = dbConn.prepareCall(sql);
			dbStatement.executeQuery();
		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		} finally {
			freeDBResource(null, dbStatement, dbConn);
		}

	}

	/**
	 * 从数据库中提取CLOB类型字段的内容并转换为字符串
	 * 
	 * @param rs
	 *            数据库ResultSet,含有CLOB类型的字段
	 * @param clobidx
	 *            含有CLOB类型字段在ResultSet中的索引
	 * @return 取出的字符内容
	 * @throws SQLException
	 */
	private String getCLOBContent(ResultSet rs, String clobname)
			throws SQLException {
		if(!(rs instanceof OracleResultSet)){
			return rs.getString(clobname);
		}
		
		oracle.sql.CLOB clobField = ((OracleResultSet) rs).getCLOB(clobname);
		if (clobField == null)
			return "";

		long clen = clobField.length();
		char clobArray[] = new char[(int) clen];
		clobField.getChars(1, (int) clen, clobArray);
		StringBuffer sb = new StringBuffer();
		sb.append(clobArray);
		return sb.toString();
	}

	// page 从1开始
	public List<List<String>> performSearchSql(String sql, int linesum, int page)
			throws Exception {
		// 起始和结束条目
		int start = linesum * (page - 1);
		int end = linesum * page;

		String datasql = "select * from  (select bbbbbb.*,rownum mynum from  (select * from  ( "
				+ sql
				+ " ) where rownum <="
				+ end
				+ ") bbbbbb) where mynum>"
				+ start;

		return performSearchSql(datasql);

	}

	// 执行查询sql 二维链表
	public List<List<String>> performSearchSql(String sql) throws Exception {
		return genSearchData(sql);
	}

	// 实际生成数据
	private List<List<String>> genSearchData(String sql) throws Exception {
		logger.info("performSearcheSql sql = " + sql);

		List<List<String>> list = new ArrayList<List<String>>();
		Connection dbConn = getConnection();
		Statement dbStatement = null;
		ResultSet rs = null;
		try {
			dbStatement = dbConn.createStatement();
			rs = dbStatement.executeQuery(sql);
			// 表头信息
			ResultSetMetaData rsMetaData = rs.getMetaData();
			List<String> titleList = new ArrayList<String>();
			for (int i = 1; i <= rsMetaData.getColumnCount(); i++) {
				String attributeName = rsMetaData.getColumnName(i)
						.toUpperCase();
				// 把表头都变成大写，，为将来比较做准备
				titleList.add(attributeName);
			}
			list.add(titleList);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			// 具体数据
			while (rs.next()) {
				List<String> valuelist = new ArrayList<String>();
				for (int i = 1; i <= rsMetaData.getColumnCount(); i++) {

					String coltype = rsMetaData.getColumnTypeName(i);
					String attributeName = rsMetaData.getColumnName(i);
					int columnType = rsMetaData.getColumnType(i);
//					if (coltype.equals("CLOB")) {
//						valuelist.add(getCLOBContent(rs, attributeName));
//					} else {
//						String tmp = rs.getString(i);
//						if (tmp == null)
//							tmp = "";
//						valuelist.add(tmp);
//					}
					String result = convOracleStr(rs, attributeName, columnType, sdf);
					valuelist.add(result);
				}
				list.add(valuelist);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		} finally {
			freeDBResource(null, dbStatement, dbConn);
		}
		return list;
	}
	
	
	// 解决获取数据时，小数类型 会把小数点前的0 去掉
		private String convOracleStr(ResultSet rs, String columName, int columnType,
				SimpleDateFormat sdf) throws SQLException {
			if (columnType == Types.FLOAT) {
				Float f = rs.getFloat(columName);
				if (f == null) {
					return null;
				} else {
					return f.toString();
				}
			} else if (columnType == Types.DOUBLE) {
				Double d = rs.getDouble(columName);
				if (d == null) {
					return null;
				} else {
					return d.toString();
				}
			} else if (columnType == Types.NUMERIC) {
				String str = rs.getString(columName);
				if (str == null) {
					return null;
				} else {
					if (str.startsWith(".")) {
						return "0" + str;
					} else if (str.startsWith("-.")) {
						return "-0." + str.substring(2);
					} else {
						return str;
					}
				}
			} else if (columnType == Types.DATE || columnType == Types.TIMESTAMP) {
				Timestamp timestamp = rs.getTimestamp(columName);
				if (timestamp != null) {
					return sdf.format(timestamp);
				} else {
					return null;
				}
			} else if (columnType==Types.CLOB) {
				return getCLOBContent(rs, columName);
			}else {
				return rs.getString(columName);
			}

		}
	
	
	
	////////////////////

	public List findAllByWhere(Class cls, String sql) throws Exception {
		List<List<String>> list = performSearchSql(sql);
		List resultList = convertListValue(cls, list);
		return resultList;
	}

	public List findAllByWhere(Class cls, String sql, int count, int page)
			throws Exception {
		List<List<String>> list = performSearchSql(sql, count, page);
		List resultList = convertListValue(cls, list);
		return resultList;
	}

	/**
	 * 转换 list 对象
	 * 
	 * @param cls
	 * @param list
	 * @return
	 * @throws Exception
	 */
	public List convertListValue(Class cls, List<List<String>> list)
			throws Exception {
		List resultList = new ArrayList();
		if (list == null || list.size() < 2) {
			return resultList;
		}

		List<String> titleList = list.get(0);
		for (int i = 1; i < list.size(); i++) {
			List<String> valueList = list.get(i);
			Object obj = setObjValue(cls, titleList, valueList);
			resultList.add(obj);
		}

		return resultList;

	}

	private Object setObjValue(Class cls, List<String> titleList,
			List<String> valueList) throws Exception {

		Object obj = cls.newInstance();

		for (int i = 0; i < titleList.size(); i++) {
			String attributeName = titleList.get(i);
			String value = valueList.get(i);
			setCutValue(obj, attributeName, value);
		}

		return obj;
	}

	// 类中所有方法的清单，通过该做法可以避免要求 字段的大小写一致
	Map<Class, List<String>> clsmnamesMap = new HashMap<Class, List<String>>();

	// 取得类 属性方法的实际名称
	private String getMethodName(Class cls, String mname) {
		List<String> list = clsmnamesMap.get(cls);
		if (list == null) {
			list = new ArrayList<String>();
			clsmnamesMap.put(cls, list);
			// 初始化
			Method[] ms = cls.getMethods();
			for (Method m : ms) {
				String name = m.getName();
				list.add(name);
			}

		}

		// 找到方法
		for (String str : list) {
			if (str.toUpperCase().equals(mname.toUpperCase())) {
				return str;
			}
		}
		return null;
	}

	// 调用方法设置值
	private void setCutValue(Object obj, String attributeName, String content)
			throws Exception {
		Object value = null;

		// 找寻对于的get方法， set 方法，如果有数据
		String name = attributeName.toLowerCase();
		Class cls = obj.getClass();
		String getmname = getMethodName(cls, "get" + name);
		if (getmname == null) {
			// 无法找到对应的方法
			throw new Exception("cls=" + cls.getName() + ",name=" + name
					+ ",无法找到对于的get方法");
		}

		// String getmname = "get" + name.substring(0, 1).toUpperCase()
		// + name.substring(1);
		Method getmet = null;
		try {
			getmet = cls.getMethod(getmname, null);
			if (getmet == null) {
				throw new Exception("cls=" + cls.getName() + ",method="
						+ getmname + ",无法找到对于的get方法");
			}
		} catch (NoSuchMethodException e) {
			throw new Exception("cls=" + cls.getName() + ",method=" + getmname
					+ ",无法找到对于的get方法");
		}

		Class returncls = getmet.getReturnType();
		// 转换值
		if (content != null) {
			value = this.read(content, returncls);
		}

		// 设置值到方法里
		String setmname = getMethodName(cls, "set" + name);
		// String setmname = "set" + name.substring(0, 1).toUpperCase()
		// + name.substring(1);
		if (setmname == null) {
			throw new Exception("cls=" + cls.getName() + ",name=" + name
					+ ",无法找到对于的set方法");
		}

		Method setmet = null;
		try {
			setmet = cls.getMethod(setmname, new Class[] { returncls });
			if (setmet == null) {
				throw new Exception("cls=" + cls.getName() + ",method="
						+ setmname + ",无法找到对于的set方法");
			}
		} catch (NoSuchMethodException e) {
			throw new Exception("cls=" + cls.getName() + ",method=" + setmname
					+ ",无法找到对于的set方法");
		}

		// Method m = obj.getClass().getMethod("set" + attributeName,
		// new Class[] { String.class });
		setmet.invoke(obj, new Object[] { value });
	}

	// 数据转换使用
	SimpleDateFormat long_df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
	SimpleDateFormat short_df = new SimpleDateFormat("yyyy-MM-dd");

	// 取得数据的转换
	public Object read(String content, Class cls) throws Exception {
		if (content == null) {
			return null;
		}
		// String content = reader.nextString();
		if (cls == Date.class) {
			Date date = null;
			if (content.length() > 19) {
				content = content.substring(0, 19);
				date = long_df.parse(content);
			} else if (content.length() > 10) {
				content = content.substring(0, 10);
				date = short_df.parse(content);
			}

			return date;
		} else if (cls == String.class) {
			return content;
		} else if (cls == Integer.class || cls == int.class) {
			Integer vv = Integer.valueOf(content);
			return vv;
		} else if (cls == Float.class || cls == float.class) {
			Float vv = Float.valueOf(content);
			return vv;
		} else if (cls == Long.class || cls == long.class) {
			Long vv = Long.valueOf(content);
			return vv;
		} else if (cls == Double.class || cls == double.class) {
			Double vv = Double.valueOf(content);
			return vv;
		} else if (cls == Short.class || cls == short.class) {
			Short vv = Short.valueOf(content);
			return vv;
		} else if (cls == Byte.class || cls == byte.class) {
			Byte vv = Byte.valueOf(content);
			return vv;
		} else if (cls == Boolean.class || cls == boolean.class) {
			Boolean vv = Boolean.valueOf(content);
			return vv;
		}

		return null;
	}

	/**
	 * 释放连接相关资源
	 * 
	 * @param r
	 * @param t
	 * @param c
	 */
	public static void freeDBResource(ResultSet r, Statement t, Connection c) {
		try {
			if (r != null) {
				r.close();
				r = null;
			}
			if (t != null) {
				t.close();
				t = null;
			}
			if (c != null) {
				c.close();
				c = null;
			}
		} catch (SQLException ex) {
			logger.error("数据库资源释放出错:" + ex.getMessage());
			// ex.printStackTrace();
		}
	}

	public static void freeDBResource(Connection c) {
		try {
			if (c != null) {
				c.close();
				c = null;
			}
		} catch (SQLException ex) {
			logger.error("数据库资源释放出错:" + ex.getMessage());
			// ex.printStackTrace();
		}
	}

	// ==============================返回List或者Map的查询操作、变量绑定方式执行insert或者update操作================================================//
	/**
	 * 返回List,Map中的Key不区分大小写
	 * 
	 * @param conn
	 * @param sql
	 * @return
	 * @throws Exception
	 */
	public List<HashMap<String, String>> executeQueryIgnoreCase(String sql)
			throws Exception {
		List<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		ResultSet rs = null;
		Statement st = null;
		Connection conn = null;
		try {
			conn = this.getConnection();
			st = conn.createStatement();
			rs = st.executeQuery(sql);
			ResultSetMetaData rsmd = rs.getMetaData();
			while (rs.next()) {
				HashMap<String, String> colMap = new CaseIgnoreHashMap<String, String>();
				for (int i = 1; i <= rsmd.getColumnCount(); i++) {
					String columName = rsmd.getColumnName(i);
					// 处理时间
					int columnType = rsmd.getColumnType(i);
//					String value = "";
//					switch (columnType) {
//					case Types.DATE:
//					case Types.TIMESTAMP:
//						Timestamp timestamp = rs.getTimestamp(i);
//						if (timestamp != null) {
//							value = sdf.format(timestamp);
//						} else {
//							value = null;
//						}
//						break;
//					default:
//						value = rs.getString(columName);
//						break;
//					}
//					colMap.put(columName.toLowerCase(),
//							"null".equals(value) ? null : value);
					String result = convOracleStr(rs, columName, columnType, sdf);
					colMap.put(columName.toLowerCase(),result);
					
				}
				list.add(colMap);
			}
		} catch (SQLException ex) {
			logger.debug(ex.getMessage());
			logger.debug("错误SQL:" + sql);
			ex.printStackTrace();
			throw ex;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			freeDBResource(rs, st, conn);
		}
		return list;
	}

	/**
	 * 变量绑定查询（可变参数方式） 返回List,Map中的Key不区分大小写 应用于查询条件固定，即sql语句固定
	 * 
	 * @param conn
	 * @param sql
	 *            :语句需要带问号
	 * @param params
	 *            ：可变参数，问号对应的参数值，支持不同的数据类型
	 * @return
	 * @throws Exception
	 */
	public List<HashMap<String, String>> exeQueryVarParamBand(String sql,
			Object... params) throws Exception {
		List<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		ResultSet rs = null;
		PreparedStatement pstmt = null;
		Connection conn = null;
		try {
			conn = this.getConnection();
			pstmt = conn.prepareStatement(sql);
			Object param = "";
			StringBuffer sbParams = new StringBuffer("参数params=");
			if (params != null) {
				for (int i = 0; i < params.length; i++) {
					int idx = i + 1;
					param = params[i];
					if (param instanceof Integer) {
						pstmt.setInt(idx, (Integer) param);
					} else if (param instanceof Double) {
						pstmt.setDouble(idx, (Double) param);
					} else if (param instanceof Boolean) {
						pstmt.setBoolean(idx, (Boolean) param);
					} else if (param instanceof Short) {
						pstmt.setShort(idx, (Short) param);
					} else if (param instanceof Long) {
						pstmt.setLong(idx, (Long) param);
					} else if (param instanceof Float) {
						pstmt.setFloat(idx, (Float) param);
					} else if (param instanceof Date) {
						pstmt.setDate(idx,
								new java.sql.Date(((Date) param).getTime()));
					} else if (param instanceof Byte) {
						pstmt.setByte(idx, (Byte) param);
					} else {
						pstmt.setString(idx, (String) param);
					}

					sbParams.append("[").append(idx).append(",")
							.append(params[i]).append("]");

				}
				System.err.println(sbParams.toString());
			}
			System.err.println("=======变量绑定查询（可变参数方式）,sql:"
					+ TransToStringUtil.transToString(sql, params));
			rs = pstmt.executeQuery();
			ResultSetMetaData rsmd = rs.getMetaData();
			while (rs.next()) {
				HashMap<String, String> colMap = new CaseIgnoreHashMap<String, String>();
				for (int i = 1; i <= rsmd.getColumnCount(); i++) {
					String columName = rsmd.getColumnName(i);
					// 处理时间
					int columnType = rsmd.getColumnType(i);
//					String value = "";
//					switch (columnType) {
//					case Types.DATE:
//					case Types.TIMESTAMP:
//						Timestamp timestamp = rs.getTimestamp(i);
//						if (timestamp != null) {
//							value = sdf.format(timestamp);
//						} else {
//							value = null;
//						}
//						break;
//					default:
//						value = rs.getString(columName);
//						break;
//					}
//					colMap.put(columName.toLowerCase(),
//							"null".equals(value) ? null : value);
					
					String result = convOracleStr(rs, columName, columnType, sdf);
					colMap.put(columName.toLowerCase(),result);
				}
				list.add(colMap);
			}
		} catch (SQLException ex) {
			logger.debug(ex.getMessage());
			logger.debug("错误SQL:" + sql);
			ex.printStackTrace();
			throw ex;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			freeDBResource(rs, pstmt, conn);
		}
		return list;
	}

	/**
	 * 变量绑定查询(查询条件封装到map) 注意map参数的key值是需要带入关系符号 返回List,Map中的Key不区分大小写
	 * 应用于查询条件不固定，即查询条件根据map变化
	 * 
	 * @param conn
	 * @param sql
	 *            语句不需带问号
	 * @param map
	 *            :key值为字段名称以及关系符号，建议空格分开 value可为不同数据类型
	 * @return
	 * @throws Exception
	 */
	public List<HashMap<String, String>> exeQueryVarBand(String sql,
			HashMap<String, Object> map) throws Exception {
		List<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		ResultSet rs = null;
		PreparedStatement pstmt = null;
		Connection conn = null;
		System.err.println("======变量绑定查询(查询条件封装到map)传入sql:" + sql);
		System.err.println("======变量绑定查询(查询条件封装到map)，查询条件:"
				+ TransToStringUtil.transToString(map));
		try {
			conn = this.getConnection();
			StringBuffer querySql = new StringBuffer(sql);
			if (!sql.toLowerCase().contains("where")) {
				querySql.append(" where 1=1");
			}
			ArrayList<Object> paraList = new ArrayList<Object>();
			if (map != null && !map.isEmpty()) {
				Iterator iter = map.entrySet().iterator();
				String key = "";
				Object val;
				while (iter.hasNext()) {
					Map.Entry entry = (Map.Entry) iter.next();
					key = (String) entry.getKey();
					val = entry.getValue();
					querySql.append(" and ").append(key).append("?");
					paraList.add(val);
				}
			}
			pstmt = conn.prepareStatement(querySql.toString());
			System.err.println("======变量绑定查询(查询条件封装到map),完成查询SQL:"
					+ TransToStringUtil.transToString(querySql.toString(),
							paraList.toArray()));
			if (!paraList.isEmpty()) {
				for (int i = 0; i < paraList.size(); i++) {
					if (paraList.get(i) instanceof Integer) {
						pstmt.setInt(i + 1, (Integer) paraList.get(i));
					} else if (paraList.get(i) instanceof Double) {
						pstmt.setDouble(i + 1, (Double) paraList.get(i));
					} else if (paraList.get(i) instanceof Boolean) {
						pstmt.setBoolean(i + 1, (Boolean) paraList.get(i));
					} else if (paraList.get(i) instanceof Short) {
						pstmt.setShort(i + 1, (Short) paraList.get(i));
					} else if (paraList.get(i) instanceof Long) {
						pstmt.setLong(i + 1, (Long) paraList.get(i));
					} else if (paraList.get(i) instanceof Float) {
						pstmt.setFloat(i + 1, (Float) paraList.get(i));
					} else if (paraList.get(i) instanceof Date) {
						pstmt.setDate(
								i + 1,
								new java.sql.Date(((Date) paraList.get(i))
										.getTime()));
					} else if (paraList.get(i) instanceof Byte) {
						pstmt.setByte(i + 1, (Byte) paraList.get(i));
					} else {
						pstmt.setString(i + 1, (String) paraList.get(i));
					}
				}
			}

			rs = pstmt.executeQuery();
			ResultSetMetaData rsmd = rs.getMetaData();
			while (rs.next()) {
				HashMap<String, String> colMap = new CaseIgnoreHashMap<String, String>();
				for (int i = 1; i <= rsmd.getColumnCount(); i++) {
					String columName = rsmd.getColumnName(i);
					// 处理时间
					int columnType = rsmd.getColumnType(i);
//					String value = "";
//					switch (columnType) {
//					case Types.DATE:
//					case Types.TIMESTAMP:
//						Timestamp timestamp = rs.getTimestamp(i);
//						if (timestamp != null) {
//							value = sdf.format(timestamp);
//						} else {
//							value = null;
//						}
//						break;
//					default:
//						value = rs.getString(columName);
//						break;
//					}
//					colMap.put(columName.toLowerCase(),
//							"null".equals(value) ? null : value);
					
					String result = convOracleStr(rs, columName, columnType, sdf);
					colMap.put(columName.toLowerCase(),result);
				}
				list.add(colMap);
			}
		} catch (SQLException ex) {
			logger.debug(ex.getMessage());
			logger.debug("错误SQL:" + sql);
			ex.printStackTrace();
			throw ex;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			freeDBResource(rs, pstmt, conn);
		}
		return list;
	}

	/**
	 * 变量绑定方式执行SQL,执行insert、update操作
	 * 
	 * @param conn
	 * @param _sql
	 * @throws Exception
	 */
	public void executeSQLVarBand(String sql, Object... params)
			throws Exception {
		if (StringUtils.isEmpty(sql))
			throw new Exception("执行出现异常：执行SQL为空");
		PreparedStatement pstmt = null;
		Connection conn = null;
		try {
			conn = this.getConnection();
			pstmt = conn.prepareStatement(sql);

			StringBuffer sbParams = new StringBuffer("参数params=");
			if (params != null) {
				Object param = "";
				for (int i = 0; i < params.length; i++) {
					int idx = i + 1;
					param = params[i];
					if (param instanceof Integer) {
						pstmt.setInt(idx, (Integer) param);
					} else if (param instanceof Double) {
						pstmt.setDouble(idx, (Double) param);
					} else if (param instanceof Boolean) {
						pstmt.setBoolean(idx, (Boolean) param);
					} else if (param instanceof Short) {
						pstmt.setShort(idx, (Short) param);
					} else if (param instanceof Long) {
						pstmt.setLong(idx, (Long) param);
					} else if (param instanceof Float) {
						pstmt.setFloat(idx, (Float) param);
					} else if (param instanceof Date) {
						pstmt.setDate(idx,
								new java.sql.Date(((Date) param).getTime()));
					} else if (param instanceof Byte) {
						pstmt.setByte(idx, (Byte) param);
					} else {
						pstmt.setString(idx, (String) param);
					}

					sbParams.append("[").append(idx).append(",")
							.append(params[i]).append("]");

				}
				System.err.println(sbParams.toString());
			}
			System.err.println("=======变量绑定方式执行SQL:"
					+ TransToStringUtil.transToString(sql, params));

			pstmt.execute();

		} catch (Exception ex) {
			ex.printStackTrace();
			logger.fatal("执行SQL：" + sql + "出现异常" + ex.toString());
		} finally {
			freeDBResource(null, pstmt, conn);
		}
	}

	/**
	 * 返回Map,Map中的Key不区分大小写
	 * 
	 * @param conn
	 * @param sql
	 * @return
	 * @throws Exception
	 */
	public HashMap<String, String> executeQueryForMap(String sql)
			throws Exception {
		HashMap<String, String> map = null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		ResultSet rs = null;
		Statement st = null;
		Connection conn = null;
		try {
			conn = this.getConnection();
			st = conn.createStatement();
			rs = st.executeQuery(sql);
			ResultSetMetaData rsmd = rs.getMetaData();
			if (rs.next()) {
				map = new CaseIgnoreHashMap<String, String>();
				for (int i = 1; i <= rsmd.getColumnCount(); i++) {
					String columName = rsmd.getColumnName(i);
					// 处理时间
					int columnType = rsmd.getColumnType(i);
//					String value = "";
//					switch (columnType) {
//					case Types.DATE:
//					case Types.TIMESTAMP:
//						Timestamp timestamp = rs.getTimestamp(i);
//						if (timestamp != null) {
//							value = sdf.format(timestamp);
//						} else {
//							value = null;
//						}
//						break;
//					default:
//						value = rs.getString(columName);
//						break;
//					}
//					map.put(columName.toLowerCase(),
//							"null".equals(value) ? null : value);
					String result = convOracleStr(rs, columName, columnType, sdf);
					map.put(columName.toLowerCase(),result);
				}
			}
		} catch (SQLException ex) {
			logger.debug(ex.getMessage());
			logger.debug("错误SQL:" + sql);
			ex.printStackTrace();
			throw ex;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			freeDBResource(rs, st, conn);
		}
		return map;
	}

	/**
	 * 变量绑定查询（可变参数方式） 返回Map,Map中的Key不区分大小写 应用于查询条件固定，即sql语句固定
	 * 
	 * @param conn
	 * @param sql
	 *            :语句需要带问号
	 * @param params
	 *            ：可变参数，问号对应的参数值，支持不同的数据类型
	 * @return
	 * @throws Exception
	 */
	public HashMap<String, String> exeQueryVarParamBandForMap(String sql,
			Object... params) throws Exception {
		HashMap<String, String> colMap = null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		ResultSet rs = null;
		PreparedStatement pstmt = null;
		Connection conn = null;
		try {
			conn = this.getConnection();
			pstmt = conn.prepareStatement(sql);
			Object param = "";
			StringBuffer sbParams = new StringBuffer("参数params=");
			if (params != null) {
				for (int i = 0; i < params.length; i++) {
					int idx = i + 1;
					param = params[i];
					if (param instanceof Integer) {
						pstmt.setInt(idx, (Integer) param);
					} else if (param instanceof Double) {
						pstmt.setDouble(idx, (Double) param);
					} else if (param instanceof Boolean) {
						pstmt.setBoolean(idx, (Boolean) param);
					} else if (param instanceof Short) {
						pstmt.setShort(idx, (Short) param);
					} else if (param instanceof Long) {
						pstmt.setLong(idx, (Long) param);
					} else if (param instanceof Float) {
						pstmt.setFloat(idx, (Float) param);
					} else if (param instanceof Date) {
						pstmt.setDate(idx,
								new java.sql.Date(((Date) param).getTime()));
					} else if (param instanceof Byte) {
						pstmt.setByte(idx, (Byte) param);
					} else {
						pstmt.setString(idx, (String) param);
					}

					sbParams.append("[").append(idx).append(",")
							.append(params[i]).append("]");

				}
				System.err.println(sbParams.toString());
			}
			System.err.println("=======变量绑定查询（可变参数方式）,sql:"
					+ TransToStringUtil.transToString(sql, params));
			rs = pstmt.executeQuery();
			ResultSetMetaData rsmd = rs.getMetaData();
			if (rs.next()) {
				colMap = new CaseIgnoreHashMap<String, String>();
				for (int i = 1; i <= rsmd.getColumnCount(); i++) {
					String columName = rsmd.getColumnName(i);
					// 处理时间
					int columnType = rsmd.getColumnType(i);
//					String value = "";
//					switch (columnType) {
//					case Types.DATE:
//					case Types.TIMESTAMP:
//						Timestamp timestamp = rs.getTimestamp(i);
//						if (timestamp != null) {
//							value = sdf.format(timestamp);
//						} else {
//							value = null;
//						}
//						break;
//					default:
//						value = rs.getString(columName);
//						break;
//					}
//					colMap.put(columName.toLowerCase(),
//							"null".equals(value) ? null : value);
					String result = convOracleStr(rs, columName, columnType, sdf);
					colMap.put(columName.toLowerCase(),result);
				}
			}
		} catch (SQLException ex) {
			logger.debug(ex.getMessage());
			logger.debug("错误SQL:" + sql);
			ex.printStackTrace();
			throw ex;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			freeDBResource(rs, pstmt, conn);
		}
		return colMap;
	}

}
