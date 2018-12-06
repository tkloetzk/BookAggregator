import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Main {

	private static final String goodreadsURL = "https://www.goodreads.com/book/title.xml?key=yXZIGleYDqexQC7C40PFg&title=";
	public static boolean include_subdirectories = true; // TODO change to false once working
	public static String base_directory, title;
	public static Bookshelf failedBooks = new Bookshelf();
	public static Bookshelf bookshelf;
	public static List<Book> files;
	private static boolean isbnsFromList;

	@SuppressWarnings("deprecation")
	public static void main(String[] args) throws IOException {
		BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("Enter 1 for ISBN List or 2 for pdf directors.");
		String answer = input.readLine();

		// TODO: Save file as directory name
		if (String.valueOf(answer).equals("2")) {
			isbnsFromList = false;
			System.out.print("Base directory name: "); // Move this out since both could require base folder
			String base_folder = input.readLine();
			title = base_folder;
			files = Files.walk(Paths.get("/Users/Tucker/Documents/" + base_folder), FileVisitOption.FOLLOW_LINKS)
					.filter(p -> (p.toString().endsWith(".pdf") || p.toString().endsWith(".epub")
							|| p.toString().endsWith(".mobi")))
					.map(fields -> new Book(FilenameUtils.removeExtension(fields.getFileName().toString()),
							fields.getParent().toString().substring(fields.getParent().toString().lastIndexOf('/') + 1)
									.trim(),
							FilenameUtils.getPath(fields.toAbsolutePath().toString()),
							FilenameUtils.getExtension(fields.toAbsolutePath().toString())))
					.collect(Collectors.toList());
		} else {
			isbnsFromList = true;
			// Location of base folder
			System.out.print("Excel name: ");
			String filename = input.readLine();
			title = filename;
			String excelFilePath = "/Users/Tucker/Downloads/" + filename + ".xlsx";
			FileInputStream inputStream = new FileInputStream(new File(excelFilePath));
			List<Book> listBooks = new ArrayList<>();

			Workbook workbook = new XSSFWorkbook(inputStream);
			Sheet firstSheet = workbook.getSheetAt(0);
			Iterator<Row> iterator = firstSheet.iterator();
			files = new ArrayList<>();

			while (iterator.hasNext()) {
				Row nextRow = iterator.next();
				Iterator<Cell> cellIterator = nextRow.cellIterator();
				Book isbnBook = new Book();
				while (cellIterator.hasNext()) {
					Cell nextCell = cellIterator.next();
					int columnIndex = nextCell.getColumnIndex();

					switch (columnIndex) {
					case 0:
						isbnBook.setISBN((String) getCellValue(nextCell));
						break;
					case 1:
						isbnBook.setCategory((String) getCellValue(nextCell));
						break;
					case 2:
						isbnBook.setCourse((String) getCellValue(nextCell));
						break;
					case 3:
						isbnBook.setSchool((String) getCellValue(nextCell));
						break;
					default:
						break;
					}
				}
				if (!(isbnBook == null || (isbnBook.getTitle() == null && isbnBook.getISBN() == null))) {
					files.add(isbnBook);

				}
			}

			workbook.close();
			inputStream.close();
		}

		// TODO Doesn't work
		// System.out.print("Do you want to include subdirectories? (yes/no): ");
		// String yes_no = input.readLine();
		// if (Character.toLowerCase(yes_no.charAt(0)) == 'y') {
		// include_subdirectories = true;
		// }

		bookshelf = new Bookshelf(files, title);

		createGoodreadsThreads(bookshelf);
		readAmazonFromDataJSON(bookshelf);
		// createAmazonThreads(bookshelf);

		if (failedBooks.getNumberOfBooks() > 0) {
			// System.out.println(failedBooks.toString());

			System.out.print(
					failedBooks.getNumberOfBooks() + " failed. Do you want to edit the filenames and try again? ");
			var editFiles = (char) input.read();
			if (Character.toLowerCase(editFiles) == 'y') {
				editFailedFiles();
				createGoodreadsThreads(failedBooks);
				readAmazonFromDataJSON(failedBooks);
				// createAmazonThreads(failedBooks);
			}
		}

		// TODO Mean and min are off. Too big
		int goodreadsVotes = 0, amazonVotes = 0, total = 0;
		for (var i = 0; i < bookshelf.getNumberOfBooks(); i++) {
			// for (Book book: bookshelf) { // TODO Iterator
			goodreadsVotes += bookshelf.getBook(i).getGoodreadsAverageRating();
			amazonVotes += bookshelf.getBook(i).getAmazonAverageRating();
			total += goodreadsVotes + amazonVotes;
			// total += goodreadsVotes;
		}

		bookshelf.setMeanGoodreadsVotes(goodreadsVotes / bookshelf.getNumberOfBooks());
		bookshelf.setMeanAmazonVotes(amazonVotes / bookshelf.getNumberOfBooks());
		// bookshelf.setTotalMean((total/2)/bookshelf.getNumberOfBooks());
		bookshelf.setTotalMean((total) / bookshelf.getNumberOfBooks());
		System.out.println(bookshelf.getTotalMean());

		System.out.println(" ...Finished");

		// System.out.print("Do you want to export csv? ");
		// var exportCSV = (char)input.read();
		// if (Character.toLowerCase(exportCSV) == 'y') {
		ExcelExporter excelExporter = new ExcelExporter(bookshelf);
		excelExporter.export();
		// }

		// TODO Save to database
		// System.out.println("Do you want to save to database?");
		// var dbSave = (char)input.read();
		// if (Character.toLowerCase(dbSave) == 'y') {
		// dbSave();
		// }
		//
		input.close();
	}

	@SuppressWarnings("deprecation")
	private static Object getCellValue(Cell cell) {
		switch (cell.getCellType()) {
		case Cell.CELL_TYPE_STRING:
			return cell.getStringCellValue();

		case Cell.CELL_TYPE_BOOLEAN:
			return cell.getBooleanCellValue();

		case Cell.CELL_TYPE_NUMERIC:
			return cell.getNumericCellValue();
		}

		return null;
	}

	private static void createAmazonThreads(Bookshelf bookshelf) {

		System.out.println("reading from data.json");
		JSONParser jsonParser = new JSONParser();

		try (FileReader reader = new FileReader("data.json")) {
			Object obj = jsonParser.parse(reader);

			JSONArray amazonData = (JSONArray) obj;
			System.out.println(amazonData);

//			Thread[] threads = new Thread[amazonData.size()];
			for (int i = 0; i < amazonData.size(); i++) {
				parseAmazonData((JSONObject) amazonData.get(i));
//				threads[i] = new Thread(new GetAmazonTask((JSONObject) amazonData.get(i)));
//				threads[i].start();
			}

//			// wait for all the threads to finish
//			for (int i = 0; i < threads.length; i++) {
//				try {
//					threads[i].join();
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}

	}

	private static void createGoodreadsThreads(Bookshelf bookshelf) {
		Thread[] threads = new Thread[bookshelf.getNumberOfBooks()];
		PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
		cm.setMaxTotal(5); // increase max total connection to 20
		cm.setDefaultMaxPerRoute(5); // increase max connection per route to 20
		RequestConfig localConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build();
		CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(cm).build();

		for (int i = 0; i < threads.length; i++) {
			String title;
			if (bookshelf.getBook(i).getTitle() != null) {
				title = bookshelf.getBook(i).getTitle();
			} else {
				title = bookshelf.getBook(i).getISBN();
			}
			HttpGet httpgetGoodreads;
			try {
				httpgetGoodreads = new HttpGet(
						goodreadsURL + URLEncoder.encode(title.replaceAll("/y+|y+/", ""), "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				httpgetGoodreads = new HttpGet(goodreadsURL + title);
			}
			httpgetGoodreads.setConfig(localConfig);
			threads[i] = new Thread(new GetGoodreadsTask(httpClient, httpgetGoodreads, bookshelf.getBook(i)));
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

	private static void readAmazonFromDataJSON(Bookshelf bookshelf) {
		System.out.println("reading from data.json");
		JSONParser jsonParser = new JSONParser();

		try (FileReader reader = new FileReader("data.json")) {
			Object obj = jsonParser.parse(reader);

			JSONArray amazonData = (JSONArray) obj;
			System.out.println(amazonData);

			// Iterate over employee array
			amazonData.forEach(emp -> parseAmazonData((JSONObject) emp));

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}

	}

	private static void parseAmazonData(JSONObject book) {
		List amazonData = new ArrayList<>();
		// Get employee first name

		// Get employee last name
		String reviewCount = (String) book.get("REVIEW_COUNT");
		amazonData.add(reviewCount);
		System.out.println(reviewCount);

		// Get employee website name
		String ratingString = (String) book.get("RATING");
		String rating = ratingString.substring(0, ratingString.indexOf("o")).trim();
		amazonData.add(rating);
		System.out.println(rating);

		String name = (String) book.get("NAME");
		amazonData.add(name);
		System.out.println(name);

		String isbn = (String) book.get("ASIN");
		amazonData.add(isbn);
		System.out.println(isbn);

		// Add to amazon bookshelf or find it right now?
		// Create amazon book POJO, then update it
		bookshelf.getBookByISBN(isbn, amazonData);

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
		// input.close();

	}

	private static void renameFile(Book book, String renamedTitle) {
		File file = new File(book.getFullPath() + book.getExt());
		File newFile = new File(book.getPath() + renamedTitle + book.getExt());
		if (file.renameTo(newFile)) {
			book.setTitle(renamedTitle);
			System.out.println("File rename success");
		} else {
			System.out.println("File rename failed");
		}
	}

}
