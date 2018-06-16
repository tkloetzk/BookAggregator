import org.jsoup.Jsoup;

public class GetAmazonTask implements Runnable {
	private Book book;

	public GetAmazonTask(Book book) {
		this.book = book;
	}

	
	@Override
	public void run() {
		String url = "http://www.amazon.com/product-reviews/" + book.getISBN();
		try {
			// Get the max number of review pages;
			org.jsoup.nodes.Document reviewpage1 = null;
			reviewpage1 = Jsoup.connect(url).timeout(10*1000).get();
			Double rating = Double.parseDouble(reviewpage1.select(".arp-rating-out-of-text").text().substring(0, 3));
			int reviewCount = Integer.parseInt(reviewpage1.select(".totalReviewCount").text());
		//	System.out.println(book.getTitle() + " - " + book.getISBN() + " has a rating of " + rating + " with " + reviewCount + " reviews");
			book.setAmazonAverageRating(rating);
			book.setAmazonRatingsCount(reviewCount);

		}
		catch (Exception e) { // TODO May success but no rating. Like Tips & Traps
			System.out.println(book.getTitle() + " - " + book.getISBN() + " Exception" + " " + e.toString());
		}
		
	}

}
