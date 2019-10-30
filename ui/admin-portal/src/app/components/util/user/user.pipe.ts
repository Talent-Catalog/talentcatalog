import {Pipe, PipeTransform} from '@angular/core';
import {User} from "../../../model/user";

@Pipe({
  name: 'user'
})
export class UserPipe implements PipeTransform {

  transform(user: User, args?: any): any {
    if (user){
      switch(args) {
        case 'fullName':
          return this.fullName(user);

         default:
          console.log('[User Pipe] Unrecognised argument', args);
          return this.fullName(user);
      }
    }
    return null;

  }

  private fullName(user: User) {
    return ((user.firstName || '') + ' ' + (user.lastName || '')).trim();
  }

}
