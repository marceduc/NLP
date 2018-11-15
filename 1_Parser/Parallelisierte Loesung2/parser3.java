package test;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.text.NumberFormat;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class parser3 {

	public static HashMap<String, Integer> wL = new HashMap<String, Integer>();
	public static HashMap<String, Integer> yL = new HashMap<String, Integer>();
	public static HashMap<String, Integer> tL = new HashMap<String, Integer>();
	public static HashMap<String, Integer> mL = new HashMap<String, Integer>();

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

		HashMap<String, Integer> wLs = sortByValue(wL);
		HashMap<String, Integer> yLs = sortByValue(yL);
		HashMap<String, Integer> tLs = sortByValue(tL);
		HashMap<String, Integer> mLs = sortByValue(mL);

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
		System.out.println("Wörter: " + (int) wordCount + " (" + wL.size() + " distinct)");

		int i = 0;

		for (HashMap.Entry<String, Integer> e : wLs.entrySet()) {

			anzahlP = " (" + nf.format(e.getValue() / wordCount * 100) + " %)";
			top += e.getValue();
			System.out.println(e.getKey() + " : " + e.getValue() + anzahlP);

			if (i == 49)
				break;
			i++;
		}

		topP = "Top50 : " + (int) top + " (" + nf.format(top / wordCount * 100) + " %)";
		System.out.println(topP);
		top = 0;
		i = 0;
		System.out.println("");

		// Ausgabe Jahreszahlen
		System.out.println("Jahresangaben: " + (int) yearCount + " (" + yL.size() + " distinct)");

		for (HashMap.Entry<String, Integer> e : yLs.entrySet()) {

			anzahlP = " (" + nf.format(e.getValue() / yearCount * 100) + " %)";
			top += e.getValue();
			System.out.println(e.getKey() + " : " + e.getValue() + anzahlP);

			if (i == 9)
				break;
			i++;
		}

		topP = "Top10 : " + (int) top + " (" + nf.format(top / yearCount * 100) + " %)";
		System.out.println(topP);
		top = 0;
		i = 0;
		System.out.println("");

		// Ausgabe Journaltitel
		System.out.println("Journale: " + (int) titleCount + " (" + tL.size() + " distinct)");

		for (HashMap.Entry<String, Integer> e : tLs.entrySet()) {

			anzahlP = " (" + nf.format(e.getValue() / titleCount * 100) + " %)";
			top += e.getValue();
			System.out.println(e.getKey() + " : " + e.getValue() + anzahlP);

			if (i == 9)
				break;
			i++;
		}

		topP = "Top10 : " + (int) top + " (" + nf.format(top / titleCount * 100) + " %)";
		System.out.println(topP);
		top = 0;
		i = 0;
		System.out.println("");

		// Ausgabe MeSH
		System.out.println("MesH: " + (int) meshCount + " (" + mL.size() + " distinct)");

		for (HashMap.Entry<String, Integer> e : mLs.entrySet()) {

			anzahlP = " (" + nf.format(e.getValue() / meshCount * 100) + " %)";
			top += e.getValue();
			System.out.println(e.getKey() + " : " + e.getValue() + anzahlP);

			if (i == 9)
				break;
			i++;
		}

		topP = "Top10 : " + (int) top + " (" + nf.format(top / meshCount * 100) + " %)";
		System.out.println(topP);

	}

	public static HashMap<String, Integer> sortByValue(HashMap<String, Integer> hm) {
		// Create a list from elements of HashMap
		List<Map.Entry<String, Integer>> list = new LinkedList<Map.Entry<String, Integer>>(hm.entrySet());

		// Sort the list
		Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
			public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
				return (o2.getValue()).compareTo(o1.getValue());
			}
		});

		// put data from sorted list to hashmap
		HashMap<String, Integer> temp = new LinkedHashMap<String, Integer>();
		for (Map.Entry<String, Integer> aa : list) {
			temp.put(aa.getKey(), aa.getValue());
		}
		return temp;
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

				int anzahl;

				if (ArticleTitle) {

					String line = new String(ch, start, length).toLowerCase().replace(".", "");
					String[] words = line.split("\\s+");
					wordCount += words.length;

					for (String word : words) {

						if (wL.containsKey(word)) {

							anzahl = wL.get(word) + 1;
							wL.put(word, anzahl);

						} else {

							wL.put(word, 1);

						}

					}

					ArticleTitle = false;

				}

				if (ArticleAbs) {

					String line = new String(ch, start, length).toLowerCase().replace(".", "");
					String[] words = line.split("\\s+");
					wordCount += words.length;

					for (String word : words) {

						if (wL.containsKey(word)) {

							anzahl = wL.get(word) + 1;
							wL.put(word, anzahl);

						} else {

							wL.put(word, 1);

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

						if (year.substring(4, 5).equals("-") == false) {

							year = year.substring(0, 4);

						}
					}

					if (year == "")
						return;
					int anzahl;

					// Journal: Jahreszahlen

					yearCount += 1;

					if (yL.containsKey(year)) {

						anzahl = yL.get(year) + 1;
						yL.put(year, anzahl);

					} else {

						yL.put(year, 1);

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

					int anzahl;

					String title = new String(ch, start, length);
					titleCount += 1;

					if (tL.containsKey(title)) {

						anzahl = tL.get(title) + 1;
						tL.put(title, anzahl);

					} else {

						tL.put(title, 1);

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

					int anzahl = 0;

					if (qualifierList.isEmpty()) {

						meshCount += 1;

						if (mL.containsKey(descriptor)) {

							anzahl = mL.get(descriptor) + 1;
							mL.put(descriptor, anzahl);

						} else {

							mL.put(descriptor, 1);

						}

					} else {

						for (String qual : qualifierList) {

							String mesh = descriptor + "//" + qual;
							meshCount += 1;

							if (mL.containsKey(mesh)) {

								anzahl = mL.get(mesh) + 1;
								mL.put(mesh, anzahl);

							} else {

								mL.put(mesh, 1);

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
