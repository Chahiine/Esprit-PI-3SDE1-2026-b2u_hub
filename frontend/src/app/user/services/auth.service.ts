import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject, tap } from 'rxjs';
import { environment } from '../../../environments/environment';

export type UserRole = 'STUDENT' | 'COMPANY' | 'ADMIN';

export interface AuthResponse {
  token: string;
  userId: number;
  firstName: string;
  lastName: string;
  email: string;
  role: UserRole;
  profileImageUrl?: string;
  cvUrl?: string;
  bio?: string;
  skills?: string;
}

const TOKEN_KEY = 'b2u_token';
const USER_KEY = 'b2u_user';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly api = `${environment.apiUrl}/api/users`;

  private _user$ = new BehaviorSubject<AuthResponse | null>(this.loadUser());

  get currentUser(): AuthResponse | null {
    return this._user$.value;
  }

  isLoggedIn(): boolean {
    return !!this._user$.value;
  }

  isAdmin(): boolean {
    return this._user$.value?.role === 'ADMIN';
  }

  isStudent(): boolean {
    return this._user$.value?.role === 'STUDENT';
  }

  isCompany(): boolean {
    return this._user$.value?.role === 'COMPANY';
  }

  constructor(private http: HttpClient) {}

  register(req: any): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${this.api}/register`, req)
      .pipe(tap((res) => this.saveSession(res)));
  }

  login(req: any): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${this.api}/login`, req)
      .pipe(tap((res) => this.saveSession(res)));
  }

  logout(): void {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    this._user$.next(null);
  }

  getToken(): string | null {
    return localStorage.getItem(TOKEN_KEY);
  }

  updateProfile(req: any): Observable<AuthResponse> {
    return this.http
      .put<AuthResponse>(`${this.api}/me`, req)
      .pipe(tap((res) => this.saveSession({ ...res, token: this.getToken()! })));
  }

  changePassword(req: any): Observable<string> {
    return this.http.put(`${this.api}/me/password`, req, { responseType: 'text' });
  }

  uploadProfileImage(file: File): Observable<string> {
    const form = new FormData();
    form.append('file', file);
    return this.http.post(`${this.api}/me/profile-image`, form, { responseType: 'text' }).pipe(
      tap((url) => {
        const cur = this._user$.value;
        if (cur) this.saveSession({ ...cur, profileImageUrl: url });
      }),
    );
  }

  uploadCv(file: File): Observable<string> {
    const form = new FormData();
    form.append('file', file);
    return this.http.post(`${this.api}/me/cv`, form, { responseType: 'text' }).pipe(
      tap((url) => {
        const cur = this._user$.value;
        if (cur) this.saveSession({ ...cur, cvUrl: url });
      }),
    );
  }

  user(): AuthResponse | null {
    return this._user$.value;
  }

  private saveSession(res: AuthResponse): void {
    localStorage.setItem(TOKEN_KEY, res.token);
    localStorage.setItem(USER_KEY, JSON.stringify(res));
    this._user$.next(res);
  }

  private loadUser(): AuthResponse | null {
    try {
      const raw = localStorage.getItem(USER_KEY);
      return raw ? JSON.parse(raw) : null;
    } catch {
      return null;
    }
  }
}
