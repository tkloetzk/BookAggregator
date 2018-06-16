import javax.xml.bind.annotation.XmlElement;

public class Book {

	private String title, goodreadsTitle, path, category, ext, isbn;
	private double goodreadsAverageRating, amazonAverageRating;
	private int goodreadsRatingsCount, amazonRatingsCount;

	public Book(String title, String category, String path, String ext) {
		this.title = title;
		this.path = path;
		this.category = category;
		this.ext = ext;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String setGoodreadsTitle() {
		return goodreadsTitle;
	}

	public void setGoodreadsTitle(String title) {
		this.goodreadsTitle = title;
	}

	public String getPath() {
		return "/" + path + "/";
	}

	public String getFullPath() {
		return "/" + path + "/" + title;
	}

	@XmlElement
	public int getGoodreadsRatingsCount() {
		return goodreadsRatingsCount;
	}

	public void setGoodreadsRatingsCount(int ratings_count) {
		this.goodreadsRatingsCount = ratings_count;
	}

	public int getAmazonRatingsCount() {
		return amazonRatingsCount;
	}

	public void setAmazonRatingsCount(int ratings_count) {
		this.amazonRatingsCount = ratings_count;
	}
	
	public String getCategory() {
		return category;
	}

	public String getExt() {
		return "." + ext;
	}

	public String getISBN() {
		return isbn;
	}

	public void setISBN(String isbn) {
		this.isbn = isbn;
	}

	@XmlElement
	public double getGoodreadsAverageRating() {
		return goodreadsAverageRating;
	}

	public void setGoodreadsAverageRating(double average_rating) {
		this.goodreadsAverageRating = average_rating * 2;
	}
	
	public double getAmazonAverageRating() {
		return amazonAverageRating;
	}

	public void setAmazonAverageRating(double average_rating) {
		this.amazonAverageRating = average_rating * 2;
	}
	
	public int getTotalReviews() {
		return getGoodreadsRatingsCount() + getAmazonRatingsCount();
	}
	
	public double getTotalAverageRating() {
		return (getGoodreadsAverageRating() + getAmazonAverageRating()) / 2;
	}
	
	public double getTotalWeightedAverageRating() {
		return ((getGoodreadsRatingsCount() * getGoodreadsAverageRating()) + 
				(getAmazonRatingsCount() * getAmazonAverageRating())) / 
				getTotalReviews();
	}
}
