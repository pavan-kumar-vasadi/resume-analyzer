package com.resumeanalyzer.repository;

import com.resumeanalyzer.entity.Skill;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SkillRepository extends JpaRepository<Skill, Long> {}
