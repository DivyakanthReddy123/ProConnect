import { User } from "./user";

export class Job {
  id: number;
  title: string;
  description: string;
  timestamp: Date;
  companyName: string;
  location: string;
  jobType: string;
  experienceRequired: string;
  salary: string;
  skillsRequired: string;
  workMode: string;
  numberOfOpenings: number;
  applicationDeadline: string;
  status: string;
  userMadeBy: User;
  usersApplied: Array<User> = new Array<User>();
}
