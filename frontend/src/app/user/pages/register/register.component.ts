import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { UserRole } from '../../models/user.model';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './register.component.html',
})
export class RegisterComponent {
  firstName = ''; lastName = ''; email = ''; password = '';
  role: UserRole = 'STUDENT'; loading = false; error = '';

  constructor(private auth: AuthService, private router: Router) {}

  submit(): void {
    if (!this.firstName || !this.lastName || !this.email || !this.password) {
      this.error = 'Tous les champs sont obligatoires.'; return;
    }
    if (this.password.length < 6) { this.error = 'Mot de passe : minimum 6 caractères.'; return; }
    this.loading = true; this.error = '';
    this.auth.register({ firstName: this.firstName, lastName: this.lastName,
      email: this.email, password: this.password, role: this.role }).subscribe({
      next: () => { this.loading = false; this.router.navigate(['/profile']); },
      error: (err) => { this.loading = false; this.error = typeof err.error === 'string' ? err.error : 'Inscription échouée.'; }
    });
  }
}
