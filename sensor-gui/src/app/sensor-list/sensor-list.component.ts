
import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { catchError, map } from 'rxjs/operators';
import { of } from 'rxjs';
import { AuthService } from '../auth.service';

interface SensorData {
  sensorId: string;
}

interface GraphQLResponse {
  data: {
    sensorsByUser: SensorData[];
  };
  errors?: any[];
}

@Component({
  selector: 'app-sensor-list',
  templateUrl: './sensor-list.component.html',
  styleUrls: ['./sensor-list.component.css']
})
export class SensorListComponent implements OnInit {
  @Input() userId: string = '';
  @Output() sensorSelected = new EventEmitter<string>();

  sensors: SensorData[] = [];
  selectedSensorId: string = '';
  loading: boolean = false;
  error: string | null = null;

  private graphqlUrl = 'http://localhost:8090/temperaturesensor/graphql';

  constructor(private http: HttpClient,
              private authService: AuthService) {}

  async ngOnInit(): Promise<void> {
    if (this.userId) {
      await this.waitForTokenAndLoad();
    }
  }

  private async waitForTokenAndLoad(): Promise<void> {
    this.loading = true;

    try {
      await this.authService.waitForToken();
      console.log('Token available, loading sensors...');
      this.loadSensors();
    } catch (error) {
      console.error('Error waiting for token:', error);
      this.error = 'Authentication timeout. Please refresh the page.';
      this.loading = false;
    }
  }

  loadSensors(): void {
    this.loading = true;
    this.error = null;

    const token = this.authService.getStoredToken();

    if (!token) {
      this.error = 'No authentication token found';
      this.loading = false;
      return;
    } else {
      console.log("loadSensors token " + token)
    }

    const query = `
      query GetSensorsByUser($userId: String!) {
        sensorsByUser(userId: $userId) {
          sensorId
        }
      }
    `;

    const variables = {
      userId: this.userId
    };

    const headers = new HttpHeaders({
      'Content-Type': 'application/json',
      'Accept': 'application/json',
      'Authorization': `Bearer ${token}`
    });

    this.http.post<GraphQLResponse>(
      this.graphqlUrl,
      { query, variables },
      { headers }
    ).pipe(
      map(response => {
        if (response.errors) {
          throw new Error(response.errors[0]?.message || 'GraphQL Error');
        }
        return response.data.sensorsByUser;
      }),
      catchError(error => {
        console.error('Error loading sensors:', error);
        this.error = error.message || 'Failed to load sensors';
        return of([]);
      })
    ).subscribe(sensors => {
      this.sensors = sensors;
      this.loading = false;

      // Auto-select first sensor if available
      if (this.sensors.length > 0 && !this.selectedSensorId) {
        this.selectedSensorId = this.sensors[0].sensorId;
        this.onSensorChange();
      }
    });
  }

  onSensorChange(): void {
    if (this.selectedSensorId) {
      this.sensorSelected.emit(this.selectedSensorId);
    }
  }

  refreshSensors(): void {
    this.loadSensors();
  }
}
