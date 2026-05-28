/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

import {Component, OnInit} from '@angular/core';
import {UntypedFormBuilder, UntypedFormGroup} from "@angular/forms";
import {AdminService} from "../../../services/admin.service";

interface AdminCommand {
  command: string;
  description: string;
  method: 'GET' | 'POST';
}

@Component({
  selector: 'app-admin-api',
  templateUrl: './admin-api.component.html',
  styleUrls: ['./admin-api.component.scss']
})
export class AdminApiComponent implements OnInit {

  ack: string;
  error: any;
  form: UntypedFormGroup;

  // List of batch migration commands
  readonly migrationCommands: AdminCommand[] = [
    { command: 'run_api_migration', description: 'Run full anonymization job (Aurora + Mongo)', method: 'POST' },
    { command: 'run_api_migration/aurora', description: 'Run Aurora-only anonymization job', method: 'POST' },
    { command: 'run_api_migration/mongo', description: 'Run Mongo-only anonymization job', method: 'POST' },
    { command: 'run_api_migration/list/{listId}', description: 'Run anonymization using a specific listId', method: 'POST' },
    { command: 'list_api_migrations', description: 'List recent job executions', method: 'GET' },
    { command: 'stop_api_migration/{executionId}', description: 'Stop a running job by executionId', method: 'POST' },
    { command: 'restart_api_migration/{executionId}', description: 'Restart a failed job by executionId', method: 'POST' }
  ];

  // List of general admin commands
  readonly adminCommands: AdminCommand[] = [
    { command: 'set_candidate_text/cpu-{percentage}', description: 'Update text of all candidates that are not deleted or withdrawn status limiting cpu percentage', method: 'POST' },
    { command: 'set_candidate_text/list-{listId}-cpu-{percentage}', description: 'Update text of all candidates in the specified list limiting cpu percentage', method: 'POST' },
    { command: 'set_candidate_text/search-{searchId}-cpu-{percentage}', description: 'Update text of all candidates in the specified search limiting cpu percentage', method: 'POST' },
    { command: 'reassign-candidates/list-{listId}-to-partner-{partnerId}', description: 'Reassign all candidates in the specified list to the specified partner', method: 'POST' },
    { command: 'reassign-candidates/search-{searchId}-to-partner-{partnerId}', description: 'Reassign all candidates in the specified search to the specified partner', method: 'POST' },
    { command: 'move-candidate-drive/{number}', description: 'Move candidate to the current candidate data drive', method: 'POST' },
    { command: 'move-candidates-drive/{listId}', description: 'Move candidates from the given list to the current candidate data drive', method: 'POST' },
    { command: 'flush_user_cache', description: 'Flush Redis cache for cached users', method: 'POST' }
    // Add more general-purpose or maintenance commands here
  ];

  readonly duolingoCommands: AdminCommand[] = [
    { command: 'duolingo/dashboard-results', description: 'Fetch Duolingo dashboard results', method: 'GET' },
    { command: 'duolingo/verify-score?certificateId={certificateId}&birthdate={yyyy-MM-dd}', description: 'Verify Duolingo score by certificateId and birthdate', method: 'POST' },
    { command: 'reassign-duolingo-coupon/{candidateNumber}', description: 'Reassign a new Duolingo coupon to a candidate', method: 'POST' },
  ];

  readonly linkedinCommands: AdminCommand[] = [
    {
      command: '{candidateNumber}/reassign-linkedin-coupon',
      description: 'Cancel previous assignment and make new one for given candidate',
      method: 'POST'
    }
  ];

  constructor(
    private fb: UntypedFormBuilder,
    private adminService: AdminService
  ) { }

  ngOnInit(): void {
    this.form = this.fb.group({
      apicall: [],
      method: ['POST'],
    });
  }

  send() {
    if (this.form.value.apicall) {
      this.ack = null;
      this.error = null;
      this.adminService.call(this.form.value.apicall, this.form.value.method).subscribe(
        (response: string) => {
          if (response && response.trim().length > 0) {
            this.ack = this.tryFormatJson(response) ?? response;
          } else {
            this.ack = 'Done';
          }
        },
        (error) => {this.error = error}
      )
    }
  }

  private tryFormatJson(s: string): string | null {
    try {
      return JSON.stringify(JSON.parse(s), null, 2);
    } catch {
      return null;
    }
  }

  fillCommand(cmd: AdminCommand, event: MouseEvent): void {
    event.preventDefault();  // prevent anchor navigation
    this.form.patchValue({ apicall: cmd.command });
    this.form.patchValue({ method: cmd.method });
  }
}
