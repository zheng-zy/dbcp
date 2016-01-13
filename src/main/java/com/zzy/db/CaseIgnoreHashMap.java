package com.zzy.db;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 
 * CaseIgnoreHashMap.java 
 * @desc   忽略大小写KEY的Map   
 * @author zhengzy
 * @date   2016年1月13日下午4:28:43
 */
public class CaseIgnoreHashMap<K,V> extends LinkedHashMap<K,V> {
	private static final long serialVersionUID = 7808932931213786834L;
	private final Map<String, String> lowerCaseMap = new LinkedHashMap<String, String>();//key值都是小写的（这个key是起到中间值的作用） 真正的key值放在该Map中的value

    @Override
    public boolean containsKey(Object key) {
        String realKey = lowerCaseMap.get(key.toString().toLowerCase());
        return super.containsKey(realKey);
    }

    @Override
    public V get(Object key) {
    	String realKey = lowerCaseMap.get(key.toString().toLowerCase());
        return super.get(realKey);
    }

    @Override
    public V put(K key, V value) {
        String oldKey = lowerCaseMap.put(key.toString().toLowerCase(), key.toString());
        V oldValue = super.remove(oldKey);
        super.put(key, value);
        return oldValue;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> t) {
        for (Map.Entry<? extends K, ? extends V> entry : t.entrySet()) {
            K key = entry.getKey();
            V value = entry.getValue();
            this.put(key, value);
        }
    }

    @Override
    public V remove(Object key) {
        String realKey = lowerCaseMap.remove(key.toString().toLowerCase());
        return super.remove(realKey);
    }
    
	@Override
	public void clear() {
		lowerCaseMap.clear();
		super.clear();
	}
}
