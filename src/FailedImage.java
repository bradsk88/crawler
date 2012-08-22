
public class FailedImage {

    private final String imageUrl;
    private final String destinationFile;

    public String getImageUrl() {
        return imageUrl;
    }

    public String getDestinationFile() {
        return destinationFile;
    }

    public FailedImage(final String imageUrl, final String destinationFile) {

        this.imageUrl = imageUrl;
        this.destinationFile = destinationFile;

    }

}
