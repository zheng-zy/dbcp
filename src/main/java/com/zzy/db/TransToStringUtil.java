package com.zzy.db;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

/**
 * TransToStringUtil.java 
 * @desc   组装参数为打印日志格式   
 * @author zhengzy
 * @date   2016年1月13日下午4:34:04
 */
public class TransToStringUtil {
	/**
	 * 将对象中的字段和字段值拼接成字符串
	 * @param obj
	 * @return
	 */
	
	public static String transToString(Object obj) {
		String str = "";
		try {
			StringBuffer sb = new StringBuffer();
			String className = obj.getClass().getName();
			sb.append(className+"：【");
			Field[] fs = obj.getClass().getDeclaredFields();
			for (Field f : fs) {
				f.setAccessible(true);
				String key = f.getName();
				String value = f.get(obj) == null ? "" : f.get(obj).toString();
				sb.append(key+"="+value+",");
			}
			str = sb.toString();
			if (StringUtils.isNotBlank(str)) {
				if (str.endsWith(",")) {
					str = str.substring(0, str.length()-1);
				}
			}
			str = str + "】";
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("将对象中的字段和字段值拼接成字符串过程出错！");
		}
		return str;
	}
	/**
	 * 将Map中的key和value拼接成字符串
	 * @param varsMap
	 * @return
	 */
	public static String transToString(Map varsMap) {
		StringBuffer sb = new StringBuffer();
		sb.append("【");
		Set<Entry> entrySet = varsMap.entrySet();
		for (Entry entry : entrySet) {
			String key = entry.getKey().toString();
			String value = entry.getValue() == null ? "" : entry.getValue().toString();
			sb.append(key+"="+value+",");
		}
		String str = sb.toString();
		if (StringUtils.isNotBlank(str)) {
			if (str.endsWith(",")) {
				str = str.substring(0, str.length()-1);
			}
		}
		str = str + "】";
		return str;
	}
	/**
	 * 将多个变量拼接成字符串
	 * @param varKey 变量名数组
	 * @param varValue 变量值数组
	 * @return
	 */
	public static String transToString(String[] varKey, Object[] varValue) {
		if (varKey.length != varValue.length) {
			throw new RuntimeException("传入的变量名数组和变量值数组元素的个数不一致！");
		}
		StringBuffer sb = new StringBuffer();
		sb.append("【");
		for (int i = 0; i < varKey.length; i++) {
			String key = varKey[i];
			String value = varValue[i].toString();
			sb.append(key+"="+value+",");
		}
		String str = sb.toString();
		if (StringUtils.isNotBlank(str)) {
			if (str.endsWith(",")) {
				str = str.substring(0, str.length()-1);
			}
		}
		str = str + "】";
		return str;
	}
	
	/**
	 * 将SQL语句的?转成真实值然后输出SQL
	 * @param sql
	 * @param params 可变参数，?对应的参数值
	 * @return
	 */
	public static String transToString(String sql, Object[] params) {
		try{
			int count = 0;
			for (int i = 0; i < sql.length(); i++) {
				if (sql.charAt(i) == '?') {
					count++;
				}
			}
			if (params.length != count) {
				throw new RuntimeException("传入的变量数组元素的个数和SQL语句中的参数个数不一致！");
			}
			for (Object param : params) {
				sql = sql.replaceFirst("\\?", param==null?"":"'"+param.toString()+"'");
			}
			
		}catch (Exception e) {
			e.printStackTrace();
		}

		return sql;
	}
	
	
	/**将多个变量拼接成字符串
	 * @param objects 格式为：变量名称,变量值,变量名称,变量值,...
	 * @return
	 */
	public static String transVarsToString(Object ...objects) {
		String ret="";
		try{
			int i=0;
			StringBuffer sb=new StringBuffer();
			sb.append("【");
			for(Object ob:objects){
				i++;
				if(i%2==0){
					sb.append(transObj2String(ob)).append(",");
				}else{
					sb.append(transObj2String(ob)).append("=");;
				}
			}
			 ret = sb.toString();
			if (StringUtils.isNotBlank(ret)) {
				if (ret.endsWith(",")) {
					ret = ret.substring(0, ret.length()-1);
				}
			}
			ret = ret + "】";
		}catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
		
	}
	
	
	/**
     * 基本类型转换String
     * @param o
     * @return
     */
    private static String transObj2String(Object o) {
        String ret = "";
        try {
            if (o instanceof Boolean) {
                ret = ((Boolean) o).toString();
            } else if (o instanceof Byte) {
                ret = ((Byte) o).toString();
            } else if (o instanceof Character) {
                ret = ((Character) o).toString();
            } else if (o instanceof java.util.Date) {
                ret = ((java.util.Date) o).toString();                
            } else if (o instanceof Double) {
                ret = ((Double) o).toString();
            } else if (o instanceof Float) {
                ret = ((Float) o).toString();
            } else if (o instanceof Integer) {
                ret = ((Integer) o).toString();
            } else if (o instanceof Long) {
                ret = ((Long) o).toString();
            } else if (o instanceof Short) {
                ret = ((Short) o).toString();
            } else if (o instanceof java.lang.Number) {
                ret = ((Number) o).toString();                
            } else if (o instanceof java.math.BigDecimal) {
                ret = ((java.math.BigDecimal) o).toString(); 
            } else if (o instanceof java.math.BigInteger) {
                ret = ((java.math.BigInteger) o).toString();                   
            } else {
                ret = (String) o;
            }
        } catch (Exception ex) {
            System.err.println("//======基本类型转成String出现异常!!");
        }
        return ret;
    }
    
    
    public static void main(String[] args){
    	String ret=transVarsToString("name","hongjt","age","30");
    	System.out.println(ret);
    }
	
	
}
