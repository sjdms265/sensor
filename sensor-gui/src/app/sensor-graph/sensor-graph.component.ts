
import {Component, Input, OnInit} from '@angular/core';
import { SensorDataService } from '../sensor-data.service';
import { AuthService } from '../auth.service';
import {GraphSensorEndpoint} from "../dto/GraphSensorEndpoint";

@Component({
  selector: 'app-sensor-graph',
  templateUrl: './sensor-graph.component.html',
  styleUrls: ['./sensor-graph.component.css']
})
export class SensorGraphComponent implements OnInit {
  graphData: { time: string; value: number }[] = [];
  loading: boolean = false;
  error: string = '';
  @Input() sensorId: string = '';

  constructor(private sensorDataService: SensorDataService, private authService: AuthService) { }

  async ngOnInit(): Promise<void> {
     await this.waitForTokenAndLoad();
  }

  private async waitForTokenAndLoad(): Promise<void> {
    this.loading = true;

    try {
      await this.authService.waitForToken();
      console.log('Token available, loading loadSensorData...');
      this.loadSensorData('', '');
    } catch (error) {
      console.error('Error waiting for token:', error);
      this.error = 'Authentication timeout. Please refresh the page.';
      this.loading = false;
    }
  }

  async loadSensorData(userId: string, pageNumber: string): Promise<void>  {

    console.log("loadSensorData userId " + userId + " sensorId " + this.sensorId + " pageNumber " + pageNumber);

    this.loading = true;
    this.error = '';

    if(userId === '' || this.sensorId === '' || pageNumber === '') {
      this.loading = false;
      return;
    }

    try {
      // Wait for token
      const token = await this.authService.waitForToken();

      this.sensorDataService.getSensorDataByUserIdAndSensorId(token, userId, this.sensorId).subscribe({
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
    } catch (error) {
      this.error = 'Authentication timeout';
      console.error('Error waiting for token:', error);
      this.loading = false;
    }

  }

  refreshData(userId: string, sensorId: string, pageNumber: string): void {
    this.loadSensorData(userId, pageNumber);
  }
}
