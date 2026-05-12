package com.linkedin.linkedinclone.repositories;

import com.linkedin.linkedinclone.model.Job;
import com.linkedin.linkedinclone.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobsRepository extends JpaRepository<Job, Long> {
    List<Job> findByUserMadeBy(User userMadeBy);
}
