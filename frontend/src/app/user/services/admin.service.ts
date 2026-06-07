import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class AdminService {
  private readonly api = `${environment.apiUrl}/api/users/admin`;
  constructor(private http: HttpClient) {}
  getAllUsers(): Observable<any[]> { return this.http.get<any[]>(`${this.api}/all`); }
  getByRole(role: string): Observable<any[]> { return this.http.get<any[]>(`${this.api}/by-role`, { params: { role } }); }
  banUser(id: number): Observable<any> { return this.http.put(`${this.api}/${id}/ban`, {}); }
  unbanUser(id: number): Observable<any> { return this.http.put(`${this.api}/${id}/unban`, {}); }
  deleteUser(id: number): Observable<void> { return this.http.delete<void>(`${this.api}/${id}`); }
  getStats(): Observable<any> { return this.http.get<any>(`${this.api}/stats`); }
}
