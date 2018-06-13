import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BookList {

	private Book[] Book;
	private List<Book> books = new ArrayList<Book>();

	public Book getBook(int index) {
		return books.get(index);
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

	@Override
	public String toString() {
		return "BookList [Book=" + Arrays.toString(Book) + ", books=" + books + "]";
	}
}