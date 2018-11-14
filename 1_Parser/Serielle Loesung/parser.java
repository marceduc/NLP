
package test;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.text.NumberFormat;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

public class parser {

	public static void main(String[] args) {

		long time = System.currentTimeMillis();

		File path = new File(args[0]);
		analyze(path);
		
		System.out.println("-------------------------");
		System.out.println(System.currentTimeMillis() - time + "ms");

	}

	public static void analyze(File ordner) {

		File[] files = ordner.listFiles();

		List<String> wordList = new ArrayList<String>();
		List<String> yearList = new ArrayList<String>();
		List<String> titleList = new ArrayList<String>();
		List<String> meshList = new ArrayList<String>();

		List<Integer> anzahlWordList = new ArrayList<Integer>();
		List<Integer> anzahlYearList = new ArrayList<Integer>();
		List<Integer> anzahlTitleList = new ArrayList<Integer>();
		List<Integer> anzahlMeshList = new ArrayList<Integer>();

		int articleCount = 0;
		double wordCount = 0;
		double yearCount = 0;
		double titleCount = 0;
		double meshCount = 0;
		
		int ind;
		int anzahl;
		double top = 0;
		String topP;
		String wordE;
		String anzahlP;
		
		// Formatierer
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(2);
		
		try {

			for (File datei : files) {

				System.out.println("Analysiere: " + datei.getAbsolutePath());
				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				Document doc = dBuilder.parse(datei.getAbsolutePath());
				doc.getDocumentElement().normalize();

				// Parsen nach gesuchten Elementem
				NodeList artikel = doc.getElementsByTagName("PubmedArticle");
				NodeList artikel_titel = doc.getElementsByTagName("ArticleTitle");
				NodeList artikel_abstracts = doc.getElementsByTagName("AbstractText");
				NodeList pubdates = doc.getElementsByTagName("PubDate");
				NodeList jtitles = doc.getElementsByTagName("Title");
				NodeList meshes = doc.getElementsByTagName("MeshHeading");

				// Anzahl der Artikel
				articleCount += artikel.getLength();

				// ArticleText und ArticleAbstract: Wörter finden und zählen

				String line;
				String[] words;

				for (int i = 0; i < artikel_titel.getLength(); i++) {

					line = artikel_titel.item(i).getTextContent().toLowerCase();
					words = line.split("\\s+");
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
					
					System.out.println("ArticleTitle: " + (i+1) +  "/" + artikel_titel.getLength()); 
				}

				for (int i = 0; i < artikel_abstracts.getLength(); i++) {

					line = artikel_abstracts.item(i).getTextContent().toLowerCase();
					words = line.split("\\s+");
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
					
					System.out.println("ArticleAbstract: " + (i+1) +  "/" + artikel_abstracts.getLength());

				}
				
				
				//Journal: Jahreszahlen
				
				NodeList childs;
				String year;
				
				for(int i = 0; i < pubdates.getLength(); i++) {
					
					childs = pubdates.item(i).getChildNodes();
					year = childs.item(1).getTextContent();
					
					if(year != "") {
						
						yearCount += 1;
						
						if(yearList.contains(year)) {
							
							ind = yearList.indexOf(year);
							anzahl = anzahlYearList.get(ind) + 1;
							anzahlYearList.set(ind, anzahl);
							
						}
						else {
							
							yearList.add(year);
							anzahlYearList.add(1);
							
						}
						
					}
					
					System.out.println("PubDate: " + (i+1) +  "/" + pubdates.getLength());
				}
				
				
				//Journal: Titel
				
				for(int i = 0; i < jtitles.getLength(); i++) {
					
					line = jtitles.item(i).getTextContent();
					titleCount += 1;
					
					if (titleList.contains(line)) {
						
						ind = titleList.indexOf(line);
						anzahl = anzahlTitleList.get(ind) + 1;
						anzahlTitleList.set(ind, anzahl);
						
					}
					else {
						
						titleList.add(line);
						anzahlTitleList.add(1);
						
					}
					
					
					System.out.println("Journal: " + (i+1) +  "/" + jtitles.getLength());
				}
				
				
				//Mesh
				
				String descriptor;
				
				meshCount += meshes.getLength();
				for(int i = 0; i < meshes.getLength(); i++) {
					
					childs = meshes.item(i).getChildNodes();
					descriptor = childs.item(1).getTextContent();
					if(childs.getLength() == 3) {
						
						if(meshList.contains(descriptor)) {
							
							ind = meshList.indexOf(descriptor);
							anzahl = anzahlMeshList.get(ind) + 1;
							anzahlMeshList.set(ind, anzahl);
							
						}
						else {
							
							meshList.add(descriptor);
							anzahlMeshList.add(1);
						}
						
						
					}
					else {
						
						for(int x = 3; x < childs.getLength(); x += 2) {
							
							if(meshList.contains(descriptor + "//" + childs.item(x).getTextContent())) {
								
								ind = meshList.indexOf(descriptor + "//" + childs.item(x).getTextContent());
								anzahl = anzahlMeshList.get(ind) + 1;
								anzahlMeshList.set(ind, anzahl);
								
							}
							else {
								
								meshList.add(descriptor + "//" + childs.item(x).getTextContent());
								anzahlMeshList.add(1);
							}
							
						}
						
					}
					
					System.out.println("MesH : " + (i+1) +  "/" + (int) meshCount);
					
				}

			}
			
			
			System.out.println("");
			System.out.println("Artikel: " + articleCount);
			System.out.println("");
			
			//Ausgabe Wörter
			System.out.println("Wörter: " + (int) wordCount + " (" + wordList.size() + " distinct)");
			
			for(int i = 0; i < 50; i++) {
				
				if(wordList.isEmpty()) break;
				ind = anzahlWordList.indexOf(Collections.max(anzahlWordList));
				wordE = wordList.get(ind);
				anzahl = anzahlWordList.get(ind);
				top += anzahl;
				anzahlP = " (" + nf.format(anzahl/wordCount*100) + " %)";
				
				System.out.println(wordE + " : " + anzahl + anzahlP);
				
				anzahlWordList.remove(ind);
				wordList.remove(ind);
				
			}
			
			topP = "Top50 : " + (int) top + " (" + nf.format(top / wordCount * 100) + " %)";
			System.out.println(topP);
			top = 0;
			System.out.println("");
			
			//Ausgabe Jahreszahlen
			System.out.println("Jahresangaben: " + (int) yearCount + " (" + yearList.size() + " distinct)");
			
			for(int i = 0; i < 10; i++) {
				
				if (yearList.isEmpty()) break;
				ind = anzahlYearList.indexOf(Collections.max(anzahlYearList));
				wordE = yearList.get(ind);
				anzahl = anzahlYearList.get(ind);
				top += anzahl;
				anzahlP = " (" + nf.format(anzahl/yearCount*100) + " %)";
				
				System.out.println(wordE + " : " + anzahl + anzahlP);
				
				anzahlYearList.remove(ind);
				yearList.remove(ind);
			}
			
			topP = "Top10 : " + (int) top + " (" + nf.format(top / yearCount * 100) + " %)";
			System.out.println(topP);
			top = 0;
			System.out.println("");
			
			//Ausgabe Journaltitel
			System.out.println("Journale: " + (int) titleCount + " (" + titleList.size() + " distinct)");
			
			for(int i = 0; i < 10; i++) {
				
				if (titleList.isEmpty()) break;
				ind = anzahlTitleList.indexOf(Collections.max(anzahlTitleList));
				wordE = titleList.get(ind);
				anzahl = anzahlTitleList.get(ind);
				top += anzahl;
				anzahlP = " (" + nf.format(anzahl/titleCount*100) + " %)";
				
				System.out.println(wordE + " : " + anzahl + anzahlP);
				
				anzahlTitleList.remove(ind);
				titleList.remove(ind);
				
			}
			
			topP = "Top10 : " + (int) top + " (" + nf.format(top / titleCount * 100) + " %)";
			System.out.println(topP);
			top = 0;
			System.out.println("");
			
			//Ausgabe MeSH
			System.out.println("MesH: " + (int) meshCount + " (" + meshList.size() + " distinct)");
			
			for(int i = 0; i < 10; i++) {
				
				if (meshList.isEmpty()) break;
				ind = anzahlMeshList.indexOf(Collections.max(anzahlMeshList));
				wordE = meshList.get(ind);
				anzahl = anzahlMeshList.get(ind);
				top += anzahl;
				anzahlP = " (" + nf.format(anzahl/meshCount*100) + " %)";
				
				System.out.println(wordE + " : " + anzahl + anzahlP);
				
				anzahlMeshList.remove(ind);
				meshList.remove(ind);
				
			}
			
			topP = "Top10 : " + (int) top + " (" + nf.format(top / meshCount * 100) + " %)";
			System.out.println(topP);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
