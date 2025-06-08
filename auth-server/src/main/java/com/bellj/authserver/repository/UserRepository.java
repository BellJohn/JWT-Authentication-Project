package com.bellj.authserver.repository;

import com.bellj.authserver.entity.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {
  User findByUsername(String username);
}
