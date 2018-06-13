 import java.util.List;
 
 public class Books {
   private Book[] Book;
   private List<Book> books;
 
   public List<Book> getBooks() {
       return books;
   }
 
   public void addBook(Book book) {
       books.add(book);
   }
 }