
import { Component, OnInit } from '@angular/core';
import { SensorDataService } from '../sensor-data.service';
import { ServiceEndpoint } from '../ServiceEndpoint';
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {TokenResponse} from "../TokenResponse";
import {GraphSensorEndpoint} from "../GraphSensorEndpoint";

@Component({
  selector: 'app-sensor-graph',
  templateUrl: './sensor-graph.component.html',
  styleUrls: ['./sensor-graph.component.css']
})
export class SensorGraphComponent implements OnInit {
  graphData: { time: string; value: number }[] = [];
  loading: boolean = false;
  error: string = '';

  constructor(private sensorDataService: SensorDataService, private http: HttpClient ) { }

  ngOnInit(): void {

    localStorage.removeItem('authToken');
    localStorage.removeItem('refreshToken');

    this.getAuthToken();
    this.loadSensorData('', '', '');
  }

  loadSensorData(userId: string, sensorId: string, pageNumber: string): void {

    console.log("loadSensorData userId " + userId + " sensorId " + sensorId + " pageNumber " + pageNumber);

    this.loading = true;
    this.error = '';

    if(userId === '' || sensorId === '' || pageNumber === '') {
      this.loading = false;
      return;
    }

    // Get token from your auth service or local storage
    const token = this.getAuthToken();

    if(token === '') {
      console.error('No token found');
    } else {
      this.sensorDataService.getSensorDataByUserIdAndSensorId(token, userId, sensorId).subscribe({
        next: (data: GraphSensorEndpoint[]) => {
          this.graphData = data.map(item => ({
            time: item.parsedDateTime || '',
            value: item.value || 0
          }));
          this.loading = false;
        },
        error: (err) => {
          this.error = 'Failed to load sensor data';
          console.error('Error loading sensor data:', err);
          this.loading = false;
        }
      });
    }

  }

  private getAuthToken(): string {

    if(localStorage.getItem('authToken') === null) {
      const headers = new HttpHeaders({
        'Content-Type': 'application/json'
      });

      const body = {
        username:  'admin',
        password: '1234'
      }

      this.http.post<TokenResponse>("http://localhost:8090/sensormanager/api/auth/token", body, { headers }).subscribe({
        next: (data: TokenResponse) => {
          localStorage.setItem('authToken', data.access_token);
          localStorage.setItem('refreshToken', data.refresh_token);
          console.log("token successfully requested");
        },
        error: (err) => {
          this.error = 'Failed to load token';
          console.error('Error loading sensor data:', err);
          this.loading = false;
        }
      });
    } else {
      console.log("token already exists");
    }

    return localStorage.getItem('authToken') || '';
  }

  refreshData(userId: string, sensorId: string, pageNumber: string): void {
    this.loadSensorData(userId, sensorId, pageNumber);
  }
}
