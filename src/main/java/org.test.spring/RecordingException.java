package org.test.spring;

public class RecordingException extends Exception {

  public RecordingException(String message) {
    super(message);
  }

  public RecordingException(String message, Throwable cause) {
    super(message, cause);
  }

  public RecordingException(Throwable cause) {
    super(cause);
  }

  protected RecordingException(
      String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
