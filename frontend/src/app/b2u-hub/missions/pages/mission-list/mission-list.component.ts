import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { MissionApiService } from '../../mission-api.service';
import type { MissionDto, MissionStatus } from '../../models';
import { MISSION_STATUS_LABELS } from '../../models';

@Component({
  selector: 'app-mission-list',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './mission-list.component.html',
  styleUrl: './mission-list.component.css',
})
export class MissionListComponent implements OnInit {
  private readonly api = inject(MissionApiService);
  private readonly fb = inject(FormBuilder);

  readonly missions = signal<MissionDto[]>([]);
  readonly loading = signal(true);
  readonly sourceApi = signal(false);
  readonly statusLabels = MISSION_STATUS_LABELS;

  readonly filters = this.fb.nonNullable.group({
    keyword: [''],
    location: [''],
    status: ['' as MissionStatus | ''],
    skills: [''],
    companyName: [''],
  });

  ngOnInit() {
    this.rechercher();
  }

  rechercher() {
    this.loading.set(true);
    const v = this.filters.getRawValue();
    this.api
      .listMissions({
        keyword: v.keyword || undefined,
        location: v.location || undefined,
        status: v.status || undefined,
        skills: v.skills || undefined,
        companyName: v.companyName || undefined,
      })
      .subscribe({
        next: (list) => {
          this.missions.set(list);
          this.sourceApi.set(true);
          this.loading.set(false);
        },
        error: () => {
          this.missions.set([]);
          this.sourceApi.set(false);
          this.loading.set(false);
        },
      });
  }

  reinitialiser() {
    this.filters.reset({
      keyword: '',
      location: '',
      status: '',
      skills: '',
      companyName: '',
    });
    this.rechercher();
  }

  statutLabel(status: MissionStatus): string {
    return this.statusLabels[status] ?? status;
  }
}
