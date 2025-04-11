/*
 * Copyright (c) 2024 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

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
    let name = ((user.firstName || '(first name not set)') +
      ' ' + (user.lastName || '(last name not set)')).trim() || '-';
    if (user.partner?.abbreviation) {
      name += '(' + user.partner.abbreviation + ')';
    }
    return name;
  }

}
