import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthenticationService } from '../../authentication.service';
import { UserService } from '../../services/user.service';
import { User } from '../../model/user';
import { UserDetails } from '../../model/user-details';

@Component({ selector: 'app-recruiter-profile', templateUrl: './recruiter-profile.component.html', styleUrls: ['./recruiter-profile.component.css'] })
export class RecruiterProfileComponent implements OnInit {
  user: User = new User();
  userDetails: UserDetails;
  saved = false;

  constructor(private authService: AuthenticationService, private userService: UserService, private router: Router) {}

  ngOnInit(): void {
    this.authService.getLoggedInUser().subscribe(userDetails => this.userDetails = userDetails);
    if (!this.userDetails) { this.router.navigate(['/login']); return; }
    this.userService.getUser(this.userDetails.id.toString()).subscribe(user => Object.assign(this.user, user));
  }

  save(): void {
    this.user.id = this.userDetails.id;
    this.userService.editUserJob(this.user).subscribe(() => this.saved = true);
  }
}
