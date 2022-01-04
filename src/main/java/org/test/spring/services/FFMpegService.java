package org.test.spring.services;

import org.springframework.stereotype.Service;
import org.test.spring.model.TempFiles;

import java.util.List;

@Service
public class FFMpegService {
    public void combineFiles(String outputFileName, List<TempFiles> files){};
}
