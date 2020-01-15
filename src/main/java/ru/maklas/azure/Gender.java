package ru.maklas.azure;

import com.badlogic.gdx.utils.Array;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

public enum Gender {

	MALE, FEMALE, UNKNOWN;

	private static String[] unisexNameEndings = {"саша", "женя", "sasha"};

	private static String[] manEndings = {"др", "ий", "ей", "ис", "лл", "илья", "он", "ад", "ан", "кита", "им", "ил",
			"ир", "ел", "кс", "ег", "ор", "ай", "ав", "ем", "има", "ома", "ема", "рь", "ур", "ин", "рк", "ст", "арк",
			"паша", "ев", "рома", "еб", "даня", "ян", "ен", "остя", "нид", "тас", "анила",  "рій", "ард", "етр", "еша",
			"лик", "сеня", "дар", "лод", "рас", "итя", "вид", "ха", "алій", "рик", "ков", "авик", "ега", "ежа", "сек",
			"оря", "нек", "пп", "дик", "берт", //Endings

			"гера", "кира", "юра", "жора", "валера", "коля", "толя", "гена", "лева", "люша", "сева", "деня", "виталя",
			"сава", "амиль", "гарри", "ваня", "гоша", "леня", "гриша", "миша",  "саня", "жека", "вася", "петя", "ярик",
			"дэн", "самед", "эмиль", "рустам", "адам", "темка", //names

			"isha", "asha", "enis", "sey", "axim", "rtem", "slav", "gor", "ander", "iy", "slan", "opher", "omas", //English Endings

			"dimik", "andrew", "edgar", "dima", "oleg", "gosha", "danil", "danny", "alex", "nikita", "vadim", "henri",
			"stefan", "sema", "georg", "ilya", "robert", "vlad", "kirill", "bogdan", "timur", "andrey", "sergey", "david",
			"leonid", "valeriy", "anton", "ivan", "slava", "gleb", "yura" , "danya", "eldar", "johan", "alexey", "maks" //English names
};

	private static String[] femaleEndings = {"ия", "ва", "на", "иса", "ита", "астя", "юша", "атя", "ика", "ня",
			"ля", "аша", "еся", "иза", "лла", "ра", "мма", "арья", "вета", "фья", "ася", "лья", "сья", "нюта", "овь", //Endings

			"ольга", "мариша", "людмила", "анастасія", //names

			"sha", //English endings

			"anna", "emilia", "elizabeth"//English names
			};


	private static String[] maleLastNameEndings = {"вич", "ый", "ов", "ин", "ев"};
	private static String[] femaleLastNameEndings = {"вна", "ая", "ва", "на"};

	static {
		Array<String> arr = new Array<>(manEndings);
		Set<String> set = new HashSet<>();
		for (String s : arr) {
			if (!set.add(s.replace('ё', 'е'))) {
				System.err.println('\'' + s + "' repeated in manEndings");
			}
		}
		arr.addAll(new Array<>(femaleEndings));
		set = new HashSet<>();
		for (String s : arr) {
			if (!set.add(s.replace('ё', 'е'))) {
				System.err.println('\'' + s + "' repeated in femaleEndings");
			}
		}
	}

	public static Gender byName(String firstName, @Nullable String lastName) {
		firstName = firstName.toLowerCase().replace('ё', 'е');
		lastName = StringUtils.isEmpty(lastName) ? null : lastName.toLowerCase().replace('ё', 'е');

		for (String unisexNameEnding : unisexNameEndings) {
			if (firstName.endsWith(unisexNameEnding)) {
				return lastName != null ? lastNameGender(lastName) : UNKNOWN;
			}
		}

		for (String end : manEndings) {
			if (firstName.endsWith(end)) return MALE;
		}

		for (String end : femaleEndings) {
			if (firstName.endsWith(end)) return FEMALE;
		}

		return lastName != null ? lastNameGender(lastName) : UNKNOWN;
	}

	private static Gender lastNameGender(@NotNull String lastName) {
		for (String e : maleLastNameEndings) {
			if (lastName.endsWith(e)) {
				return MALE;
			}
		}
		for (String e : femaleLastNameEndings) {
			if (lastName.endsWith(e)) {
				return FEMALE;
			}
		}
		return UNKNOWN;
	}

}
