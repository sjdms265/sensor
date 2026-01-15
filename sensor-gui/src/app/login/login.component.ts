import { Component } from '@angular/core';
import { AuthService } from '../auth.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent {
  username: string = '';
  password: string = '';
  error: string = '';
  isLoading: boolean = false;

  constructor(
    private authService: AuthService,
  private router: Router
) {}

  onSubmit(): void {
    if (!this.username || !this.password) {
      this.error = 'Please enter both username and password';
      return;
    }

    this.isLoading = true;
    this.error = '';

    this.authService.login(this.username, this.password).subscribe({
      next: () => {
        console.log('Login successful');
        localStorage.setItem('username', this.username);
        this.isLoading = false;
        this.router.navigate(['/']);
      },
      error: (err) => {
        console.error('Login error:', err);
        this.error = 'Login failed. Please check your credentials.';
        this.isLoading = false;
      }
    });
  }
}
