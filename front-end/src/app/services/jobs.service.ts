import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Job } from '../model/job';
import { Observable } from 'rxjs';
import { User } from '../model/user';

@Injectable({
  providedIn: 'root'
})
export class JobsService {

  private baseUrl = 'https://localhost:8443';

  constructor(private http: HttpClient) {}

  addJob(job: Job, userId: number): Observable<HttpResponse<string>> {
    return this.http.post<string>(`${this.baseUrl}/in/${userId}/new-job`, job, { observe: 'response' });
  }

  getJobs(userId: number): Observable<Job[]> {
    return this.http.get<Job[]>(`${this.baseUrl}/in/${userId}/jobs`);
  }

  getRecommendedJobs(userId: number): Observable<Job[]> {
    return this.http.get<Job[]>(`${this.baseUrl}/in/${userId}/recommended-jobs`);
  }

  apply(jobId: number, userId: number): Observable<string> {
    return this.http.put<string>(`${this.baseUrl}/in/${userId}/jobs/make-application/${jobId}`, {});
  }

  getRecruiterDashboard(userId: number): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/in/${userId}/recruiter/dashboard`);
  }

  getRecruiterJobs(userId: number): Observable<Job[]> {
    return this.http.get<Job[]>(`${this.baseUrl}/in/${userId}/recruiter/jobs`);
  }

  getApplicants(userId: number, jobId: number): Observable<User[]> {
    return this.http.get<User[]>(`${this.baseUrl}/in/${userId}/jobs/${jobId}/applicants`);
  }

  updateJob(userId: number, jobId: number, job: Job): Observable<HttpResponse<string>> {
    return this.http.put<string>(`${this.baseUrl}/in/${userId}/jobs/${jobId}`, job, { observe: 'response' });
  }

  deleteJob(userId: number, jobId: number): Observable<HttpResponse<string>> {
    return this.http.delete<string>(`${this.baseUrl}/in/${userId}/jobs/${jobId}`, { observe: 'response' });
  }
}
