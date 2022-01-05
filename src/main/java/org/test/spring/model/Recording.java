package org.test.spring.model;

import org.test.spring.services.RecordingService;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "recordings")
public class Recording {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = "id", nullable = false)
  private Long id;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "duration", nullable = false)
  private Integer duration;

  @Column(name = "completed", nullable = true)
  private Integer completed;

  // might want to track the ffmpeg changes.

  @Column(name = "status", nullable = true)
  private RecordingService.Status status;

  // These will never be persisted
  @Transient private List<TempFiles> tmpFiles = new ArrayList<>();

  public Recording() {}

  public Recording(String name, Integer duration) {
    this.name = name;
    this.duration = duration;
  }

  public RecordingService.Status getStatus() {
    return status;
  }

  public void setStatus(RecordingService.Status status) {
    this.status = status;
  }

  public Integer getCompleted() {
    return completed;
  }

  public void setCompleted(Integer completed) {
    this.completed = completed;
  }

  public List<TempFiles> getTmpFiles() {
    return tmpFiles;
  }

  public void setTmpFiles(List<TempFiles> tmpFiles) {
    this.tmpFiles = tmpFiles;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Integer getDuration() {
    return duration;
  }

  public void setDuration(Integer duration) {
    this.duration = duration;
  }

  public static class Builder {
    private String name;
    private Integer duration;

    public Builder name(String name) {
      this.name = name;
      return this;
    }

    public Builder duration(Integer duration) {
      this.duration = duration;
      return this;
    }

    public Recording build() {
      return new Recording(this.name, this.duration);
    }
  }
}
