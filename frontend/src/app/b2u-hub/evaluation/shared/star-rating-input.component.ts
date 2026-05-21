import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'app-star-rating-input',
  standalone: true,
  template: `
    <div class="star-row" role="group" [attr.aria-label]="label">
      @for (n of stars; track n) {
        <button
          type="button"
          class="star-btn"
          [class.active]="n <= value"
          (click)="pick(n)"
          [attr.aria-pressed]="n <= value"
        >
          ★
        </button>
      }
      @if (hint) {
        <span class="star-hint">{{ hint }}</span>
      }
    </div>
  `,
  styles: [
    `
      .star-row {
        display: flex;
        align-items: center;
        flex-wrap: wrap;
        gap: 0.25rem;
      }
      .star-btn {
        border: none;
        background: transparent;
        font-size: 1.75rem;
        line-height: 1;
        color: #ccc;
        cursor: pointer;
        padding: 0 0.1rem;
      }
      .star-btn.active {
        color: #f5a623;
      }
      .star-btn:focus-visible {
        outline: 2px solid #1a2237;
        border-radius: 4px;
      }
      .star-hint {
        font-size: 0.85rem;
        color: #666;
        margin-left: 0.5rem;
      }
    `,
  ],
})
export class StarRatingInputComponent {
  @Input({ required: true }) label = '';
  @Input() hint = '';
  @Input() value = 0;
  @Output() valueChange = new EventEmitter<number>();

  readonly stars = [1, 2, 3, 4, 5];

  pick(n: number) {
    this.valueChange.emit(n);
  }
}
