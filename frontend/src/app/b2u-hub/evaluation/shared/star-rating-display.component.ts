import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-star-rating-display',
  standalone: true,
  template: `
    <span class="stars" [attr.aria-label]="label + ' : ' + value + ' sur 5'">
      @for (n of stars; track n) {
        <span class="star" [class.filled]="n <= value">★</span>
      }
      @if (showValue) {
        <span class="value">{{ value }}/5</span>
      }
    </span>
  `,
  styles: [
    `
      .stars {
        display: inline-flex;
        align-items: center;
        gap: 0.05rem;
      }
      .star {
        color: #ddd;
        font-size: 1.1rem;
      }
      .star.filled {
        color: #f5a623;
      }
      .value {
        font-size: 0.85rem;
        color: #555;
        margin-left: 0.35rem;
        font-weight: 600;
      }
    `,
  ],
})
export class StarRatingDisplayComponent {
  @Input({ required: true }) value = 0;
  @Input() label = 'Note';
  @Input() showValue = true;
  readonly stars = [1, 2, 3, 4, 5];
}
