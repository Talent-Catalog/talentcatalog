import { Injectable } from '@angular/core';
import { Candidate } from '../model/candidate';
import { Observable } from 'rxjs/index';
import { of } from 'rxjs';
import { delay } from 'rxjs/internal/operators';

@Injectable({providedIn: 'root'})
export class CandidateService {

  private candidates: Candidate[];

  constructor() {
    this.candidates = [];

    let nextId = 0;

    this.candidates.push({
      id: nextId++,
      candidateNumber: 'CN0001',
      firstName: 'Test1',
      lastName: 'Person1'
    });

    this.candidates.push({
      id: nextId++,
      candidateNumber: 'CN0002',
      firstName: 'Test2',
      lastName: 'Person2'
    });

    this.candidates.push({
      id: nextId++,
      candidateNumber: 'CN0003',
      firstName: 'Test3',
      lastName: 'Person3'
    });
  }

  search(): Observable<Candidate[]> {
    return of(this.candidates).pipe(delay(1000));
  }
}
