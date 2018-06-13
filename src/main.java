import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.FilenameUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

public class main {

	private static String goodreadsURL = "https://www.goodreads.com/book/title.xml?key=yXZIGleYDqexQC7C40PFg&title=";
	public static boolean include_subdirectories = true; //TODO change to false once working
	public String base_directory;
	public static List<Book> bookList;
	public static BookList failedTitles = new BookList();
	public static BookList succeededTitles = new BookList();
	
	public static void main(String[] args) {
		Scanner input = new Scanner(System.in);
	//	System.out.print("Base directory name: ");
		//String base_folder = input.nextLine();
		String base_folder ="Learnings";

		//TODO Doesn't work
//		System.out.print("Do you want to include subdirectories? (yes/no): ");
//		String yes_no = input.nextLine();
//		if (Character.toLowerCase(yes_no.charAt(0)) == 'y') {
//			include_subdirectories = true;
//		}

		try {
			bookList = Files.walk(Paths.get("/Users/Tucker/Documents/" + base_folder), FileVisitOption.FOLLOW_LINKS)
					.filter(p -> (p.toString().endsWith(".pdf") || p.toString().endsWith(".epub")))
					.map(fields -> 
						new Book(FilenameUtils.removeExtension(fields.getFileName().toString()), 
								fields.getParent().toString().substring(fields.getParent().toString().lastIndexOf('/') + 1).trim(),
								FilenameUtils.getPath(fields.toAbsolutePath().toString()),
								FilenameUtils.getExtension(fields.toAbsolutePath().toString())))
					.collect(Collectors.toList()); //TODO Save this to BookList

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
			
		createGoodreadsThreads(bookList);

		if (failedTitles.getNumberOfBooks() > 0) {
			System.out.println(failedTitles.toString());
			
			System.out.print(failedTitles.getNumberOfBooks() + " failed. Do you want to edit the filenames and try again? ");
			
			var editFailedFiles = false;
			if (Character.toLowerCase(input.next().charAt(0)) == 'y') {
				editFailedFiles = true;
			}
			if (editFailedFiles) {
				editFailedFiles();
			}
		//	createGoodreadsThreads(test);
			
			System.out.println(failedTitles.toString());
		}
		
		input.close();
		
	}

	private static void createGoodreadsThreads(List<Book> titles) { // change to generic
		Thread[] threads = new Thread[titles.size()];
		PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
		cm.setMaxTotal(5); // increase max total connection to 20
		cm.setDefaultMaxPerRoute(5); // increase max connection per route to 20
		RequestConfig localConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build();
		CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(cm).build();
		for (int i = 0; i < threads.length; i++) {
			String title = titles.get(i).getTitle();
			HttpGet httpgetGoodreads;
			try {
				httpgetGoodreads = new HttpGet(goodreadsURL + URLEncoder.encode(title, "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				httpgetGoodreads = new HttpGet(goodreadsURL + title);
			}
			httpgetGoodreads.setConfig(localConfig);
			threads[i] = new Thread(new main().new GetGoodreadsTask(httpClient, httpgetGoodreads, titles.get(i)));
			threads[i].start();
		}
		// wait for all the threads to finish
		for (int i = 0; i < threads.length; i++) {
			try {
				threads[i].join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private static void editFailedFiles() {
		try {
			BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
//			for (var i = 0; i < failedTitles.size(); i++) {
			for (var i = 0; i < 2; i++) {
				Book failedBook = failedTitles.getBook(i);
				System.out.println("Edit title: " + failedBook.getTitle());
				try {
					String renamedTitle = input.readLine();
					if (renamedTitle.trim().length() > 0) {
						File file = new File(failedBook.getFullPath() + failedBook.getExt());
				        File newFile = new File(failedBook.getPath() + renamedTitle + failedBook.getExt());
				        if (file.renameTo(newFile)){
				            System.out.println("File rename success");;
				        } else {
				            System.out.println("File rename failed");
				        }
					}
				} catch (IOException e) {
					System.out.println("Failed to read input");
					e.printStackTrace();
				}
				
	
			}
			input.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	class GetGoodreadsTask implements Runnable {
		private final CloseableHttpClient httpClient;
		private final HttpContext context;
		private final HttpGet httpget;
		private Book book;
		
		public GetGoodreadsTask(CloseableHttpClient httpClient, HttpGet httpget, Book book) {
			this.httpClient = httpClient;
			this.context = HttpClientContext.create();
			this.httpget = httpget;
			this.book = book;
		}

		@Override
		public void run() {
			try {
				CloseableHttpResponse response = httpClient.execute(httpget, context);
				try {
					HttpEntity httpEntity = response.getEntity();
					String res = EntityUtils.toString(httpEntity, "UTF-8");

					EntityUtils.consume(httpEntity);

					getDocument(res);

				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} finally {
					response.close();
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		private void getDocument(String docString) {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder;
			InputSource is;
			try {
				builder = factory.newDocumentBuilder();
				is = new InputSource(new StringReader(docString));
				Document doc = builder.parse(is);

				String title = doc.getElementsByTagName("title").item(0).getTextContent();
				var average_rating = doc.getElementsByTagName("average_rating").item(0).getTextContent();
				var ratings_count = doc.getElementsByTagName("ratings_count").item(0).getTextContent();
				System.out.println(title + " has " + ratings_count + " ratings with an average rating of " + average_rating);
				book.setAverage_rating(Double.parseDouble(average_rating));
				book.setRatings_count(Double.parseDouble(ratings_count));
				succeededTitles.addBook(book);
			} catch (NullPointerException e) {
				failedTitles.addBook(book);
				//System.out.println("Failed to get title: " + bookTitle);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

//		private void saveBook(String title, String average_rating, String ratings_count) {
//			book = new Book(title, Double.parseDouble(ratings_count), Double.parseDouble(average_rating));
//		//	books.addBook(book);
//		}
		
		public Book getBook() {
			return book;
		}

	}

}
