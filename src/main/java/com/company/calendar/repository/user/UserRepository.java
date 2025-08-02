package com.company.calendar.repository.user;

import com.company.calendar.entity.User;

import java.util.Optional;
import java.util.List;
import java.util.Set;

public interface UserRepository {
    void save(User user);
    Optional<User> findById(String id);
    void deleteById(String id);
    List<User> findByIds(Set<String> ids);
}
