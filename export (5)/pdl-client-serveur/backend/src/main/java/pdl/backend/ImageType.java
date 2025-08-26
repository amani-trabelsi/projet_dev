package pdl.backend;

public enum ImageType {
    PNG("png"),
    JPEG("jpeg"),
    JPG("jpg");

    private final String extension;

    ImageType(String extension) {
        this.extension = extension;
    }

    public String getExtension() {
        return extension;
    }

    public static boolean isSupported(String ext) {
        for (ImageType type : ImageType.values()) {
            if (type.getExtension().equalsIgnoreCase(ext)) {
                return true;
            }
        }
        return false;
    }
}
