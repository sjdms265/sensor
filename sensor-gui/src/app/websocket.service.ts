import {Injectable} from "@angular/core";
import SockJS from 'sockjs-client';
import Stomp from 'stompjs';

@Injectable()
export class WebsocketService {

  // Open connection with the back-end socket
  public connect() {
    let socket = new SockJS(`http://localhost:8081/sensor-gui`);

    return Stomp.over(<WebSocket>socket);
  }
}
