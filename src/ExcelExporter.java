import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelExporter {
	private Bookshelf bookshelf;
	
	public ExcelExporter(Bookshelf bookshelf) {
		this.bookshelf = bookshelf;
	}
	public void export() throws IOException {
		String[] headers =  {"Title", "Category", "GR Rating", "GR Review Count", "GR Adjusted Rating", "Am Rating", "Am Review Count", "Am Adjusted Rating", "Overall Adjusted Rating"};
		
		//Blank workbook
		Workbook wb = new HSSFWorkbook();
	    //Workbook wb = new XSSFWorkbook();
	    CreationHelper createHelper = wb.getCreationHelper();
	    Sheet sheet = wb.createSheet("new sheet");

        XSSFWorkbook s = new XSSFWorkbook();

    	var rownum = 0;
//		Row row = sheet.createRow(rownum);
//    	for (var i = 0; i < headers.length; i++) {
//    		row.createCell(i).setCellValue(headers[i]);
//    	}
    	for (var i = 1; i < bookshelf.getNumberOfBooks(); i++) {
    		Row row = sheet.createRow(rownum++);
    		Book book = bookshelf.getBook(i);
        	row.createCell(0).setCellValue(book.getTitle());
        	row.createCell(1).setCellValue(book.getCategory());
        	row.createCell(2).setCellValue(book.getGoodreadsAverageRating());
        	row.createCell(3).setCellValue(book.getGoodreadsRatingsCount());
        	row.createCell(4).setCellValue(
        			getAjustedRating(
        					book.getGoodreadsRatingsCount(), 
        					book.getGoodreadsAverageRating(), 
        					bookshelf.getMeanGoodreadsVotes(),
        					bookshelf.getGoodreadsMinVotes()));
        	row.createCell(5).setCellValue(book.getAmazonAverageRating());
        	row.createCell(6).setCellValue(book.getAmazonRatingsCount());
        	row.createCell(7).setCellValue(
        			getAjustedRating(
        					book.getAmazonRatingsCount(), 
        					book.getAmazonAverageRating(), 
        					bookshelf.getMeanAmazonVotes(),
        					bookshelf.getAmazonMinVotes()));
        	row.createCell(8).setCellValue(
        			getAjustedRating(
        					book.getTotalReviews(),
        					book.getTotalWeightedAverageRating(), // or regular average?
        					bookshelf.getTotalMean(),
        					bookshelf.getTotalMinVotes()
        					));
        	System.out.println(book.getTitle() + " has avg of " + book.getTotalAverageRating() + " with weight average of " + book.getTotalWeightedAverageRating());

    	}
    	
//        for (Book book: bookshelf) {
//        }
    	  try
          {
              //Write the workbook in file system
              FileOutputStream out = new FileOutputStream("BookRatings.xls");
              wb.write(out);
              out.close();
              System.out.println("BookRatings.xlsx written successfully on disk.");
          }
          catch (Exception e)
          {
              e.printStackTrace();
          }
    	  finally {
    		  wb.close();
    	  }
	}
	
	


//	weighted rating (WR) = (v ÷ (v+m)) × R + (m ÷ (v+m)) × C
//	R = average for the movie (mean) = (Rating)
//	v = number of votes for the movie = (votes)
//	m = minimum votes required to be listed in the Top 250 (currently 25,000)
//	C = the mean vote across the whole report.
	private Double getAjustedRating(int ratingsCount, double averageRating, double meanVote, double minVotes) {
		double rating = averageRating * 2;
		return (ratingsCount / (ratingsCount + minVotes)) * rating + (minVotes / (ratingsCount + minVotes)) * meanVote;

	}


}
