package com.redhat.consulting.demos.entity;


public class PostResponseEntity extends PostEntity {
  private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "PostResultEntity{" +
                "id=" + id +
                '}';
    }
}
