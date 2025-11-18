import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { CommonModule } from '@angular/common';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { LineGraphComponent } from './line-graph/line-graph.component';
import { SensorGraphComponent } from './sensor-graph/sensor-graph.component';
import { SensorDataService } from './sensor-data.service';
import { SensorListComponent } from './sensor-list/sensor-list.component';
import {FormsModule} from "@angular/forms";
import { LoginComponent } from './login/login.component';
import { AuthService } from './auth.service';
import { AuthGuard } from './auth.guard';

@NgModule({
  declarations: [
    AppComponent,
    LoginComponent,
    LineGraphComponent,
    SensorGraphComponent,
    SensorListComponent
  ],
  imports: [
    BrowserModule,
    CommonModule,
    AppRoutingModule,
    FormsModule
  ],
  providers: [SensorDataService, AuthService, AuthGuard,
    provideHttpClient(withInterceptorsFromDi())],
  bootstrap: [AppComponent]
})
export class AppModule { }
