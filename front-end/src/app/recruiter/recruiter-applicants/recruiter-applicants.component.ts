import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthenticationService } from '../../authentication.service';
import { JobsService } from '../../services/jobs.service';
import { User } from '../../model/user';
import { UserDetails } from '../../model/user-details';

@Component({ selector: 'app-recruiter-applicants', templateUrl: './recruiter-applicants.component.html', styleUrls: ['./recruiter-applicants.component.css'] })
export class RecruiterApplicantsComponent implements OnInit {
  userDetails: UserDetails;
  jobId: number;
  applicants: User[] = [];

  constructor(private authService: AuthenticationService, private jobsService: JobsService, private route: ActivatedRoute, private router: Router) {}

  ngOnInit(): void {
    this.authService.getLoggedInUser().subscribe(userDetails => this.userDetails = userDetails);
    if (!this.userDetails) { this.router.navigate(['/login']); return; }
    this.jobId = Number(this.route.snapshot.paramMap.get('jobId'));
    this.jobsService.getApplicants(this.userDetails.id, this.jobId).subscribe(applicants => this.applicants = applicants);
  }
}
