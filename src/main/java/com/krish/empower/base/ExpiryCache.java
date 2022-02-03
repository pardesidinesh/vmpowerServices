package com.krish.empower.base;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExpiryCache<V> {
	private static Logger logger = LoggerFactory.getLogger(ExpiryCache.class);
	
	private Map<String, Entry<V>> map = new ConcurrentHashMap<>();
	private Map<Long, Entry<V>> tsMap = new TreeMap<>();
	private ReentrantLock rlock = new ReentrantLock();
	private long expiryTimeout = 30000;
	
	private Timer timer = new Timer();
	
	public ExpiryCache(long expiryTimeout) {
		this.expiryTimeout = expiryTimeout;
		timer.schedule(new ExpiryTask(), this.expiryTimeout, this.expiryTimeout);
	}
	
	public V get(String key) {
		V value = null;
		Entry<V> entry = map.get(key);
		if(entry != null) {
			value = entry.value;
			try {
				rlock.lock();
				tsMap.remove(entry.ts);
				entry.ts =System.nanoTime();
				tsMap.put(entry.ts, entry);
			}finally {
				rlock.unlock();
			}
		}
		return value;
	}
	
	public void put(String key, V value) {
		Entry<V> entry = new Entry();
		entry.key = key;
		entry.value =value;
		entry.ts=System.nanoTime();
		map.put(key, entry);
		try {
			rlock.lock();
			tsMap.put(entry.ts, entry);
		}finally {
			rlock.unlock();
		}
	}
	
	public void close() {
		map.clear();
		map=null;
		try {
			rlock.lock();
			tsMap.clear();
			tsMap=null;
		}finally {
			rlock.unlock();
		}
		timer.cancel();
		timer=null;
	}
	
	private class ExpiryTask extends TimerTask{
		public ExpiryTask() {
			
		}
		
		@Override
		public void run() {
			long now = System.nanoTime();
			try {
				rlock.lock();
				Set<Long> keySet = tsMap.keySet();
				Iterator<Long> iter = keySet.iterator();
				while(iter.hasNext()) {
					long l = iter.next();
					Entry<V> e = tsMap.get(l);
					long interval =  now - e.ts;
					if(interval >= (ExpiryCache.this.expiryTimeout * 1000000)) {
						iter.remove();
						logger.info("Removed Key : "+e.key);
					}else {
						break;
					}
					
				}
			}finally {
				rlock.unlock();
			}
		}
	}
}

class Entry<V> {
	public String key;
	public V value;
	public long ts;
}