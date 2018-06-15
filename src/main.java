import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

public class Main {

	private static String goodreadsURL = "https://www.goodreads.com/book/title.xml?key=yXZIGleYDqexQC7C40PFg&title=";
	public static boolean include_subdirectories = true; //TODO change to false once working
	public String base_directory;
	public static BookList failedBooks = new BookList();
	public static BookList bookList;

	public static void main(String[] args) throws IOException {
		BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
		//	System.out.print("Base directory name: ");
		//String base_folder = input.readLine();
		String base_folder ="Learnings";

		//TODO Doesn't work
		//		System.out.print("Do you want to include subdirectories? (yes/no): ");
		//		String yes_no = input.readLine();
		//		if (Character.toLowerCase(yes_no.charAt(0)) == 'y') {
		//			include_subdirectories = true;
		//		}

			List<Book> files = Files.walk(Paths.get("/Users/Tucker/Documents/" + base_folder), FileVisitOption.FOLLOW_LINKS)
					.filter(p -> (p.toString().endsWith(".pdf") || p.toString().endsWith(".epub") || p.toString().endsWith(".mobi")))
					.map(fields -> 
					new Book(FilenameUtils.removeExtension(fields.getFileName().toString()), 
							fields.getParent().toString().substring(fields.getParent().toString().lastIndexOf('/') + 1).trim(),
							FilenameUtils.getPath(fields.toAbsolutePath().toString()),
							FilenameUtils.getExtension(fields.toAbsolutePath().toString())))
					.collect(Collectors.toList());

			bookList = new BookList(files);


		createGoodreadsThreads(bookList);

		if (failedBooks.getNumberOfBooks() > 0) {
			//System.out.println(failedBooks.toString());

			System.out.print(failedBooks.getNumberOfBooks() + " failed. Do you want to edit the filenames and try again? ");
			var editFiles = (char)input.read(); 
			var editFailedFiles = false;
			if (Character.toLowerCase(editFiles) == 'y') {
				editFailedFiles = true;
			}
			if (editFailedFiles) {
				editFailedFiles();
			}

			createGoodreadsThreads(failedBooks);

			System.out.println(failedBooks.toString());
		}
		input.close();
	}

	private static void createGoodreadsThreads(BookList bookList) { // change to generic
		Thread[] threads = new Thread[bookList.getNumberOfBooks()];
		PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
		cm.setMaxTotal(5); // increase max total connection to 20
		cm.setDefaultMaxPerRoute(5); // increase max connection per route to 20
		RequestConfig localConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build();
		CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(cm).build();

		for (int i = 0; i < threads.length; i++) {
			String title = bookList.getBook(i).getTitle();
			HttpGet httpgetGoodreads;
			try {
				httpgetGoodreads = new HttpGet(goodreadsURL + URLEncoder.encode(title.replaceAll("/y+|y+/",""), "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				httpgetGoodreads = new HttpGet(goodreadsURL + title);
			}
			httpgetGoodreads.setConfig(localConfig);
			threads[i] = new Thread(new GetGoodreadsTask(httpClient, httpgetGoodreads, bookList.getBook(i)));
			threads[i].start();
		}
		// wait for all the threads to finish
		for (int i = 0; i < threads.length; i++) {
			try {
				threads[i].join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private static void editFailedFiles() throws IOException {
		BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
		for (var i = 0; i < failedBooks.getNumberOfBooks(); i++) {
			Book failedBook = failedBooks.getBook(i);
			System.out.println("Edit title: " + failedBook.getTitle());
				String renamedTitle = input.readLine();
				if (renamedTitle.trim().length() > 0) {
					renameFile(failedBook, renamedTitle);
				}
		}
		input.close();
	
	}

	private static void renameFile(Book book, String renamedTitle) {
		File file = new File(book.getFullPath() + book.getExt());
		File newFile = new File(book.getPath() + renamedTitle + book.getExt());
		if (file.renameTo(newFile)){
			book.setTitle(renamedTitle);
			System.out.println("File rename success");
		} else {
			System.out.println("File rename failed");
		}
	}

}
