package org.test.spring.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractService {
    protected Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());
}
