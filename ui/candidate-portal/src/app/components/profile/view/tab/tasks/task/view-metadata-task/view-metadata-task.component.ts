import {Component, Input, OnInit} from '@angular/core';
import {UntypedFormGroup, UntypedFormBuilder, Validators} from '@angular/forms';
import {TaskAssignment} from "../../../../../../../model/task-assignment";
import {MetadataField, MetadataOption} from "../../../../../../../model/task";
import {CountryService} from "../../../../../../../services/country.service";
import {Candidate} from "../../../../../../../model/candidate";

@Component({
  selector: 'app-view-metadata-task',
  templateUrl: './view-metadata-task.component.html',
  styleUrls: ['./view-metadata-task.component.scss']
})
export class ViewMetadataTaskComponent implements OnInit {
  @Input() form!: UntypedFormGroup;
  @Input() selectedTask!: TaskAssignment;
  @Input() candidate!: Candidate;
  metadataFields: MetadataField[] = [];
  @Input() discrepantFields: Set<string> = new Set();

  constructor(private fb: UntypedFormBuilder, private countryService: CountryService) {}

  ngOnInit(): void {
    if (!this.selectedTask?.task?.requiredMetadata) return;

    this.metadataFields = this.selectedTask.task.requiredMetadata as MetadataField[];

    this.metadataFields.forEach(field => {
      if (field.type === 'select') {
        field.options = this.getOptionsForField(field);
      }
    });

    this.addMetadataControls();
  }

  private getOptionsForField(field: MetadataField): MetadataOption[] {
    if (field.name === 'gender') {
      return [
        { value: 'male', label: 'GENDER.MALE' },
        { value: 'female', label: 'GENDER.FEMALE' },
        { value: 'other', label: 'GENDER.OTHER' }
      ];
    }

    if (field.name === 'birthCountry') {
      this.countryService.listCountries().subscribe(countries => {
        field.options = countries.map(c => ({ value: c.id.toString(), label: c.name }));
      });
      return [];
    }

    if (field.options && typeof field.options[0] === 'string') {
      return (field.options as string[]).map(opt => ({ value: opt, label: opt }));
    }

    return (field.options as MetadataOption[]) || [];
  }

  private addMetadataControls(): void {
    this.metadataFields.forEach(field => {
      const value = this.normalizeValue(field, this.getFieldValue(field.name));
      this.form.addControl(field.name, this.fb.control(value, [Validators.required]));
    });
  }

  /** Pull raw values from candidate/task */
  getFieldValue(fieldName: string): any {
    if (!this.candidate) return this.getPropertyValue(fieldName);

    const candidateMap: Record<string, any> = {
      firstName: this.candidate.user?.firstName,
      lastName: this.candidate.user?.lastName,
      dob: this.candidate.dob ? new Date(this.candidate.dob) : null,
      gender: this.candidate.gender,
      birthCountry: this.candidate.birthCountry || null
    };

    return candidateMap[fieldName] ?? this.getPropertyValue(fieldName);
  }

  private getPropertyValue(fieldName: string): string | null {
    const prop = this.selectedTask.candidateProperties?.find(p => {
      return p.name.endsWith(`_${fieldName}`);
    });

    return prop?.value?.toString() || null;
  }

  /** Normalize any raw value for form binding */
  private normalizeValue(field: MetadataField, value: any): any {
    if (!value) return '';

    if (field.type === 'date') {
      // Accept Date or string → always output YYYY-MM-DD
      const d = value instanceof Date ? value : new Date(value);
      return !isNaN(d.getTime()) ? d.toISOString().split('T')[0] : '';
    }

    if (field.type === 'select') {
      // If it's an object with id → return id as string
      if (typeof value === 'object' && value.id) return value.id.toString();
      return value.toString();
    }

    return value;
  }

  getDisplayValue(field: MetadataField): string {
    const rawValue = this.getFieldValue(field.name);
    const normalized = this.normalizeValue(field, rawValue);

    if (normalized && field.options?.length) {
      const opt = (field.options as MetadataOption[]).find(o => o.value === normalized);
      return opt ? opt.label : normalized;
    }

    return normalized || 'N/A';
  }
}
