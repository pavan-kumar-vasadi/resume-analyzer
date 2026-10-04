package com.resumeanalyzer.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "skills")
public class Skill {
    @Id private Long id;
    private String name;
    private String category;
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getCategory() { return category; }
}
