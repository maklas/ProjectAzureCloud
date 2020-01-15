package ru.maklas.azure.beans;

import ru.maklas.azure.CsvUtils;

import java.time.LocalDate;

public class MarriageInfo {

	public static final String head = CsvUtils.toCsv("id", "person_id", "engagementDate");

	private int id;
	private int personId;
	private LocalDate engagementDate;

	public MarriageInfo(int id, int personId, LocalDate engagementDate) {
		this.id = id;
		this.personId = personId;
		this.engagementDate = engagementDate;
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

	public LocalDate getEngagementDate() {
		return engagementDate;
	}

	public void setEngagementDate(LocalDate engagementDate) {
		this.engagementDate = engagementDate;
	}

	@Override
	public String toString() {
		return CsvUtils.toCsv(id, personId, engagementDate);
	}
}
