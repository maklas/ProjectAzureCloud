package ru.maklas.azure;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;
import org.jetbrains.annotations.Nullable;
import ru.maklas.azure.beans.*;

import java.io.File;
import java.time.LocalDate;
import java.util.Random;

public class Main {

	private static final Random rand = new Random();

	private static final IntMap<Person> personMap = new IntMap<>();
	private static final IntMap<JobInfo> jobMap = new IntMap<>();
	private static final IntMap<MarriageInfo> marriageMap = new IntMap<>();
	private static final IntMap<Array<CreditHistoryRecord>> creditMap = new IntMap<>();
	private static final IntMap<ScoreInfo> scoreMap = new IntMap<>();

	private static int personIdCounter;
	private static int jobIdCounter;
	private static int marriageIdCounter;
	private static int creditHistoryIdCounter;

	public static void main(String[] args) {
		int size = 1_000_000;
		Array<Person> people = new Array<>();
		for (int i = 0; i < size; i++) {
			int id = personIdCounter++;
			Person person = randomPerson(id);
			people.add(person);
			personMap.put(id, person);
			populatePerson(id);
			ScoreInfo scoreInfo = score(id);
			scoreMap.put(id, scoreInfo);
		}

		for (int i = 0; i < 100; i++) {
			printPerson(i);
		}

		double sum = 0;
		for (Person person : people) {
			sum += scoreMap.get(person.getId()).getScore();
		}

		System.out.println("Average score: " + (sum / size));



		File saveFolder = Utils.resourceSource(".");
		CsvUtils.writeCsv(new File(saveFolder, "people.csv"), personMap.values().toArray(), Person.head);
		CsvUtils.writeCsv(new File(saveFolder, "jobs.csv"), jobMap.values().toArray(), JobInfo.head);
		CsvUtils.writeCsv(new File(saveFolder, "marriages.csv"), marriageMap.values().toArray(), MarriageInfo.head);
		CsvUtils.writeCsv(new File(saveFolder, "credit_histories.csv"), getInlinedCH(), CreditHistoryRecord.head);
		CsvUtils.writeCsv(new File(saveFolder, "scores.csv"), scoreMap.values().toArray(), ScoreInfo.head);
	}

	private static Array<CreditHistoryRecord> getInlinedCH() {
		Array<CreditHistoryRecord> arr = new Array<>();
		for (Array<CreditHistoryRecord> value : creditMap.values()) {
			arr.addAll(value);
		}
		return arr;
	}

	/** Job, Marriage and Credits **/
	private static void populatePerson(int id) {
		Person person = personMap.get(id);
		int age = person.getAge();
		double jobProbability = age < 17 ? 0.1 : (age < 20 ? 0.6 : 0.97);
		if (rand.nextDouble() < jobProbability) {
			JobInfo jobInfo = randomJobInfo(id);
			jobMap.put(id, jobInfo);
		}
		double marriageProbability = age <= 18 ? 0.01 : 0.05 + Math.min(0.6, Math.sqrt((age - 18) / 60.0));
		if (rand.nextDouble() < marriageProbability) {
			MarriageInfo marriage = randomMarriage(person);
			marriageMap.put(id, marriage);
		}
		int records = rand.nextDouble() < 0.25 ? 0 : (rand.nextInt(3) + 1);
		for (int i = 0; i < records; i++) {
			CreditHistoryRecord record = randomRecord(id);
			Array<CreditHistoryRecord> history = creditMap.get(id);
			if (history == null) {
				history = new Array<>();
				creditMap.put(id, history);
			}
			history.add(record);
		}
	}

	private static void printPerson(int id) {
		Person person = personMap.get(id);
		@Nullable JobInfo job = jobMap.get(id);
		@Nullable MarriageInfo marriage = marriageMap.get(id);
		@Nullable Array<CreditHistoryRecord> creditHistory = creditMap.get(id);
		@Nullable ScoreInfo score = scoreMap.get(id);
		System.out.println("Person: " + person.getId() + ", " + person.getLastName() + " " + person.getName() + ", " + person.getAge() + " y.o.");
		System.out.println("Job: " + (job != null ? job.getJobName() + ", " + (job.getSalary() / 1000) + "k": "null"));
		System.out.println("Marriage: " + marriage);
		System.out.println("History: " + creditHistory);
		System.out.println("Score: " + (score != null ? score.getScore() : "null"));
		System.out.println("------------------------");
	}

	private static CreditHistoryRecord randomRecord(int personId) {
		int amount = ((int) MathUtils.randomTriangular(2, 300, 5)) * 10_000;
		int period = (int) ((Math.sqrt(amount) / 50.0) * randDouble(0.5, 1.5));
		return new CreditHistoryRecord(creditHistoryIdCounter++, personId, amount, period);
	}

	private static JobInfo randomJobInfo(int personId) {
		return new JobInfo(jobIdCounter++, personId, CsvUtils.randomJobName(), randInt(20, 250) * 1_000);
	}

	@Nullable
	private static MarriageInfo randomMarriage(Person person) {
		if (person.getAge() < 18) return null;
		long start = LocalDate.now().toEpochDay();
		long end = person.getBirthDate().plusYears(18).toEpochDay();

		return new MarriageInfo(marriageIdCounter++, person.getId(), LocalDate.ofEpochDay(randLong(start, end)));
	}

	private static Person randomPerson(int id) {
		Gender gender = rand.nextBoolean() ? Gender.MALE : Gender.FEMALE;
		LocalDate birthDate = LocalDate.now().minusYears(randInt(18, 80)).minusDays(randInt(0, 365));
		return new Person(id, CsvUtils.randomName(gender), CsvUtils.randomLastName(gender), String.valueOf(randLong(1111111111L, 9999999999L)), gender, birthDate);
	}

	private static ScoreInfo score(int personId) {
		Person person = personMap.get(personId);
		@Nullable JobInfo job = jobMap.get(personId);
		@Nullable MarriageInfo marriage = marriageMap.get(personId);
		@Nullable Array<CreditHistoryRecord> creditHistory = creditMap.get(personId);

		double score;
		if (job == null || job.getSalary() < 15_000) {
			score = 0;
		} else {
			score = 10;
			score += Math.min((job.getSalary() / 7_000), 50);
			if (person.getGender() == Gender.MALE) {
				score += 5;
			}
			int age = person.getAge();
			score += (age < 60 ? ((age - 18) / 7.0) : 0);
			if (marriage != null) {
				score += 10;
			}
			if (creditHistory != null && !creditHistory.isEmpty()) {
				score += 5;
				int maxCreditAmount = 0;
				for (CreditHistoryRecord record : creditHistory) {
					if (record.getAmount() > maxCreditAmount) {
						maxCreditAmount = record.getAmount();
					}
				}
				score += Math.min((maxCreditAmount / 50_000), 50);
			}
			score *= randDouble(0.8, 1.3);
			score += randInt(-10, 10);
		}

		return new ScoreInfo(personId, MathUtils.clamp((int) score, 0, 100));
	}

	/** All inclusive **/
	private static int randInt(int min, int max) {
		return rand.nextInt(max - min + 1) + min;
	}

	/** All inclusive **/
	private static long randLong(long min, long max) {
		return MathUtils.random(min, max);
	}

	/** All inclusive **/
	private static double randDouble(double min, double max) {
		return rand.nextDouble() * (max - min) + min;
	}

}
