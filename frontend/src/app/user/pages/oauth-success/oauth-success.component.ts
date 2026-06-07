import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Params, Router } from '@angular/router';

@Component({
  selector: 'app-oauth-success',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div style="display:flex;align-items:center;justify-content:center;min-height:100vh;
                background:linear-gradient(135deg,#1a1a2e,#0f3460);color:#fff;
                font-family:sans-serif;text-align:center;padding:2rem">
      <div>
        <div style="font-size:3rem">{{ error ? '❌' : '🔄' }}</div>
        <h2 style="margin:1rem 0">{{ error ? 'Connexion échouée' : 'Connexion en cours...' }}</h2>
        <p style="color:#94a3b8">{{ error ?? 'Veuillez patienter' }}</p>
      </div>
    </div>
  `
})
export class OauthSuccessComponent implements OnInit {

  error: string | null = null;

  constructor(private route: ActivatedRoute, private router: Router) {}

  ngOnInit(): void {
    this.route.queryParams.subscribe((params: Params) => {
      const token:  string | undefined = params['token'];
      const userId: string | undefined = params['userId'];
      const err:    string | undefined = params['error'];

      if (err) {
        this.error = `Connexion LinkedIn échouée : ${err}`;
        setTimeout(() => this.router.navigate(['/login']), 3000);
        return;
      }

      if (token) {
        localStorage.setItem('b2u_token', token);
        if (userId) {
          localStorage.setItem('b2u_user', JSON.stringify({ token, userId: Number(userId) }));
        }
        this.router.navigate(['/profile']);
      } else {
        this.error = 'Token non reçu. Redirection...';
        setTimeout(() => this.router.navigate(['/login']), 2000);
      }
    });
  }
}
