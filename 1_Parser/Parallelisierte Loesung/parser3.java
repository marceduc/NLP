package test;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.text.NumberFormat;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class parser3 {

	public static List<String> wordList = new ArrayList<String>();
	public static List<String> yearList = new ArrayList<String>();
	public static List<String> titleList = new ArrayList<String>();
	public static List<String> meshList = new ArrayList<String>();

	public static List<Integer> anzahlWordList = new ArrayList<Integer>();
	public static List<Integer> anzahlYearList = new ArrayList<Integer>();
	public static List<Integer> anzahlTitleList = new ArrayList<Integer>();
	public static List<Integer> anzahlMeshList = new ArrayList<Integer>();

	public static int articleCount = 0;
	public static double wordCount = 0;
	public static double yearCount = 0;
	public static double titleCount = 0;
	public static double meshCount = 0;

	public static void main(String[] args) {

		File path = new File(args[0]);
		File[] files = path.listFiles();

		analyseAbs abs = new analyseAbs(files);
		analyseYears years = new analyseYears(files);
		analyseTitles titles = new analyseTitles(files);
		analyseMesh mesh = new analyseMesh(files);

		Thread t1 = new Thread(abs);
		Thread t2 = new Thread(years);
		Thread t3 = new Thread(titles);
		Thread t4 = new Thread(mesh);

		t1.start();
		t2.start();
		t3.start();
		t4.start();

		try {
			t1.join();
			t2.join();
			t3.join();
			t4.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// Variablen für Ausgabe
		int ind;
		String wordE;
		int anzahl;
		int top = 0;
		String anzahlP;
		String topP;

		// Formatierer
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(2);

		// Ausgabe: Anzahl Artikel
		System.out.println("");
		System.out.println("Artikel: " + articleCount);

		// Ausgabe: Wörterliste
		System.out.println("Wörter: " + (int) wordCount + " (" + wordList.size() + " distinct)");

		for (int i = 0; i < 50; i++) {

			if (wordList.isEmpty())
				break;
			ind = anzahlWordList.indexOf(Collections.max(anzahlWordList));
			wordE = wordList.get(ind);
			anzahl = anzahlWordList.get(ind);
			top += anzahl;
			anzahlP = " (" + nf.format(anzahl / wordCount * 100) + " %)";

			System.out.println(wordE + " : " + anzahl + anzahlP);

			anzahlWordList.remove(ind);
			wordList.remove(ind);

		}

		topP = "Top50 : " + (int) top + " (" + nf.format(top / wordCount * 100) + " %)";
		System.out.println(topP);
		top = 0;
		System.out.println("");

		// Ausgabe Jahreszahlen
		System.out.println("Jahresangaben: " + (int) yearCount + " (" + yearList.size() + " distinct)");

		for (int i = 0; i < 10; i++) {

			if (yearList.isEmpty())
				break;
			ind = anzahlYearList.indexOf(Collections.max(anzahlYearList));
			wordE = yearList.get(ind);
			anzahl = anzahlYearList.get(ind);
			top += anzahl;
			anzahlP = " (" + nf.format(anzahl / yearCount * 100) + " %)";

			System.out.println(wordE + " : " + anzahl + anzahlP);

			anzahlYearList.remove(ind);
			yearList.remove(ind);
		}

		topP = "Top10 : " + (int) top + " (" + nf.format(top / yearCount * 100) + " %)";
		System.out.println(topP);
		top = 0;
		System.out.println("");

		// Ausgabe Journaltitel
		System.out.println("Journale: " + (int) titleCount + " (" + titleList.size() + " distinct)");

		for (int i = 0; i < 10; i++) {

			if (titleList.isEmpty())
				break;
			ind = anzahlTitleList.indexOf(Collections.max(anzahlTitleList));
			wordE = titleList.get(ind);
			anzahl = anzahlTitleList.get(ind);
			top += anzahl;
			anzahlP = " (" + nf.format(anzahl / titleCount * 100) + " %)";

			System.out.println(wordE + " : " + anzahl + anzahlP);

			anzahlTitleList.remove(ind);
			titleList.remove(ind);

		}

		topP = "Top10 : " + (int) top + " (" + nf.format(top / titleCount * 100) + " %)";
		System.out.println(topP);
		top = 0;
		System.out.println("");

		// Ausgabe MeSH
		System.out.println("MesH: " + (int) meshCount + " (" + meshList.size() + " distinct)");

		for (int i = 0; i < 10; i++) {

			if (meshList.isEmpty())
				break;
			ind = anzahlMeshList.indexOf(Collections.max(anzahlMeshList));
			wordE = meshList.get(ind);
			anzahl = anzahlMeshList.get(ind);
			top += anzahl;
			anzahlP = " (" + nf.format(anzahl / meshCount * 100) + " %)";

			System.out.println(wordE + " : " + anzahl + anzahlP);

			anzahlMeshList.remove(ind);
			meshList.remove(ind);

		}

		topP = "Top10 : " + (int) top + " (" + nf.format(top / meshCount * 100) + " %)";
		System.out.println(topP);

	}

	// Klasse zum Parsen nach Wörter
	public static class analyseAbs implements Runnable {

		public static File[] paths;

		// Constructor
		public analyseAbs(File[] files) {

			paths = files;

		}

		// RUN
		public void run() {

			try {

				SAXParserFactory factory = SAXParserFactory.newInstance();
				SAXParser saxParser = factory.newSAXParser();
				UserHandler uh = new UserHandler();

				for (File datei : paths) {
					System.out.println("Parse nach Wörter: " + datei.getName());
					saxParser.parse(datei.getAbsolutePath(), uh);
				}

			} catch (Exception e) {
				e.printStackTrace();

			}

		}

		class UserHandler extends DefaultHandler {

			boolean ArticleTitle = false;
			boolean ArticleAbs = false;

			@Override
			public void startElement(String uri, String localName, String qName, Attributes attributes)
					throws SAXException {

				if (qName == "PubmedArticle") {
					articleCount += 1;
				}

				if (qName == "ArticleTitle") {
					ArticleTitle = true;
				}

				if (qName == "Abstract") {
					ArticleAbs = true;
				}

			}

			@Override
			public void endElement(String uri, String localName, String qName) throws SAXException {

				if (qName == "Abstract") {
					ArticleAbs = false;
				}

			}

			@Override
			public void characters(char ch[], int start, int length) throws SAXException {

				int ind;
				int anzahl;

				if (ArticleTitle) {

					String line = new String(ch, start, length).toLowerCase().replace(".", "");
					String[] words = line.split("\\s+");
					wordCount += words.length;

					for (String word : words) {

						if (wordList.contains(word)) {

							ind = wordList.indexOf(word);
							anzahl = anzahlWordList.get(ind) + 1;
							anzahlWordList.set(ind, anzahl);

						} else {

							wordList.add(word);
							anzahlWordList.add(1);

						}

					}

					ArticleTitle = false;

				}

				if (ArticleAbs) {

					String line = new String(ch, start, length).toLowerCase().replace(".", "");
					String[] words = line.split("\\s+");
					wordCount += words.length;

					for (String word : words) {

						if (wordList.contains(word)) {

							ind = wordList.indexOf(word);
							anzahl = anzahlWordList.get(ind) + 1;
							anzahlWordList.set(ind, anzahl);

						} else {

							wordList.add(word);
							anzahlWordList.add(1);

						}

					}

				}

			}

		}

	}

	// Klasse zum Parsen nach Jahreszahlen
	public static class analyseYears implements Runnable {

		public static File[] paths;

		public analyseYears(File[] files) {

			paths = files;

		}

		public void run() {

			try {

				SAXParserFactory factory = SAXParserFactory.newInstance();
				SAXParser saxParser = factory.newSAXParser();
				UserHandler uh = new UserHandler();

				for (File datei : paths) {
					System.out.println("Parse nach Jahresangaben: " + datei.getName());
					saxParser.parse(datei.getAbsolutePath(), uh);
				}

			} catch (Exception e) {
				e.printStackTrace();

			}

		}

		class UserHandler extends DefaultHandler {

			boolean found = false;
			boolean yearormedline = false;

			@Override
			public void startElement(String uri, String localName, String qName, Attributes attributes)
					throws SAXException {

				if (qName == "PubDate") {
					found = true;
				}
				if (qName == "Year" || qName == "MedlineDate") {

					yearormedline = true;
				}
			}

			public void endElement(String uri, String localName, String qName) throws SAXException {

				if (qName == "PubDate") {
					found = false;
				}
				if (qName == "Year" || qName == "MedlineDate") {
					yearormedline = false;
				}
			}

			@Override
			public void characters(char ch[], int start, int length) throws SAXException {

				if (found == true && yearormedline == true) {

					String year = new String(ch, start, length);
					
					if (year.length() > 4) {
						
						if (year.substring(4,5).equals("-") == false) {
							
							year = year.substring(0,4);
							
						}
					}
					
					if (year == "") return;
					int ind;
					int anzahl;

					// Journal: Jahreszahlen

					yearCount += 1;

					if (yearList.contains(year)) {

						ind = yearList.indexOf(year);
						anzahl = anzahlYearList.get(ind) + 1;
						anzahlYearList.set(ind, anzahl);

					} else {

						yearList.add(year);
						anzahlYearList.add(1);

					}

				}

			}

		}

	}

	// Klasse zum Parsen nach Journaltiteln
	public static class analyseTitles implements Runnable {

		public static File[] paths;

		public analyseTitles(File[] files) {

			paths = files;

		}

		public void run() {

			try {

				SAXParserFactory factory = SAXParserFactory.newInstance();
				SAXParser saxParser = factory.newSAXParser();
				UserHandler uh = new UserHandler();

				for (File datei : paths) {
					System.out.println("Parse nach Journals: " + datei.getName());
					saxParser.parse(datei.getAbsolutePath(), uh);
				}

			} catch (Exception e) {
				e.printStackTrace();

			}

		}

		class UserHandler extends DefaultHandler {

			boolean found = false;

			@Override
			public void startElement(String uri, String localName, String qName, Attributes attributes)
					throws SAXException {

				if (qName == "Title") {
					found = true;
				}

			}

			@Override
			public void characters(char ch[], int start, int length) throws SAXException {

				if (found) {

					int ind;
					int anzahl;

					String title = new String(ch, start, length);
					titleCount += 1;

					if (titleList.contains(title)) {

						ind = titleList.indexOf(title);
						anzahl = anzahlTitleList.get(ind) + 1;
						anzahlTitleList.set(ind, anzahl);

					} else {

						titleList.add(title);
						anzahlTitleList.add(1);

					}

					found = false;

				}

			}

		}

	}

	// Klasse zum Parsen nach MesH
	public static class analyseMesh implements Runnable {

		public static File[] paths;

		public analyseMesh(File[] files) {

			paths = files;

		}

		public void run() {

			try {

				SAXParserFactory factory = SAXParserFactory.newInstance();
				SAXParser saxParser = factory.newSAXParser();
				UserHandler uh = new UserHandler();

				for (File datei : paths) {
					System.out.println("Parse nach MesH: " + datei.getName());
					saxParser.parse(datei.getAbsolutePath(), uh);
				}

			} catch (Exception e) {
				e.printStackTrace();

			}

		}

		class UserHandler extends DefaultHandler {

			boolean inMesh = false;
			boolean qualifier = false;
			String descriptor;
			List<String> qualifierList = new ArrayList<String>();

			@Override
			public void startElement(String uri, String localName, String qName, Attributes attributes)
					throws SAXException {

				if (qName == "MeshHeading") {
					inMesh = true;
				}
				if (qName == "QualifierName") {
					qualifier = true;
				}
			}

			@Override
			public void endElement(String uri, String localName, String qName) throws SAXException {

				if (qName == "MeshHeading") {

					if (qualifierList.isEmpty()) {

						meshCount += 1;

						if (meshList.contains(descriptor)) {

							int ind = meshList.indexOf(descriptor);
							int anzahl = anzahlMeshList.get(ind) + 1;
							anzahlMeshList.set(ind, anzahl);

						} else {

							meshList.add(descriptor);
							anzahlMeshList.add(1);

						}

					} else {

						for (String qual : qualifierList) {

							String mesh = descriptor + "//" + qual;
							meshCount += 1;

							if (meshList.contains(mesh)) {

								int ind = meshList.indexOf(mesh);
								int anzahl = anzahlMeshList.get(ind) + 1;
								anzahlMeshList.set(ind, anzahl);

							} else {

								meshList.add(mesh);
								anzahlMeshList.add(1);

							}

						}

					}

				}

				qualifierList.clear();

			}

			@Override
			public void characters(char ch[], int start, int length) throws SAXException {

				if (inMesh) {
					descriptor = new String(ch, start, length);
				}
				if (qualifier) {
					qualifierList.add(new String(ch, start, length));
					qualifier = false;
				}

			}

		}

	}

}
