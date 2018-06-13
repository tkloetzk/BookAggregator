import java.nio.file.Path;

import javax.xml.bind.annotation.XmlElement;

public class Book {

	private String title, path, category, ext;
	private double ratings_count, average_rating;
	
	public Book(String title, String category, String path, String ext) {
		this.title = title;
		this.path = path;
		this.category = category;
		this.ext = ext;
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

	
	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getPath() {
		return "/" + path + "/";
	}
	public String getFullPath() {
		return "/" + path + "/" + title;
	}

	public String getExt() {
		return "." + ext;
	}
	
	public String getCategory() {
		return category;
	}
}
