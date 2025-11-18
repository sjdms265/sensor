import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { tap } from 'rxjs/operators';
import { TokenResponse } from './dto/TokenResponse';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private authTokenSubject = new BehaviorSubject<string | null>(null);
  public authToken$ = this.authTokenSubject.asObservable();

  private readonly TOKEN_KEY = 'authToken';
  private readonly REFRESH_TOKEN_KEY = 'refreshToken';
  private readonly MAX_WAIT_TIME = 10000; // 10 seconds
  private readonly CHECK_INTERVAL = 100; // 100ms
  private apiUrl = 'http://localhost:8090/sensormanager';

  private tokenRequestInProgress = false;

  constructor(private http: HttpClient) {
    // Check if token exists on service initialization
    const existingToken = this.getStoredToken();
    if (existingToken) {
      this.authTokenSubject.next(existingToken);
    }
  }

  login(username: string, password: string): Observable<TokenResponse> {
    return this.http.post<TokenResponse>(`${this.apiUrl}/api/auth/token`, {
      username,
      password
    }).pipe(
      tap(response => {
        this.setToken(response.access_token);
        if (response.refresh_token) {
          this.setRefreshToken(response.refresh_token);
        }
      })
    );
  }

  requestToken(): Observable<TokenResponse> {
    // This is for automatic token requests (if still needed)
    return this.http.get<TokenResponse>(`${this.apiUrl}/token`).pipe(
      tap(response => {
        this.setToken(response.access_token);
        if (response.refresh_token) {
          this.setRefreshToken(response.refresh_token);
        }
      })
    );
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
    const token = this.getToken();
    if (!token) {
      return false;
    }

    // Optional: Check if token is expired
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      const expiry = payload.exp * 1000; // Convert to milliseconds
      return Date.now() < expiry;
    } catch (e) {
      return false;
    }
  }

  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  setToken(token: string): void {
    localStorage.setItem(this.TOKEN_KEY, token);
  }

  getRefreshToken(): string | null {
    return localStorage.getItem(this.REFRESH_TOKEN_KEY);
  }

  setRefreshToken(token: string): void {
    localStorage.setItem(this.REFRESH_TOKEN_KEY, token);
  }

  getUsername(): string {
    return localStorage.getItem("username") || '';
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
    localStorage.removeItem('username');
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
