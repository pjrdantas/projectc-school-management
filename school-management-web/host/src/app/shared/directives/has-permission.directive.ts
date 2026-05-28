import {
  Directive,
  Input,
  OnDestroy,
  TemplateRef,
  ViewContainerRef,
  inject,
} from '@angular/core';
import { AuthStateService } from '../../core/auth/auth-state.service';
import { hasPermission } from '../../core/auth/permission.util';
import { Subscription } from 'rxjs';

@Directive({
  selector: '[appHasPermission]',
  standalone: true,
})
export class HasPermissionDirective implements OnDestroy {
  private authState = inject(AuthStateService);
  private templateRef = inject(TemplateRef<unknown>);
  private vcr = inject(ViewContainerRef);
  private sub?: Subscription;

  private currentPermission?: string;

  @Input() set appHasPermission(permission: string) {
    this.currentPermission = permission;
    this.trackPermissionChanges();
  }

  private trackPermissionChanges() {
    this.sub?.unsubscribe();
    this.sub = this.authState.usuario$.subscribe(() => {
      this.updateView();
    });
  }

  private updateView() {
    const userPermissions = this.authState.getPermissions();

    const canRender = this.currentPermission
      ? hasPermission(userPermissions, this.currentPermission)
      : false;

    if (canRender) {
      if (this.vcr.length === 0) {
        this.vcr.createEmbeddedView(this.templateRef);
      }
    } else {
      this.vcr.clear();
    }
  }

  ngOnDestroy() {
    this.sub?.unsubscribe();
  }
}
