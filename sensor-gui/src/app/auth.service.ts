import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, BehaviorSubject, throwError } from 'rxjs';
import { tap, catchError } from 'rxjs/operators';
import { TokenResponse } from './TokenResponse';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private authTokenSubject = new BehaviorSubject<string | null>(null);
  public authToken$ = this.authTokenSubject.asObservable();

  private readonly AUTH_URL = 'http://localhost:8090/sensormanager/api/auth/token';
  private readonly TOKEN_KEY = 'authToken';
  private readonly REFRESH_TOKEN_KEY = 'refreshToken';
  private readonly MAX_WAIT_TIME = 10000; // 10 seconds
  private readonly CHECK_INTERVAL = 100; // 100ms

  private tokenRequestInProgress = false;

  constructor(private http: HttpClient) {
    // Check if token exists on service initialization
    const existingToken = this.getStoredToken();
    if (existingToken) {
      this.authTokenSubject.next(existingToken);
    }
  }

  /**
   * Get token from localStorage
   */
  getStoredToken(): string | null {
    console.log("getStoredToken", localStorage.getItem(this.TOKEN_KEY))
    return localStorage.getItem(this.TOKEN_KEY);
  }

  /**
   * Get refresh token from localStorage
   */
  getStoredRefreshToken(): string | null {
    return localStorage.getItem(this.REFRESH_TOKEN_KEY);
  }

  /**
   * Check if user is authenticated
   */
  isAuthenticated(): boolean {
    return !!this.getStoredToken();
  }

  /**
   * Request authentication token
   */
  requestToken(username: string = 'admin', password: string = '1234'): Observable<TokenResponse> {
    if (this.tokenRequestInProgress) {
      console.log('Token request already in progress');
      return throwError(() => new Error('Token request already in progress'));
    }

    this.tokenRequestInProgress = true;

    const headers = new HttpHeaders({
      'Content-Type': 'application/json'
    });

    const body = { username, password };

    return this.http.post<TokenResponse>(this.AUTH_URL, body, { headers }).pipe(
      tap((data: TokenResponse) => {
        console.log('Token response:', data);
        localStorage.setItem(this.TOKEN_KEY, data.access_token);
        localStorage.setItem(this.REFRESH_TOKEN_KEY, data.refresh_token);
        this.authTokenSubject.next(data.access_token);
        this.tokenRequestInProgress = false;
        console.log('Token successfully requested and stored');
      }),
      catchError((error) => {
        this.tokenRequestInProgress = false;
        console.error('Failed to request token:', error);
        return throwError(() => error);
      })
    );
  }

  /**
   * Wait for token to be available in localStorage
   * Returns a Promise that resolves when token is available or rejects on timeout
   */
  waitForToken(): Promise<string> {
    return new Promise((resolve, reject) => {
      const startTime = Date.now();

      const checkToken = () => {
        const token = this.getStoredToken();

        if (token) {
          console.log('Token found');
          resolve(token);
          return;
        }

        // Check if we've exceeded max wait time
        if (Date.now() - startTime > this.MAX_WAIT_TIME) {
          console.error('Timeout waiting for auth token');
          reject(new Error('Authentication timeout'));
          return;
        }

        // Check again after interval
        setTimeout(checkToken, this.CHECK_INTERVAL);
      };

      checkToken();
    });
  }

  /**
   * Clear authentication tokens
   */
  clearTokens(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.REFRESH_TOKEN_KEY);
    this.authTokenSubject.next(null);
    console.log('Tokens cleared');
  }

  /**
   * Logout user
   */
  logout(): void {
    this.clearTokens();
  }
}
