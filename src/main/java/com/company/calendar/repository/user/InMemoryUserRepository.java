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
    private final Map<String, User> emailToUser = new ConcurrentHashMap<>();
    private final Object userLock = new Object();

    @Override
    public void save(User user) {
        synchronized (userLock) {
            userStore.put(user.getId(), user);
            emailToUser.put(user.getUserMetadata().getEmail(), user);
        }
    }

    public boolean saveIfAbsent(User user) {
        synchronized (userLock) {
            if (userStore.containsKey(user.getId())) {
                return false;
            }

            if (emailToUser.containsKey(user.getUserMetadata().getEmail())) {
                return false;
            }

            userStore.put(user.getId(), user);
            emailToUser.put(user.getUserMetadata().getEmail(), user);
            return true;
        }
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

    @Override
    public Optional<User> findByEmail(String email) {
        return Optional.ofNullable(emailToUser.get(email));
    }

    @Override
    public boolean existsByEmailExcludingId(String email, String excludedUserId) {
        User existing = emailToUser.getOrDefault(email, null);
        return existing != null && !existing.getId().equals(excludedUserId);
    }
}
