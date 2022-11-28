import { DefaultService } from '@anona/berloga-api-client';
import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, CanActivateChild, Router, RouterStateSnapshot, UrlTree } from '@angular/router';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AuthGuardService implements CanActivate {

  constructor(public router: Router, public service: DefaultService) {}

  async canActivate(): Promise<boolean> {
    let youShallNotPass = true;
    try {
      youShallNotPass = (await this.service.userControllerAmILoggedGET().toPromise()).status!=='logged';
    } catch(e) {
      console.log('Backend request fail, maybe not running?');
    }
    if(youShallNotPass) {
      this.router.navigate(['login']);
      return false;
    } else {
      return true;
    }
  }

}
