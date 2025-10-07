import { Component } from '@angular/core';
import {ServiceEndpoint} from "./ServiceEndpoint";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  public sensorValues: ServiceEndpoint[] = [];

  sensorValue: ServiceEndpoint = {};

  private webSocket : WebSocket;

  constructor() {
    this.webSocket = new WebSocket('ws://localhost:8081/sensormanager/sensor-gui');
    this.webSocket.onmessage = (event) => {
      // console.log("event.data " + event.data)
      this.sensorValue = JSON.parse(event.data);
      this.sensorValues.push(this.sensorValue);
      // console.log("sensorValues size " + this.sensorValues.length)
    }
  }
}
