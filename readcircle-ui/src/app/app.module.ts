import { APP_INITIALIZER, NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { HTTP_INTERCEPTORS, HttpClient, HttpClientModule } from '@angular/common/http';
import { LoginComponent } from './pages/login/login.component';
import { FormsModule } from '@angular/forms';
import { RegisterComponent } from './pages/register/register.component';
import { ActivateAccountComponent } from './pages/activate-account/activate-account.component';
import { CodeInputModule } from 'angular-code-input';
import { HttpTokenInterceptor } from './services/interceptor/http-token.interceptor';
import { ApiModule } from './services/api.module';
import { KeycloakService } from './services/keycloak/keycloak.service';
import { ToastrModule } from 'ngx-toastr';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
export function kcFactory(kcService: KeycloakService) {
  return () => kcService.init();
}

@NgModule({
  declarations: [
    AppComponent,
    LoginComponent,
    RegisterComponent,
    ActivateAccountComponent,
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    HttpClientModule,
    FormsModule,
    CodeInputModule,
    ToastrModule,
     BrowserAnimationsModule,
        ToastrModule.forRoot({
      progressBar: true,
      closeButton: true,
      newestOnTop: true,
      tapToDismiss: true,
      positionClass: 'toast-bottom-right',
      timeOut: 8000
    }),
    ApiModule.forRoot({rootUrl: 'http://144.24.124.183:8088/api/v1'})

    // ApiModule.forRoot({rootUrl: 'http://localhost:8088/api/v1'})


  ],
  providers: [
    HttpClient,
    {
      provide: HTTP_INTERCEPTORS,
      useClass: HttpTokenInterceptor,
      multi: true
    },
    {
      provide: APP_INITIALIZER,
      deps: [KeycloakService],
      useFactory: kcFactory,
      multi: true
    }

  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
