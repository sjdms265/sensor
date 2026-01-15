import {Component, OnInit} from '@angular/core';
import {ServiceEndpoint} from "./dto/ServiceEndpoint";
import { AuthService } from './auth.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {
  public sensorValues: ServiceEndpoint[] = [];
  sensorValue: ServiceEndpoint = {};
  private webSocket : WebSocket | undefined;
  error: string = '';
  selectedSensorId: string  = '';

  constructor(protected authService: AuthService, private router: Router) {
  }

  ngOnInit(): void {

    // Check if user is authenticated
    if (!this.authService.isAuthenticated()) {
      this.router.navigate(['/login']);
    } else {
      this.initializeWebSocket();
    }
  }

  private initializeWebSocket(): void {
    this.webSocket = new WebSocket('ws://localhost:8081/sensormanager/sensor-gui');
    this.webSocket.onmessage = (event) => {
      this.sensorValue = JSON.parse(event.data);
      this.sensorValues.push(this.sensorValue);
    };

    this.webSocket.onerror = (error) => {
      console.error('WebSocket error:', error);
      this.error = 'WebSocket connection failed';
    };

    this.webSocket.onclose = () => {
      console.log('WebSocket connection closed');
    };
  }

  // In your parent component TypeScript
  onSensorSelected(sensorId: string): void {
    console.log('Selected sensor:', sensorId);
    this.selectedSensorId = sensorId;
    // Do something with the selected sensor ID
  }

  logout(): void {
    this.authService.logout();
    if (this.webSocket) {
      this.webSocket.close();
    }
    this.router.navigate(['/login']);
  }
}
