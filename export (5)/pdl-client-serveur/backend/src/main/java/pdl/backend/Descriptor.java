package pdl.backend;

public enum Descriptor {
    HISTO_RGB("histoRGB"),
    HISTO_TEINTE_SATURATION("histoTeinteSaturation"),
    COMBINED("combined");

    private final String value;

    Descriptor(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static Descriptor fromString(String str) {
        switch (str.toLowerCase()) {
            case "rgb":
            case "historgb":
                return HISTO_RGB;
            case "ts":
            case "histoteintesaturation":
                return HISTO_TEINTE_SATURATION;
            case "combined":
                return COMBINED;
            default:
                throw new IllegalArgumentException("Descripteur non reconnu: " + str);
        }
    }
}
