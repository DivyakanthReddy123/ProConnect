package com.linkedin.linkedinclone.controllers;

import com.linkedin.linkedinclone.enumerations.RoleType;
import com.linkedin.linkedinclone.exceptions.UserNotFoundException;
import com.linkedin.linkedinclone.model.Job;
import com.linkedin.linkedinclone.model.Picture;
import com.linkedin.linkedinclone.model.Role;
import com.linkedin.linkedinclone.model.User;
import com.linkedin.linkedinclone.recommendation.RecommendationAlgos;
import com.linkedin.linkedinclone.repositories.CommentRepository;
import com.linkedin.linkedinclone.repositories.JobsRepository;
import com.linkedin.linkedinclone.repositories.UserRepository;
import com.linkedin.linkedinclone.services.UserService;
import com.linkedin.linkedinclone.services.NotificationDeliveryService;
import com.linkedin.linkedinclone.model.Notification;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.util.*;

import static com.linkedin.linkedinclone.utils.PictureSave.decompressBytes;
import static com.linkedin.linkedinclone.enumerations.NotificationType.JOB_POST;

@RestController
@AllArgsConstructor
public class JobsController {

    @Autowired
    UserService userService;

    private final UserRepository userRepository;
    private final JobsRepository jobRepository;
    private final CommentRepository commentRepository;
    private final com.linkedin.linkedinclone.repositories.NotificationRepository notificationRepository;
    private final NotificationDeliveryService notificationDeliveryService;

    @CrossOrigin(origins = "*")
    @PostMapping("/in/{id}/new-job")
    public ResponseEntity newJob(@PathVariable Long id, @RequestBody Job job) {
        User currentUser = getUser(id);
        if (!hasRole(currentUser, RoleType.RECRUITER) && !hasRole(currentUser, RoleType.ADMIN)) {
            return ResponseEntity.status(403).body(errorMessage("Only recruiters can post jobs."));
        }
        job.setUserMadeBy(currentUser);
        job.setTimestamp(new Timestamp(System.currentTimeMillis()));
        if (job.getCompanyName() == null || job.getCompanyName().trim().isEmpty()) {
            job.setCompanyName(currentUser.getCurrentCompany());
        }
        if (job.getLocation() == null || job.getLocation().trim().isEmpty()) {
            job.setLocation(currentUser.getCity());
        }
        if (job.getStatus() == null || job.getStatus().trim().isEmpty()) {
            job.setStatus("ACTIVE");
        }
        jobRepository.save(job);
        List<User> recipients = getJobNotificationRecipients(currentUser);
        for (User recipient : recipients) {
            notificationRepository.save(new Notification(JOB_POST, recipient, job));
        }
        notificationDeliveryService.sendJobPostedNotifications(currentUser, job, recipients);
        return ResponseEntity.ok("\"Job created with success!\"");
    }

    @CrossOrigin(origins = "*")
    @GetMapping("/in/{id}/jobs")
    public Set<Job> getJobs(@PathVariable Long id) {
        getUser(id);
        Set<Job> jobs = new HashSet<>(jobRepository.findAll());
        hydrateJobs(jobs);
        return jobs;
    }

    @CrossOrigin(origins = "*")
    @GetMapping("/in/{id}/recruiter/jobs")
    public List<Job> getRecruiterJobs(@PathVariable Long id) {
        User recruiter = getUser(id);
        List<Job> jobs = jobRepository.findByUserMadeBy(recruiter);
        hydrateJobs(new HashSet<>(jobs));
        Collections.reverse(jobs);
        return jobs;
    }

    @CrossOrigin(origins = "*")
    @GetMapping("/in/{id}/recruiter/dashboard")
    public Map<String, Object> getRecruiterDashboard(@PathVariable Long id) {
        User recruiter = getUser(id);
        List<Job> jobs = jobRepository.findByUserMadeBy(recruiter);
        int activeJobs = 0;
        int totalApplicants = 0;
        for (Job job : jobs) {
            if (job.getStatus() == null || job.getStatus().equalsIgnoreCase("ACTIVE")) {
                activeJobs++;
            }
            totalApplicants += job.getUsersApplied().size();
        }
        Map<String, Object> dashboard = new LinkedHashMap<>();
        dashboard.put("totalJobsPosted", jobs.size());
        dashboard.put("totalActiveJobs", activeJobs);
        dashboard.put("totalApplicants", totalApplicants);
        dashboard.put("recentJobs", jobs.size() > 5 ? jobs.subList(0, 5) : jobs);
        return dashboard;
    }

    @CrossOrigin(origins = "*")
    @PutMapping("/in/{id}/jobs/{jobId}")
    public ResponseEntity updateJob(@PathVariable Long id, @PathVariable Long jobId, @RequestBody Job incomingJob) {
        User recruiter = getUser(id);
        Job job = getJob(jobId);
        if (!isOwnerOrAdmin(recruiter, job)) {
            return ResponseEntity.status(403).body(errorMessage("You can update only your own job posts."));
        }
        copyJobFields(incomingJob, job);
        jobRepository.save(job);
        return ResponseEntity.ok("\"Job updated with success!\"");
    }

