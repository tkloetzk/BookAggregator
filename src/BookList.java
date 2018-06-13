import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BookList {

	private Book[] Book;
	private List<Book> books;
	
	public BookList() {
		this.books = new ArrayList<Book>();
	}
	
	public BookList(List<Book> bookList) {
		this.books = bookList;
	}

	public Book getBook(int index) {
		return books.get(index);
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

	@Override
	public String toString() {
		return "BookList [Book=" + Arrays.toString(Book) + ", books=" + books + "]";
	}

}