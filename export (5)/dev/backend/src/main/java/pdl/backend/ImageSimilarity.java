package pdl.backend;

public class ImageSimilarity {
    private Image image;
    private double score;

    public ImageSimilarity(Image image, double score) {
        this.image = image;
        this.score = score;
    }

    public Image getImage() {
        return image;
    }

    public double getScore() {
        return score;
    }
}
