package com.bellj.resourceserver.repository;

import com.bellj.resourceserver.entity.Profile;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfileRepository extends CrudRepository<Profile, Long> {
  Optional<Profile> findByUserId(Long userId);
}
