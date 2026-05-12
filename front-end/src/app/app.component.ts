import { Component, OnInit } from '@angular/core';
import { AuthenticationService} from './authentication.service';
import {UserDetails} from './model/user-details';
import {Router} from '@angular/router';
import { ToastService } from './shared/toast/toast.service';


@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {
  title = 'Linked-in';
  userDetails: UserDetails;
  redirectUrl: string;

  constructor(private authService: AuthenticationService, private router: Router, private toast: ToastService) {

  }


  ngOnInit(): void {
    window.alert = (message?: any) => this.toast.error(String(message || 'Something went wrong'));


    this.authService.getLoggedInUser().subscribe((userDetails) => {
    this.userDetails = userDetails;
    });

  }

  hasRole(rolename: string): boolean{
    let flag = false;
    if(this.userDetails) {
      this.userDetails.roles.forEach((role) => {
        if (role === rolename)
          flag = true;
      });
    }
    return flag;
  }

  makeRedirectUrl():string{
    let redirectUrl: string = null;
    if(this.hasRole('PROFESSIONAL'))
      redirectUrl = '/in';
    else if(this.hasRole('ADMIN'))
      redirectUrl = '/admin';
    else
      redirectUrl = '/homepage';


    return redirectUrl;
  }


  logout(){
    this.authService.logout();
  }

  // goToProfile(){
  //   this.router.navigate(['/users/' + this.userDetails.id.toString()]).then(() => {
  //     location.reload();
  //   });
  // }

}
