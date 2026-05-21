import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-evaluation-shell',
  standalone: true,
  imports: [RouterOutlet],
  template: `
    <section class="section-top">
      <div class="container">
        <div class="col-lg-10 offset-lg-1 text-center">
          <div class="section-top-title wow fadeInRight">
            <h1>B2U-HUB — Évaluation &amp; feedback</h1>
            <p class="text-muted mb-0">
              Module Évaluation &amp; feedback — entreprises &amp; freelances B2U-HUB
            </p>
          </div>
        </div>
      </div>
    </section>
    <div class="container section-padding pb-5">
      <router-outlet />
    </div>
  `,
})
export class EvaluationShellComponent {}
