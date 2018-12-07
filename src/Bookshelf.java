import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

public class Bookshelf {
	private List<Book> books;
	private String title;
	private double mean_goodreads_votes, mean_amazon_votes, mean_total;
	private int min_goodreads_votes, min_amazon_votes;
	private final double percentage = .25;
	
	public Bookshelf() {
		this.books = new ArrayList<Book>();
	}
	
	public Bookshelf(List<Book> bookshelf, String title) {
		this.books = bookshelf;
		this.title = title;
	}
	
	public String getTitle() {
		return title;
	}

	public Book getBook(int index) {
		return books.get(index);
	}
	
	public void getBookByISBN(Map<String, Object> amazonData) {
		IntStream.range(0,books.size()).forEach(i-> {
			if (books.get(i).getISBN().equals(amazonData.get("isbn").toString())) {
				books.get(i).setAmazonAverageRating(Double.parseDouble(amazonData.get("rating").toString()));
				books.get(i).setAmazonRatingsCount(Integer.parseInt(amazonData.get("reviews").toString()));
				books.get(i).setTitle(amazonData.get("name").toString());
			}
		});
	}
	public void removeBook(Book book) {
		books.remove(book);
	}
	
	public void removeBookByIndex(int index) {
		books.remove(index);
	}

	public List<Book> getBooks() {
		return books;
	}

	public void addBook(Book book) {
		books.add(book);
	}

	public int getNumberOfBooks() {
		return books.size();
	}

	public double getMeanGoodreadsVotes() {
		return mean_goodreads_votes;
	}

	public void setMeanGoodreadsVotes(double d) {
		this.mean_goodreads_votes = d;
	}

	public double getMeanAmazonVotes() {
		return mean_amazon_votes;
	}

	public void setMeanAmazonVotes(double d) {
		this.mean_amazon_votes = d;
	}
	
	
	// TODO This stuff probably shouldn't be in bookshelf class. Maybe a calculator class?
	public List<Integer> getGoodreadsRatingsCountList() {
		List<Integer> votes = new ArrayList<>();
		for (Book book: books) {
			votes.add(book.getGoodreadsRatingsCount());
		}

		Collections.sort(votes);
		return votes;
	}

	public List<Integer> getAmazonRatingsCountList() {
		List<Integer> votes = new ArrayList<>();
		for (Book book: books) {
			votes.add(book.getAmazonRatingsCount());
		}

		Collections.sort(votes);
		return votes;
	}
	
	public List<Integer> getTotalRatingsCountList() {
		List<Integer> votes = new ArrayList<>();
		votes.addAll(getGoodreadsRatingsCountList());
		votes.addAll(getAmazonRatingsCountList()); 

		Collections.sort(votes);
		return votes;
	}
	public double getTotalMean() {
		return mean_total;
	}
	
	public void setTotalMean(double d) {
		this.mean_total = d;
	}
	
	public double getTotalMinVotes() {
		return trimmean(getTotalRatingsCountList());
	}
	public double getGoodreadsMinVotes() { // TODO When GR and Am run together, just add ratings when book is added
		return trimmean(getGoodreadsRatingsCountList());
	}
	
	public double getAmazonMinVotes() { // TODO When GR and Am run together, just add ratings when book is added
		return trimmean(getAmazonRatingsCountList());
	}
	
	private double trimmean(List<Integer> votes) {
		Collections.sort(votes);
		
		double removeAmount = (votes.size() * percentage) / 2;
		int roundDownNearedMultipleTwo = (int) (removeAmount >= 0 ? (removeAmount / 2) * 2 : ((removeAmount - 2 + 1) / 2) * 2);
		return getMean(votes.subList(roundDownNearedMultipleTwo, votes.size() - roundDownNearedMultipleTwo));
	}
	
	private double getMean(List<Integer> array) {
		double sum = 0;
	    for (int i = 0; i < array.size(); i++) {
	        sum += array.get(i);
	    }
	    return sum / array.size();
	}

	@Override
	public String toString() {
		return "bookshelf [books=" + books.toString() + "]";
	}
}