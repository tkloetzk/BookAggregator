import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.jsoup.Jsoup;

public class GetAmazonTask implements Runnable {
	private Book book;

	public GetAmazonTask(Book book) {
		this.book = book;
	}

	
	@Override
	public void run() {
		//String url = "http://www.amazon.com/product-reviews/" + book.getISBN();
		String url;
		try {
			url = "http://www.amazon.com/product-reviews/" + URLEncoder.encode(book.getTitle(), java.nio.charset.StandardCharsets.UTF_8.toString());
			// Get the max number of review pages;
			org.jsoup.nodes.Document reviewpage1 = null;
			reviewpage1 = Jsoup.connect(url).timeout(10*1000).get();			
			Double rating = Double.parseDouble(reviewpage1.select(".a-icon.a-icon-star>span.a-icon-alt").text().split(" out")[0]);
			int reviewCount = Integer.parseInt(reviewpage1.select("div.a-row.a-spacing-mini>a[href*=\"" + book.getISBN() + "\"]").text());
		//	System.out.println(book.getTitle() + " - " + book.getISBN() + " has a rating of " + rating + " with " + reviewCount + " reviews");
			book.setAmazonAverageRating(rating);
			book.setAmazonRatingsCount(reviewCount);

		} catch (UnsupportedEncodingException e1) {
			System.out.println("Error " + e1.getMessage());
		}
		catch (Exception e) { // TODO May success but no rating. Like Tips & Traps
			System.out.println(book.getTitle() + " - " + book.getISBN() + " Exception" + " " + e.toString());
		}
		
	}

}
