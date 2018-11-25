package service;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class index_parser {

	// Key = Word, Jahr, Descriptor || Key = PMID, Value = Position
	public static HashMap<String, HashMap<Integer, ArrayList<Integer>>> titleWL = new HashMap<String, HashMap<Integer, ArrayList<Integer>>>();
	public static HashMap<String, HashMap<Integer, ArrayList<Integer>>> abstractWL = new HashMap<String, HashMap<Integer, ArrayList<Integer>>>();
	public static HashMap<String, ArrayList<Integer>> yearL = new HashMap<String, ArrayList<Integer>>();
	public static HashMap<String, HashMap<Integer, ArrayList<Integer>>> meshWL = new HashMap<String, HashMap<Integer, ArrayList<Integer>>>();

	public static Path pfad;

	public index_parser(Path get_path) {

		pfad = get_path.toAbsolutePath();

	}

	public static void main(String args[]) {

		File directory = new File(args[0]);
		File[] files = directory.listFiles();

		analyseTitles titles = new analyseTitles(files);
		analyseAbs abs = new analyseAbs(files);
		analyseYears years = new analyseYears(files);
		analyseMesh mesh = new analyseMesh(files);

		Thread t1 = new Thread(titles);
		Thread t2 = new Thread(abs);
		Thread t3 = new Thread(years);
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

		System.out.println(abstractWL.size());
		System.out.println(titleWL.size());
		System.out.println(yearL.size());
		System.out.println(meshWL.size());

		System.out.println("finished");

	}

	public void run() {

		File directory = new File(pfad.toString());
		File[] files = directory.listFiles();

		analyseTitles titles = new analyseTitles(files);
		analyseAbs abs = new analyseAbs(files);
		analyseYears years = new analyseYears(files);
		analyseMesh mesh = new analyseMesh(files);

		Thread t1 = new Thread(titles);
		Thread t2 = new Thread(abs);
		Thread t3 = new Thread(years);
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

	}

	// Klasse zum Parsen der Titel nach Wörter
	public static class analyseTitles implements Runnable {

		public static File[] paths;

		// Constructor
		public analyseTitles(File[] files) {

			paths = files;

		}

		// RUN
		public void run() {

			try {

				SAXParserFactory factory = SAXParserFactory.newInstance();
				SAXParser saxParser = factory.newSAXParser();
				UserHandler uh = new UserHandler();

				for (File datei : paths) {
					System.out.println("Parse Title nach Wörter: " + datei.getName());
					saxParser.parse(datei.getAbsolutePath(), uh);
				}

			} catch (Exception e) {
				e.printStackTrace();

			}

		}

		class UserHandler extends DefaultHandler {

			boolean ArticleTitle = false;
			boolean ArticleID = false;
			int pmid = 0;
			int index = 0;

			@Override
			public void startElement(String uri, String localName, String qName, Attributes attributes)
					throws SAXException {

				if (qName == "PMID") {
					ArticleID = true;
				}

				if (qName == "ArticleTitle") {
					ArticleTitle = true;
				}

			}

			@Override
			public void endElement(String uri, String localName, String qName) throws SAXException {

			}

			@Override
			public void characters(char ch[], int start, int length) throws SAXException {

				if (ArticleID) {

					pmid = Integer.parseInt(new String(ch, start, length));
					ArticleID = false;

				}

				if (ArticleTitle) {

					String line = new String(ch, start, length).toLowerCase();
					String[] tokens = line.split("[\\s+.,:?!]+");

					for (String token : tokens) {

						if (!token.equals("")) {

							index += 1;

							if (titleWL.containsKey(token)) {

								HashMap<Integer, ArrayList<Integer>> hm = titleWL.get(token);

								if (hm.containsKey(pmid)) {

									hm.get(pmid).add(index);

								} else {

									ArrayList<Integer> pos = new ArrayList<Integer>();
									pos.add(index);
									hm.put(pmid, pos);

								}

							} else {

								HashMap<Integer, ArrayList<Integer>> hm = new HashMap<Integer, ArrayList<Integer>>();
								ArrayList<Integer> pos = new ArrayList<Integer>();
								pos.add(index);
								hm.put(pmid, pos);
								titleWL.put(token, hm);

							}
						}

					}

					ArticleTitle = false;
					index = 0;

				}

			}

		}

	}

	// Klasse zum Parsen der Abstracts nach Wörter
	public static class analyseAbs implements Runnable {

		public static File[] paths;

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
					System.out.println("Parse Abstracts nach Wörter: " + datei.getName());
					saxParser.parse(datei.getAbsolutePath(), uh);
				}

			} catch (Exception e) {
				e.printStackTrace();

			}

		}

		class UserHandler extends DefaultHandler {

			boolean ArticleAbs = false;
			boolean ArticleID = false;
			int pmid = 0;
			int index = 0;

			@Override
			public void startElement(String uri, String localName, String qName, Attributes attributes)
					throws SAXException {

				if (qName == "PMID") {
					ArticleID = true;
				}

				if (qName == "Abstract") {
					ArticleAbs = true;
				}

			}

			@Override
			public void endElement(String uri, String localName, String qName) throws SAXException {

				if (qName == "Abstract") {
					ArticleAbs = false;
					index = 0;
				}

			}

			@Override
			public void characters(char ch[], int start, int length) throws SAXException {

				if (ArticleID) {

					pmid = Integer.parseInt(new String(ch, start, length));
					ArticleID = false;

				}

				if (ArticleAbs) {

					String line = new String(ch, start, length).toLowerCase();
					String[] tokens = line.split("[\\s+.,:?!]+");

					for (String token : tokens) {

						index += 1;

						if (!token.equals("")) {

							if (abstractWL.containsKey(token)) {

								HashMap<Integer, ArrayList<Integer>> hm = abstractWL.get(token);

								if (hm.containsKey(pmid)) {

									hm.get(pmid).add(index);

								} else {

									ArrayList<Integer> pos = new ArrayList<Integer>();
									pos.add(index);
									hm.put(pmid, pos);

								}

							} else {

								HashMap<Integer, ArrayList<Integer>> hm = new HashMap<Integer, ArrayList<Integer>>();
								ArrayList<Integer> pos = new ArrayList<Integer>();
								pos.add(index);
								hm.put(pmid, pos);
								abstractWL.put(token, hm);

							}
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
			boolean ArticleID = false;
			int pmid = 0;

			@Override
			public void startElement(String uri, String localName, String qName, Attributes attributes)
					throws SAXException {

				if (qName == "PMID") {
					ArticleID = true;
				}
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

				if (ArticleID) {
					pmid = Integer.parseInt(new String(ch, start, length));
					ArticleID = false;
				}

				if (found == true && yearormedline == true) {

					String year = new String(ch, start, length);

					if (year.length() > 4) {

						if (year.substring(4, 5).equals("-") == false) {

							year = year.substring(0, 4);

							if (yearL.containsKey(year)) {

								yearL.get(year).add(pmid);

							} else {

								ArrayList<Integer> pmids = new ArrayList<Integer>();
								pmids.add(pmid);
								yearL.put(year, pmids);
							}

						} else {

							String[] years = year.split("-");

							for (String y : years) {

								if (yearL.containsKey(y)) {

									ArrayList<Integer> pmids = yearL.get(y);
									pmids.add(pmid);

								} else {

									ArrayList<Integer> pmids = new ArrayList<Integer>();
									pmids.add(pmid);
									yearL.put(y, pmids);

								}

							}

						}

					} else {

						if (yearL.containsKey(year)) {

							yearL.get(year).add(pmid);

						} else {

							ArrayList<Integer> pmids = new ArrayList<Integer>();
							pmids.add(pmid);
							yearL.put(year, pmids);

						}

					}

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

			boolean dName = false;
			boolean ArticleID = false;
			String descriptor;
			int pmid = 0;
			int index = 0;

			@Override
			public void startElement(String uri, String localName, String qName, Attributes attributes)
					throws SAXException {

				if (qName == "PMID") {
					ArticleID = true;
				}
				
				if (qName == "MeshHeading") {
					dName = true;
				}


			}

			@Override
			public void endElement(String uri, String localName, String qName) throws SAXException {

				if (qName == "DescriptorName") {
	
					dName = false;
					index = 0;

				}
				
				
			}

			@Override
			public void characters(char ch[], int start, int length) throws SAXException {

				if (ArticleID) {
					pmid = Integer.parseInt(new String(ch, start, length));
					ArticleID = false;
				}

				if (dName) {

					descriptor = new String(ch, start, length).toLowerCase();
					String[] tokens = descriptor.split("[\\s+.,:?!]+");
					
					for (String token : tokens) {
						
						index += 1;
						
						if (meshWL.containsKey(token)) {
						
							HashMap<Integer, ArrayList<Integer>> hm = meshWL.get(token);

							if (hm.containsKey(pmid)) {
								
								hm.get(pmid).add(index);
								
							}
							else {
								
								ArrayList<Integer> pos = new ArrayList<Integer>();
								pos.add(index);
								hm.put(pmid, pos);
								
							}
							

						} else {

							HashMap<Integer, ArrayList<Integer>> hm = new HashMap<Integer, ArrayList<Integer>>();
							ArrayList<Integer> pos = new ArrayList<Integer>();
							pos.add(index);
							hm.put(pmid, pos);
							meshWL.put(token, hm);

						}
						
					}
					
				}

			}

		}

	}

}
