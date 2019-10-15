import {Pipe, PipeTransform} from '@angular/core';
import {Candidate} from "../model/candidate";

@Pipe({
  name: 'candidate'
})
export class CandidatePipe implements PipeTransform {

  transform(candidate: Candidate, args?: any): any {
    const u = candidate.user;
    switch (args) {
      case 'name':
        return u ? u.firstName + ' ' + u.lastName : 'Candidate ' + candidate.candidateNumber;

      default:
        return u ? u.firstName + ' ' + u.lastName : 'Candidate ' + candidate.candidateNumber;
    }
  }

}
