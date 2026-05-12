package com.linkedin.linkedinclone.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@RequiredArgsConstructor
@Table(name = "job")
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column @NonNull
    private String title;

    @Column @NonNull @Size(max = 1500)
    private String description;

    @Column
    private Timestamp timestamp;

    @Column
    private String companyName;

    @Column
    private String location;

    @Column
    private String jobType;

    @Column
    private String experienceRequired;

    @Column
    private String salary;

    @Column(length = 1000)
    private String skillsRequired;

    @Column
    private String workMode;

    @Column
    private Integer numberOfOpenings;

    @Column
    private String applicationDeadline;

    @Column
    private String status = "ACTIVE";

    @ManyToOne(cascade = CascadeType.ALL)
    @JsonIgnoreProperties(value = {"jobsCreated","jobApplied","recommendedJobs","interestReactions"},allowSetters = true)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private User userMadeBy;

    @ManyToMany(fetch = FetchType.EAGER)
    @JsonIgnoreProperties(value = {"jobApplied","jobsCreated","recommendedJobs","interestReactions"},allowSetters = true)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Set<User> usersApplied = new HashSet<>();

    @ManyToMany(mappedBy="recommendedJobs",fetch = FetchType.EAGER)
    @JsonIgnoreProperties(value = {"recommendedJobs","jobsCreated","jobApplied","interestReactions","usersFollowing","userFollowedBy","posts"},allowSetters = true)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private List<User> recommendedTo = new ArrayList<>();
}
