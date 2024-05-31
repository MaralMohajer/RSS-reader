import org.jsoup.nodes.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.jsoup.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Scanner;

public class RSSReader {
    private static final int MAX_ITEMS = 5;

    //html ->title
    public static String extractPageTitle(String html)
    {
        try {
             Document doc = Jsoup.parse(html);
             return doc.select("title").first().text();
        }
        catch (Exception e) {
            return "Error: no title tag found in page source!";
        }
    }


    //rss -> content
    public static void retrieveRssContent(String rssUrl)
    {
        try {
             String rssXml = fetchPageSource(rssUrl);
             DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
             DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
             StringBuilder xmlStringBuilder = new StringBuilder();
             xmlStringBuilder.append(rssXml);
             ByteArrayInputStream input = new ByteArrayInputStream(xmlStringBuilder.toString().getBytes("UTF-8"));
             org.w3c.dom.Document doc = documentBuilder.parse(input);
             NodeList itemNodes = doc.getElementsByTagName("item");
             for (int i = 0; i < MAX_ITEMS; ++i) {
                 Node itemNode = itemNodes.item(i);
                 if (itemNode.getNodeType() == Node.ELEMENT_NODE) {
                     Element element = (Element) itemNode;
                     System.out.println("Title: " + element.getElementsByTagName("title").item(0).getTextContent());
                     System.out.println("Link: " + element.getElementsByTagName("link").item(0).getTextContent());
                     System.out.println("Description: " + element.getElementsByTagName("description").item(0).getTextContent());
                 }
             }
        }
        catch (Exception e) {
             System.out.println("Error in retrieving RSS content for " + rssUrl + ": " + e.getMessage());
        }
    }


    //url -> rss url
    public static String extractRssUrl(String url) throws IOException {
        Document doc = Jsoup.connect(url).get();
        return doc.select("[type='application/rss+xml']").attr("abs:href");
    }

    //  url -> html source
    public static String fetchPageSource(String urlString) throws Exception {
        URI uri = new URI(urlString);
        URL url = uri.toURL();
        URLConnection urlConnection = url.openConnection();
        urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML , like Gecko) Chrome/108.0.0.0 Safari/537.36");
        return toString(urlConnection.getInputStream());
    }

    private static String toString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream , "UTF-8"));
        String inputLine;
        StringBuilder stringBuilder = new StringBuilder();
        while ((inputLine = bufferedReader.readLine()) != null)
            stringBuilder.append(inputLine);
        return stringBuilder.toString();
    }


    static boolean flag = true;


    private static ArrayList<String> fileLines() throws IOException {
        FileReader URLFileReader = new FileReader("RSS.txt");
        BufferedReader reader = new BufferedReader(URLFileReader);

        String line;
        ArrayList<String> fileLines = new ArrayList<String>();

        while ((line=reader.readLine()) != null)
        {
            fileLines.add(line);
        }

        reader.close();
        return fileLines;
    }

    public static void showUpdate() throws IOException {
        FileReader URLFileReader = new FileReader("RSS.txt");
        BufferedReader reader = new BufferedReader(URLFileReader);

        Scanner scanner = new Scanner(System.in);
        int counter = 0;
        ArrayList<String> fileLines = fileLines();
        String[] splitLine;
        System.out.println("[0]" + "All websites");

        for (int i = 0; i < fileLines.size(); i++) {
            counter++;
            splitLine = fileLines.get(i).split(";");
            System.out.println("[" + counter + "]" + splitLine[0]);
        }

        System.out.println("enter -1 to return");

        String[] spl;
        int wantedWebsite = scanner.nextInt();
        if (wantedWebsite == 0) {
            for (int i = 0; i < counter; i++) {
                spl = fileLines.get(i).split(";");
                retrieveRssContent(extractRssUrl(spl[1]));
            }
        }
        else if (wantedWebsite == -1) {
            flag = false;
        }
        else if (wantedWebsite > 0 || wantedWebsite <= counter) {
            spl = fileLines.get(wantedWebsite - 1).split(";");
            retrieveRssContent(extractRssUrl(spl[1]));
        }
        else
        {
            System.out.println("yor number should be between 1 & " + counter);
        }
        reader.close();
    }

    public static void addURL() throws Exception {
        File file = new File("RSS.txt");
        FileWriter URLFileWriter = new FileWriter(file, true);
        BufferedWriter writer = new BufferedWriter(URLFileWriter);

        FileReader fileReader = new FileReader(file);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        Scanner scanner = new Scanner(System.in);
        System.out.println("please enter website URL to add");
        String URL = scanner.nextLine();
        ArrayList<String> fileLines = new ArrayList<>();
        String line;
        while ((line = bufferedReader.readLine()) != null)
        {
            fileLines.add(line);
        }

        String[] lineSplit;

        boolean f = true;
        for (int i = 0; i < fileLines.size(); i++) {
            lineSplit = fileLines.get(i).split(";");
            if (lineSplit[1].equals(URL)){
                f = false;
            }
        }

        if (f){
            writer.write(extractPageTitle(fetchPageSource(URL)) +  ";" + URL + ";" + extractRssUrl(URL) + "\n");
            System.out.println("Added " + URL + " successfully");
        }
        else
            System.out.println(URL + " already exists");

        writer.close();
    }

    public static void removeUrl() throws Exception {
        Scanner scanner = new Scanner(System.in);
//        File file = new File("RSS.txt");
        FileReader fileReader = new FileReader("RSS.txt");
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        String line;
        ArrayList <String> list = new ArrayList<>();

        System.out.println("please enter website URL to remove");
        String removedURL = scanner.next();

        while ((line = bufferedReader.readLine()) != null)
        {
            if (!line.equals(removedURL)){
                list.add(line);
            }
        }
        boolean f = true;

        FileWriter fileWriter = new FileWriter("RSS.txt");
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        for (int i = 0; i < list.size(); i++)
        {
            bufferedWriter.write(list.get(i));
            f = false;
        }
        if (f == false){
            System.out.println("remmoved " + removedURL + " successfully");
        }
        else
            System.out.println("Couldn't find " + removedURL );

    }

    public static void main(String[] args) throws Exception {
        File file = new File("RSS.txt");
        System.out.println("welcome to RSS reader");
        Scanner scan =  new Scanner(System.in);
        int clientNumber = 0;
        while (clientNumber != 4){
            System.out.println("type a valid number for your desired action");
            System.out.println("[1] Show updates");
            System.out.println("[2] Add URL");
            System.out.println("[3] Remove URL");
            System.out.println("[4] EXIT");

            clientNumber = scan.nextInt();

            if (clientNumber > 4 || clientNumber < 1){
                System.out.println("your number should be between 1 & 4");
            }

            else {
                switch (clientNumber){
                    case 1 :{
                        showUpdate();
                        if (!flag){
                            break;
                        }
                        break;
                    }
                    case 2: {
                        addURL();
                        break;
                    }
                    case 3:{
                        removeUrl();
                        break;

                    }
                }
            }
        }
    }
}
