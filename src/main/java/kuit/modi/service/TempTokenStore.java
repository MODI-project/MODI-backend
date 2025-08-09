package kuit.modi.service;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TempTokenStore {
    private final Map<String, String> store = new ConcurrentHashMap<>();

    public void save(String key, String token) {
        store.put(key, token);
    }

    public String get(String key) {
        return store.remove(key); // 한번 꺼내면 삭제
    }
}