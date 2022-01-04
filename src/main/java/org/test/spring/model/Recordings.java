package org.test.spring.model;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "recordings")
public class Recordings {

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

    //These will never be persisted
    @Transient
    private List<TempFiles> tmpFiles;

    public Recordings () {}

    public Recordings(String name, Integer duration) {
        this.name = name;
        this.duration = duration;
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

    public List<TempFiles>  getTmpFile() {
        return tmpFiles;
    }

    public void setTmpFile(List<TempFiles>  tmpFiles) {
        this.tmpFiles = tmpFiles;
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
        public Recordings build() {
            return new Recordings(this.name, this.duration);
        }

    }
}
