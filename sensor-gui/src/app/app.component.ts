import {Component, OnInit} from '@angular/core';
import {ServiceEndpoint} from "./ServiceEndpoint";
import { AuthService } from './auth.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {
  public sensorValues: ServiceEndpoint[] = [];

  sensorValue: ServiceEndpoint = {};

  private webSocket : WebSocket;

  error: string = '';

  selectedSensorId: string = 'sensor.10000db501_t';

  constructor(private authService: AuthService) {
    this.webSocket = new WebSocket('ws://localhost:8081/sensormanager/sensor-gui');
    this.webSocket.onmessage = (event) => {
      // console.log("event.data " + event.data)
      this.sensorValue = JSON.parse(event.data);
      this.sensorValues.push(this.sensorValue);
      // console.log("sensorValues size " + this.sensorValues.length)
    }
  }

  ngOnInit(): void {

    this.authService.clearTokens()

    // Request token if not available
    if (!this.authService.isAuthenticated()) {
      this.authService.requestToken().subscribe({
        next: () => console.log('Authentication successful'),
        error: (err) => {
          this.error = 'Failed to authenticate';
          console.error('Authentication error:', err);
        }
      });
    } else {
      console.log('Already authenticated');
    }

  }

  // In your parent component TypeScript
  onSensorSelected(sensorId: string): void {
    console.log('Selected sensor:', sensorId);
    this.selectedSensorId = sensorId;
    // Do something with the selected sensor ID
  }
}
