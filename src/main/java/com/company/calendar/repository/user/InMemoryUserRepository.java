package com.company.calendar.repository.user;

import com.company.calendar.entity.User;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Repository
@ConditionalOnProperty(name = "user.repository", havingValue = "in-memory", matchIfMissing = true)
public class InMemoryUserRepository implements UserRepository {

    private final Map<String, User> userStore = new ConcurrentHashMap<>();

    @Override
    public void save(User user) {
        userStore.put(user.getId(), user);
    }

    public boolean saveIfAbsent(User user) {
        return userStore.computeIfAbsent(user.getId(), id -> user) == user;
    }

    @Override
    public Optional<User> findById(String id) {
        return Optional.ofNullable(userStore.get(id));
    }

    @Override
    public void deleteById(String id) {
        userStore.remove(id);
    }

    @Override
    public List<User> findByIds(Set<String> ids) {
        return userStore.entrySet().stream()
                .filter(entry -> ids.contains(entry.getKey()))
                .map(Map.Entry::getValue)
                .toList();
    }
}
