import javax.xml.bind.annotation.XmlElement;

public class Book {

	private String title, goodreadsTitle, path, category, ext, isbn;
	private double goodreadsRatingsCount, goodreadsAverageRating;

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
	public double getGoodreadsRatingsCount() {
		return goodreadsRatingsCount;
	}

	public void setGoodreadsRatingsCount(double ratings_count) {
		this.goodreadsRatingsCount = ratings_count;
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
		this.goodreadsAverageRating = average_rating;
	}

}
