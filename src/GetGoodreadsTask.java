import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

public class GetGoodreadsTask implements Runnable {
	private final CloseableHttpClient httpClient;
	private final HttpContext context;
	private final HttpGet httpget;
	private final String bookTitle;
	private Book book;
	
	public GetGoodreadsTask(CloseableHttpClient httpClient, HttpGet httpget, String title) {
		this.httpClient = httpClient;
		this.context = HttpClientContext.create();
		this.httpget = httpget;
		this.bookTitle = title;
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
			String average_rating = doc.getElementsByTagName("average_rating").item(0).getTextContent();
			String ratings_count = doc.getElementsByTagName("ratings_count").item(0).getTextContent();
			//System.out.println(title + " has " + ratings_count + " ratings with an average rating of " + average_rating);
			saveBook(title, average_rating, ratings_count);
		} catch (NullPointerException e) {
			System.out.println("Failed to get title: " + bookTitle);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void saveBook(String title, String average_rating, String ratings_count) {
		//book = new Book(title, Double.parseDouble(ratings_count), Double.parseDouble(average_rating));
	//	books.addBook(book);
	}
	
	public Book getBook() {
		return book;
	}

}
