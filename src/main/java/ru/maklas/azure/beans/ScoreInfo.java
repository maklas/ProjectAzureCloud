package ru.maklas.azure.beans;

import ru.maklas.azure.CsvUtils;

public class ScoreInfo {

	public static final String head = CsvUtils.toCsv("user_id", "score");

	private int userId;
	private int score; //0...100

	public ScoreInfo(int userId, int score) {
		this.userId = userId;
		this.score = score;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	@Override
	public String toString() {
		return CsvUtils.toCsv(userId, score);
	}
}
