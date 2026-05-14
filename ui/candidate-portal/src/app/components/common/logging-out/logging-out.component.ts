import {Component, OnDestroy, OnInit} from '@angular/core';
import {NgIf} from "@angular/common";
import {Subject, timer} from 'rxjs';
import {map, takeUntil, takeWhile, tap} from 'rxjs/operators';
import {AuthenticationService} from "../../../services/authentication.service";
import {ActivatedRoute, Router} from "@angular/router";

@Component({
  selector: 'app-logging-out',
  standalone: true,
  imports: [
    NgIf
  ],
  templateUrl: './logging-out.component.html',
  styleUrl: './logging-out.component.scss'
})
export class LoggingOutComponent implements OnInit, OnDestroy {
  reason: string;
  secondsRemaining = 10;

  private readonly destroy$ = new Subject<void>();

  constructor(private authenticationService: AuthenticationService, private route: ActivatedRoute,
              private router: Router)
  {}

  ngOnInit() {
    this.reason = this.route.snapshot.queryParamMap.get('reason');

    const countdownSeconds = 180;
    //Automatically logout after countdown seconds
    timer(0, 1000).pipe(
      //Convert elapsed seconds to remaining seconds
      map(elapsedSeconds => countdownSeconds - elapsedSeconds),
      tap(remainingSeconds => this.secondsRemaining = remainingSeconds),
      takeWhile(remainingSeconds => remainingSeconds >= 0),
      takeUntil(this.destroy$)
    ).subscribe(remainingSeconds => {
      if (remainingSeconds <= 0) {
        this.logout();
      }
    });
  }

  logout() {
    //Stop the countdown timer
    this.destroy$.next();

    //Force a logout
    this.authenticationService.logout();

    this.router.navigate(['']);
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
