import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LanguageSelectorComponent } from '@app/i18n';
import { UntilDestroy } from '@ngneat/until-destroy';

@UntilDestroy()
@Component({
  selector: 'app-header',
  standalone: true,
  imports: [ LanguageSelectorComponent, CommonModule ],
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss']
})
export class HeaderComponent {
  menuHidden = true;
}
