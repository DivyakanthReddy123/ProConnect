import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthenticationService } from '../../authentication.service';
import { JobsService } from '../../services/jobs.service';
import { Job } from '../../model/job';
import { UserDetails } from '../../model/user-details';

@Component({ selector: 'app-recruiter-my-jobs', templateUrl: './recruiter-my-jobs.component.html', styleUrls: ['./recruiter-my-jobs.component.css'] })
export class RecruiterMyJobsComponent implements OnInit {
  userDetails: UserDetails;
  jobs: Job[] = [];

  constructor(private authService: AuthenticationService, private jobsService: JobsService, private router: Router) {}

  ngOnInit(): void {
    this.authService.getLoggedInUser().subscribe(userDetails => this.userDetails = userDetails);
    if (!this.userDetails) { this.router.navigate(['/login']); return; }
    this.loadJobs();
  }

  loadJobs(): void {
    this.jobsService.getRecruiterJobs(this.userDetails.id).subscribe(jobs => this.jobs = jobs);
  }

  closeJob(job: Job): void {
    job.status = 'CLOSED';
    this.jobsService.updateJob(this.userDetails.id, job.id, job).subscribe(() => this.loadJobs());
  }

  deleteJob(job: Job): void {
    if (!confirm('Delete this job post?')) { return; }
    this.jobsService.deleteJob(this.userDetails.id, job.id).subscribe(() => this.loadJobs());
  }
}
