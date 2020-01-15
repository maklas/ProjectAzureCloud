package ru.maklas.azure.beans;

import ru.maklas.azure.CsvUtils;

public class CreditHistoryRecord {

	public static final String head = CsvUtils.toCsv("id", "user_id", "amount", "period");

	private int id;
	private int userId;
	private int amount; //rubles
	private int period; //months


	public CreditHistoryRecord(int id, int userId, int amount, int period) {
		this.id = id;
		this.userId = userId;
		this.amount = amount;
		this.period = period;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public int getAmount() {
		return amount;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}

	public int getPeriod() {
		return period;
	}

	public void setPeriod(int period) {
		this.period = period;
	}

	@Override
	public String toString() {
		return CsvUtils.toCsv(id, userId, amount, period);
	}
}
