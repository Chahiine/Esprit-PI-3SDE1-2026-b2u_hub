import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { AuthResponse } from '../../models/user.model';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './profile.component.html',
})
export class ProfileComponent implements OnInit {

  user = signal<AuthResponse | null>(null);
  editing      = false;
  firstName    = ''; lastName = ''; bio = ''; skills = '';
  showPassword = false;
  currentPassword = ''; newPassword = ''; confirmPassword = '';
  imageUploading = false; cvUploading = false;
  successMsg = ''; errorMsg = '';

  constructor(private auth: AuthService, private router: Router) {}

  ngOnInit(): void {
    console.log('USER FROM AUTH', this.auth.user());
    this.user.set(this.auth.user());
    this.resetForm();
  }

  resetForm(): void {
    const u = this.user();
    if (!u) return;
    this.firstName = u.firstName ?? '';
    this.lastName  = u.lastName  ?? '';
    this.bio       = u.bio       ?? '';
    this.skills    = u.skills    ?? '';
  }

  startEdit():  void { this.editing = true;  this.resetForm(); }
  cancelEdit(): void { this.editing = false; this.resetForm(); }

  saveProfile(): void {
    this.clearMsg();
    this.auth.updateProfile({
      firstName: this.firstName,
      lastName:  this.lastName,
      bio:       this.bio,
      skills:    this.skills
    }).subscribe({
      next: (res) => {
        this.user.set(res);
        this.editing = false;
        this.successMsg = 'Profil mis à jour !';
        setTimeout(() => this.successMsg = '', 3000);
      },
      error: (err) => { this.errorMsg = err.error ?? 'Erreur mise à jour'; }
    });
  }

  savePassword(): void {
    this.clearMsg();
    if (this.newPassword !== this.confirmPassword) {
      this.errorMsg = 'Les mots de passe ne correspondent pas';
      return;
    }
    this.auth.changePassword({
      currentPassword: this.currentPassword,
      newPassword:     this.newPassword
    }).subscribe({
      next: () => {
        this.successMsg = 'Mot de passe changé !';
        this.showPassword = false;
        this.currentPassword = this.newPassword = this.confirmPassword = '';
        setTimeout(() => this.successMsg = '', 3000);
      },
      error: (err) => { this.errorMsg = err.error ?? 'Erreur changement mot de passe'; }
    });
  }

  onImageSelected(event: Event): void {
    const file = (event.target as HTMLInputElement).files?.[0];
    if (!file) return;
    this.imageUploading = true; this.clearMsg();
    this.auth.uploadProfileImage(file).subscribe({
      next: () => {
        this.user.set(this.auth.user());
        this.imageUploading = false;
        this.successMsg = 'Photo mise à jour !';
        setTimeout(() => this.successMsg = '', 3000);
      },
      error: (err) => { this.imageUploading = false; this.errorMsg = err.error ?? 'Upload échoué'; }
    });
  }


  onCvSelected(event: Event): void {
    const file = (event.target as HTMLInputElement).files?.[0];
    if (!file) return;
    this.cvUploading = true; this.clearMsg();
    this.auth.uploadCv(file).subscribe({
      next: () => {
        this.user.set(this.auth.user());
        this.cvUploading = false;
        this.successMsg = 'CV uploadé !';
        setTimeout(() => this.successMsg = '', 3000);
      },
      error: (err) => { this.cvUploading = false; this.errorMsg = err.error ?? 'Upload CV échoué'; }
    });
  }

  logout(): void { this.auth.logout(); this.router.navigate(['/login']); }

  getInitials(): string {
    const u = this.user();
    if (!u) return '?';
    const first = u.firstName?.[0] ?? '';
    const last  = u.lastName?.[0]  ?? '';
    return (first + last).toUpperCase() || '?';
  }

  getRoleClass(): string {
    return 'role-' + (this.user()?.role?.toLowerCase() ?? 'student');
  }

  getRoleLabel(): string {
    const map: Record<string, string> = {
      STUDENT: 'Étudiant',
      COMPANY: 'Entreprise',
      ADMIN:   'Administrateur'
    };
    return map[this.user()?.role ?? ''] ?? (this.user()?.role ?? '');
  }

  get skillsList(): string[] {
    return (this.user()?.skills ?? '').split(',').map(s => s.trim()).filter(Boolean);
  }


  private clearMsg(): void { this.successMsg = ''; this.errorMsg = ''; }
}
