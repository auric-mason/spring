package org.test.spring.dao;

import org.springframework.data.repository.CrudRepository;
import org.test.spring.model.Recordings;

public interface RecordingsRepository extends CrudRepository<Recordings, Long> {
    Recordings findByName(String name);
}