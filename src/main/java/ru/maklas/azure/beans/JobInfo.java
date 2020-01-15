package ru.maklas.azure.beans;

import ru.maklas.azure.CsvUtils;

public class JobInfo {

	public static final String head = CsvUtils.toCsv("id", "person_id", "job_name", "salary");

	private int id;
	private int personId;
	private String jobName;
	private int salary; //rubles per month

	public JobInfo(int id, int personId, String jobName, int salary) {
		this.id = id;
		this.personId = personId;
		this.jobName = jobName;
		this.salary = salary;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getPersonId() {
		return personId;
	}

	public void setPersonId(int personId) {
		this.personId = personId;
	}

	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	public int getSalary() {
		return salary;
	}

	public void setSalary(int salary) {
		this.salary = salary;
	}

	@Override
	public String toString() {
		return CsvUtils.toCsv(id, personId, jobName, salary);
	}
}
