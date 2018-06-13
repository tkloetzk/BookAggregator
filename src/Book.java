import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

public class Book {

	private String title;
	private double ratings_count, average_rating;
	
	public Book(String title, double ratings_count, double average_rating) {
		this.title = title;
		this.ratings_count = ratings_count;
		this.average_rating = average_rating;
	}
	@XmlElement
	public double getRatings_count() {
		return ratings_count;
	}

	public void setRatings_count(double ratings_count) {
		this.ratings_count = ratings_count;
	}

	@XmlElement
	public double getAverage_rating() {
		return average_rating;
	}

	public void setAverage_rating(double average_rating) {
		this.average_rating = average_rating;
	}

	@XmlElement
	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
}
