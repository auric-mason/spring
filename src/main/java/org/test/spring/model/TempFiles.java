package org.test.spring.model;

import java.io.File;

public record TempFiles(File tmpFile, Integer duration, Process process) {
}