    @CrossOrigin(origins = "*")
    @DeleteMapping("/in/{id}/jobs/{jobId}")
    public ResponseEntity deleteJob(@PathVariable Long id, @PathVariable Long jobId) {
        User recruiter = getUser(id);
        Job job = getJob(jobId);
        if (!isOwnerOrAdmin(recruiter, job)) {
            return ResponseEntity.status(403).body(errorMessage("You can delete only your own job posts."));
        }
        jobRepository.delete(job);
        return ResponseEntity.ok("\"Job deleted with success!\"");
    }

    @CrossOrigin(origins = "*")
    @PutMapping("/in/{id}/jobs/make-application/{jobId}")
    public ResponseEntity newApplication(@PathVariable Long id, @PathVariable Long jobId) {
        User currentUser = getUser(id);
        if (hasRole(currentUser, RoleType.RECRUITER)) {
            return ResponseEntity.status(403).body(errorMessage("Recruiters cannot apply to jobs."));
        }
        Job job = getJob(jobId);
        Set<User> usersApplied = job.getUsersApplied();
        if (!usersApplied.contains(currentUser)) {
            usersApplied.add(currentUser);
            job.setUsersApplied(usersApplied);
            jobRepository.save(job);
        } else {
            return ResponseEntity.badRequest().body(errorMessage("Application has already been made!"));
        }
        return ResponseEntity.ok("\"Application created with success!\"");
    }

    @CrossOrigin(origins = "*")
    @GetMapping("/in/{id}/jobs/{jobId}/applicants")
    public Set<User> getJobApplicants(@PathVariable Long id, @PathVariable Long jobId) {
        User recruiter = getUser(id);
        Job job = getJob(jobId);
        if (!isOwnerOrAdmin(recruiter, job)) {
            return new HashSet<>();
        }
        hydrateUsers(job.getUsersApplied());
        return job.getUsersApplied();
    }

    @CrossOrigin(origins = "*")
    @GetMapping("/in/{id}/recommended-jobs")
    public List<Job> getRecommendedJobs(@PathVariable Long id) {
        RecommendationAlgos recAlgos = new RecommendationAlgos();
        recAlgos.recommendedJobs(userRepository, jobRepository, userService);
        User currentUser = getUser(id);
        List<Job> recommendedJobs = new ArrayList<>();
        if (currentUser.getRecommendedJobs().size() != 0) {
            recommendedJobs = currentUser.getRecommendedJobs();
        } else {
            return new ArrayList<>(getJobs(id));
        }
        Collections.reverse(recommendedJobs);
        hydrateJobs(new HashSet<>(recommendedJobs));
        return recommendedJobs;
    }

    private User getUser(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User with " + id + " not found"));
    }

    private Job getJob(Long id) {
        return jobRepository.findById(id).orElseThrow(() -> new UserNotFoundException("Job not found"));
    }

    private boolean hasRole(User user, RoleType roleType) {
        for (Role role : user.getRoles()) {
            if (role.getName() == roleType) {
                return true;
            }
        }
        return false;
    }

    private boolean isOwnerOrAdmin(User user, Job job) {
        return hasRole(user, RoleType.ADMIN) || (job.getUserMadeBy() != null && job.getUserMadeBy().getId().equals(user.getId()));
    }

    private List<User> getJobNotificationRecipients(User recruiter) {
        List<User> recipients = new ArrayList<>();
        for (User user : userRepository.findAll()) {
            if (user.getId().equals(recruiter.getId())) {
                continue;
            }
            if (hasRole(user, RoleType.PROFESSIONAL)) {
                recipients.add(user);
            }
        }
        return recipients;
    }

    private String errorMessage(String message) {
        return "{\"timestamp\": \"" + new Date().toString() + "\",\"status\":400,\"error\":\"Bad Request\",\"message\":\"" + message + "\"}";
    }

    private void copyJobFields(Job source, Job target) {
        if (source.getTitle() != null) target.setTitle(source.getTitle());
        if (source.getDescription() != null) target.setDescription(source.getDescription());
        if (source.getCompanyName() != null) target.setCompanyName(source.getCompanyName());
        if (source.getLocation() != null) target.setLocation(source.getLocation());
        if (source.getJobType() != null) target.setJobType(source.getJobType());
        if (source.getExperienceRequired() != null) target.setExperienceRequired(source.getExperienceRequired());
        if (source.getSalary() != null) target.setSalary(source.getSalary());
        if (source.getSkillsRequired() != null) target.setSkillsRequired(source.getSkillsRequired());
        if (source.getWorkMode() != null) target.setWorkMode(source.getWorkMode());
        if (source.getNumberOfOpenings() != null) target.setNumberOfOpenings(source.getNumberOfOpenings());
        if (source.getApplicationDeadline() != null) target.setApplicationDeadline(source.getApplicationDeadline());
        if (source.getStatus() != null) target.setStatus(source.getStatus());
    }

    private void hydrateJobs(Set<Job> jobs) {
        for (Job job : jobs) {
            if (job.getUserMadeBy() != null) hydrateUser(job.getUserMadeBy());
            hydrateUsers(job.getUsersApplied());
        }
    }

    private void hydrateUsers(Set<User> users) {
        for (User user : users) {
            hydrateUser(user);
        }
    }

    private void hydrateUser(User user) {
        Picture pic = user.getProfilePicture();
        if (pic != null && pic.isCompressed()) {
            Picture tempPicture = new Picture(pic.getId(), pic.getName(), pic.getType(), decompressBytes(pic.getBytes()));
            tempPicture.setCompressed(false);
            user.setProfilePicture(tempPicture);
        }
    }
}
