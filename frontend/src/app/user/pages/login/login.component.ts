import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './login.component.html',
})
export class LoginComponent {
  email = ''; password = ''; loading = false; error = '';

  constructor(private auth: AuthService, private router: Router) {}

  submit(): void {
    if (!this.email || !this.password) { this.error = 'Veuillez remplir tous les champs.'; return; }
    this.loading = true; this.error = '';
    this.auth.login({ email: this.email, password: this.password }).subscribe({
      next: () => { this.loading = false; this.router.navigate(['/profile']); },
      error: (err) => { this.loading = false; this.error = typeof err.error === 'string' ? err.error : 'Connexion échouée.'; }
    });
  }

  loginWithLinkedIn(): void {
    window.location.href = 'http://localhost:8081/oauth2/authorization/linkedin';
  }
}
