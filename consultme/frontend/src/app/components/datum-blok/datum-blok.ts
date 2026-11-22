import { DatePipe } from '@angular/common';
import { Component, input } from '@angular/core';

// Datum termina kao "list kalendara": dan u nedelji, veliki broj dana i mesec.
// Koristi se na karticama termina i rezervacija (student i profesor).
// Upotreba: <app-datum-blok [datum]="termin.pocetak" />
@Component({
  selector: 'app-datum-blok',
  imports: [DatePipe],
  template: `
    <div class="blok" [class.ugaseno]="ugaseno()">
      <span class="dan-u-nedelji">{{ datum() | date: 'EEE' }}</span>
      <span class="broj">{{ datum() | date: 'd' }}</span>
      <span class="mesec">{{ datum() | date: 'MMM' }}</span>
    </div>
  `,
  styles: `
    .blok {
      width: 4.25rem;
      flex-shrink: 0;
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 0.45rem 0;
      border: 1px solid var(--ivica);
      border-top: 4px solid var(--akcenat);
      border-radius: 6px;
      background: var(--pozadina);
      line-height: 1.1;
    }
    .dan-u-nedelji, .mesec {
      font-size: 0.78rem;
      color: var(--tekst-blago);
    }
    .broj {
      font-size: 1.7rem;
      font-weight: 700;
      color: var(--tinta);
    }
    /* otkazan ili prošao termin */
    .ugaseno {
      border-top-color: var(--ivica);
      opacity: 0.7;
    }
  `,
})
export class DatumBlok {

  // input() -> vrednost koju komponenta dobija od roditelja ([datum]="...")
  datum = input.required<string>();
  ugaseno = input(false);

}
