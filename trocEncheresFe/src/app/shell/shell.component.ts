import { Component, OnInit } from '@angular/core';
import { ShellService } from '@app/shell/services/shell.service';
import { UntilDestroy } from '@ngneat/until-destroy';
import { Router, RouterModule } from '@angular/router';
import { HeaderComponent } from './components/header/header.component';
import { SidebarComponent } from './components/sidebar/sidebar.component';

@UntilDestroy()
@Component({
  selector: 'app-shell',
  standalone: true,
  imports: [
    RouterModule,
    SidebarComponent,
    HeaderComponent,
  ],
  templateUrl: './shell.component.html',
  styleUrls: ['./shell.component.scss']
})
export class ShellComponent implements OnInit {
  isSidebarActive = false;

  constructor(
    private readonly _shellService: ShellService,
    private readonly _router: Router,
  ) {}

  ngOnInit() {
    // this._socketService.connect();
  }

  sidebarToggle(toggleState: boolean) {
    this.isSidebarActive = toggleState;
  }

  private _reloadCurrentRoute(path?: string) {
    const currentUrl = path || this._router.url;
    this._router.navigateByUrl('/', { skipLocationChange: true }).then(() => {
      this._router.navigate([currentUrl]);
    });
  }
}
