package org.test.spring.dao;

import org.springframework.data.repository.CrudRepository;
import org.test.spring.model.Recording;

public interface RecordingsRepository extends CrudRepository<Recording, Long> {
  Recording findByName(String name);
}
