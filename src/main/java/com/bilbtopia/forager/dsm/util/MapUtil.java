package com.bilbtopia.forager.dsm.util;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MapUtil {

	public static Map<String, Object> getValuePairs(List<Object> values) {
		if (values == null || values.isEmpty()) {
			return Collections.emptyMap();
		}
		Map<String, Object> pairs = new HashMap<>();
		final int n = values.size();
		for (int i = 0; i < n; i += 2) {
			pairs.put((String) values.get(i), values.get(i + 1));
		}
		return pairs;
	}

	public static class MapBuilder<K, V> implements Map<K, V> {

		private final Map<K, V> map = new HashMap<>();

		public MapBuilder<K, V> and(K key, V val) {
			if (key != null && val != null) {
				map.put(key, val);
			}
			return this;
		}

		public Map<K, V> build() {
			return map;
		}

		@Override
		public int size() {
			return map.size();
		}

		@Override
		public boolean isEmpty() {
			return map.isEmpty();
		}

		@Override
		public boolean containsKey(Object key) {
			return map.containsKey(key);
		}

		@Override
		public boolean containsValue(Object value) {
			return map.containsValue(value);
		}

		@Override
		public V get(Object key) {
			return map.get(key);
		}

		@Override
		public V put(K key, V value) {
			return map.put(key, value);
		}

		@Override
		public V remove(Object key) {
			return map.remove(key);
		}

		@Override
		public void putAll(Map<? extends K, ? extends V> m) {
			map.putAll(m);
		}

		@Override
		public void clear() {
			map.clear();
		}

		@Override
		public Set<K> keySet() {
			return map.keySet();
		}

		@Override
		public Collection<V> values() {
			return map.values();
		}

		@Override
		public Set<Entry<K, V>> entrySet() {
			return map.entrySet();
		}
	}

	public static <K> MapBuilder<K, Object> ofHetero(K key, Object val) {
		MapBuilder<K, Object> builder = new MapBuilder<>();
		return builder.and(key, val);
	}

	public static <K, V> MapBuilder<K, V> of(K key, V val) {
		MapBuilder<K, V> builder = new MapBuilder<>();
		return builder.and(key, val);
	}
}
