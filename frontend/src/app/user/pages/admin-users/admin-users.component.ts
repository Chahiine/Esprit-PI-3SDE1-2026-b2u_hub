import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService } from '../../services/admin.service';

@Component({
  selector: 'app-admin-users',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-users.component.html',
})
export class AdminUsersComponent implements OnInit {
  users: any[] = []; stats: any = {}; filter = 'ALL'; search = ''; loading = false; msg = '';

  constructor(private admin: AdminService) {}

  ngOnInit(): void { this.loadStats(); this.loadUsers(); }

  loadStats(): void { this.admin.getStats().subscribe(s => this.stats = s); }

  loadUsers(): void {
    this.loading = true;
    const obs = this.filter === 'ALL' ? this.admin.getAllUsers() : this.admin.getByRole(this.filter);
    obs.subscribe({ next: (d) => { this.users = d; this.loading = false; }, error: () => { this.loading = false; } });
  }

  get filtered(): any[] {
    if (!this.search.trim()) return this.users;
    const q = this.search.toLowerCase();
    return this.users.filter(u => u.firstName?.toLowerCase().includes(q) || u.lastName?.toLowerCase().includes(q) || u.email?.toLowerCase().includes(q));
  }

  ban(u: any): void {
    if (!confirm(`Bannir ${u.firstName} ${u.lastName} ?`)) return;
    this.admin.banUser(u.id).subscribe({ next: (r) => { Object.assign(u, r); this.msg = 'Utilisateur banni.'; } });
  }

  unban(u: any): void {
    this.admin.unbanUser(u.id).subscribe({ next: (r) => { Object.assign(u, r); this.msg = 'Utilisateur réactivé.'; } });
  }

  delete(u: any): void {
    if (!confirm(`Supprimer ${u.firstName} ${u.lastName} ?`)) return;
    this.admin.deleteUser(u.id).subscribe({ next: () => { this.users = this.users.filter(x => x.id !== u.id); this.msg = 'Supprimé.'; } });
  }
}
