import java.nio.file.Path;

public class ScannedFile {

	private String title;
	private String category;
	private Path absolutePath;
	
	public ScannedFile(String title, String category, Path path) {
		this.title = title;
		this.category = category;
		this.absolutePath = path;
	}
	
	public String getTitle() {
		return title;
	}
	public String getCategory() {
		return category;
	}
	public Path getAbsolutePath() {
		return absolutePath;
	}
}
