package ru.maklas.azure.beans;

import ru.maklas.azure.Gender;
import ru.maklas.azure.CsvUtils;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Person {

	public static final String head = CsvUtils.toCsv("id", "name", "last_name", "passport", "gender", "birth_date");

	private int id;
	private String name;
	private String lastName;
	private String passport;
	private Gender gender;
	private LocalDate birthDate;


	public Person(int id, String name, String lastName, String passport, Gender gender, LocalDate birthDate) {
		this.id = id;
		this.name = name;
		this.lastName = lastName;
		this.passport = passport;
		this.gender = gender;
		this.birthDate = birthDate;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getPassport() {
		return passport;
	}

	public void setPassport(String passport) {
		this.passport = passport;
	}

	public Gender getGender() {
		return gender;
	}

	public void setGender(Gender gender) {
		this.gender = gender;
	}

	public LocalDate getBirthDate() {
		return birthDate;
	}

	public void setBirthDate(LocalDate birthDate) {
		this.birthDate = birthDate;
	}

	@Override
	public String toString() {
		return CsvUtils.toCsv(id, name, lastName, passport, gender, birthDate);
	}

	public int getAge() {
		return (int) ChronoUnit.YEARS.between(birthDate, LocalDate.now());
	}
}
