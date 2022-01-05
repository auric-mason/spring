package org.test.spring.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.test.spring.model.Recording;
import org.test.spring.services.RecordingService;

@RestController()
@RequestMapping("/api/v1")
public class RecordingsController {

  @Autowired private RecordingService recordingService;

  @RequestMapping(value = "/startRecording", method = RequestMethod.POST)
  @ResponseBody
  public ResponseEntity<?> startRecording(@RequestBody() Recording recording) {
    return new ResponseEntity<>(recordingService.startRecording(recording), HttpStatus.OK);
  }

  @RequestMapping(value = "/pauseRecording", method = RequestMethod.POST)
  @ResponseBody
  public ResponseEntity<?> pauseRecording(@PathVariable("recording") Recording recording) {
    return new ResponseEntity<>(recordingService.pauseRecording(recording.getId()), HttpStatus.OK);
  }

  @RequestMapping(value = "/resumeRecording", method = RequestMethod.POST)
  @ResponseBody
  public ResponseEntity<?> resumeRecording(@PathVariable("recording") Recording recording) {
    return new ResponseEntity<>(recordingService.resumeRecording(recording.getId()), HttpStatus.OK);
  }

  @RequestMapping(value = "/stopRecording", method = RequestMethod.POST)
  @ResponseBody
  public ResponseEntity<?> stopRecording(@PathVariable("recording") Recording recording) {
    return new ResponseEntity<>(recordingService.stopRecording(recording.getId()), HttpStatus.OK);
  }

  @RequestMapping(value = "/recentRecordings", method = RequestMethod.GET)
  @ResponseBody
  public ResponseEntity<?> listRecentRecordings() {
    return new ResponseEntity<>(recordingService.listRecentRecordings(), HttpStatus.OK);
  }
}
