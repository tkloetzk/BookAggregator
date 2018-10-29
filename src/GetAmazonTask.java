import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.NumberFormat;
import java.text.ParseException;

import org.jsoup.Jsoup;

public class GetAmazonTask implements Runnable {
	private Book book;
	private NumberFormat nf = NumberFormat.getInstance();

	public GetAmazonTask(Book book) {
		this.book = book;
	}

	
	@Override
	public void run() {
		//String url = "http://www.amazon.com/product-reviews/" + book.getISBN();
		String url;
		try {
			// url = "https://www.amazon.com/s/ref=nb_sb_ss_c_1_12?field-keywords=" + book.getISBN();
			 url = "https://www.amazon.com/s/ref=nb_sb_ss_c_1_12?field-keywords=" + URLEncoder.encode(book.getTitle(), java.nio.charset.StandardCharsets.UTF_8.toString());
			// Get the max number of review pages;
			org.jsoup.nodes.Document reviewpage1 = null;
			reviewpage1 = Jsoup.connect(url).timeout(10*1000).get();	
			Double rating = nf.parse(reviewpage1.select(".a-icon-star > span").text().split(" out")[0]).doubleValue();
			int reviewCount = nf.parse(reviewpage1.select("div:nth-child(2) > div.a-column.a-span5.a-span-last > div > a").text()).intValue();
		//	System.out.println(book.getTitle() + " - " + book.getISBN() + " has a rating of " + rating + " with " + reviewCount + " reviews");
			book.setAmazonAverageRating(rating);
			book.setAmazonRatingsCount(reviewCount);

		} catch (UnsupportedEncodingException e1) {
			System.out.println("Error " + e1.getMessage());
		}
		catch (Exception e) { // May success but no rating. Like Tips & Traps
			System.out.println(book.getTitle() + " - " + book.getISBN() + " Exception" + " " + e.toString());
		}
	}

}
