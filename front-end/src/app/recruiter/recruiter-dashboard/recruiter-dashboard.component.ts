import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthenticationService } from '../../authentication.service';
import { JobsService } from '../../services/jobs.service';
import { UserDetails } from '../../model/user-details';

@Component({ selector: 'app-recruiter-dashboard', templateUrl: './recruiter-dashboard.component.html', styleUrls: ['./recruiter-dashboard.component.css'] })
export class RecruiterDashboardComponent implements OnInit {
  userDetails: UserDetails;
  stats: any = { totalJobsPosted: 0, totalActiveJobs: 0, totalApplicants: 0, recentJobs: [] };

  constructor(private authService: AuthenticationService, private jobsService: JobsService, private router: Router) {}

  ngOnInit(): void {
    this.authService.getLoggedInUser().subscribe(userDetails => this.userDetails = userDetails);
    if (!this.userDetails) { this.router.navigate(['/login']); return; }
    this.jobsService.getRecruiterDashboard(this.userDetails.id).subscribe(stats => this.stats = stats);
  }
}
