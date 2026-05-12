import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthenticationService } from '../../authentication.service';
import { JobsService } from '../../services/jobs.service';
import { UserService } from '../../services/user.service';
import { Job } from '../../model/job';
import { User } from '../../model/user';
import { UserDetails } from '../../model/user-details';

@Component({ selector: 'app-recruiter-post-job', templateUrl: './recruiter-post-job.component.html', styleUrls: ['./recruiter-post-job.component.css'] })
export class RecruiterPostJobComponent implements OnInit {
  job: Job = new Job();
  user: User = new User();
  userDetails: UserDetails;
  submitted = false;

  constructor(private authService: AuthenticationService, private jobsService: JobsService, private userService: UserService, private router: Router) {}

  ngOnInit(): void {
    this.authService.getLoggedInUser().subscribe(userDetails => this.userDetails = userDetails);
    if (!this.userDetails) { this.router.navigate(['/login']); return; }
    this.job.jobType = 'Full-time';
    this.job.workMode = 'Hybrid';
    this.job.status = 'ACTIVE';
    this.userService.getUser(this.userDetails.id.toString()).subscribe(user => {
      Object.assign(this.user, user);
      this.job.companyName = this.user.currentCompany;
      this.job.location = this.user.city;
    });
  }

  submit(form): void {
    this.submitted = true;
    if (!form.form.valid) { return; }
    this.jobsService.addJob(this.job, this.userDetails.id).subscribe(() => this.router.navigate(['/recruiter/jobs']));
  }
}
