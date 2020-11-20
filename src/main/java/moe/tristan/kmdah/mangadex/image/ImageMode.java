package moe.tristan.kmdah.mangadex.image;

public enum ImageMode {

    DATA("data"),
    DATA_SAVER("data-saver");

    private final String pathFragment;

    ImageMode(String pathFragment) {
        this.pathFragment = pathFragment;
    }

    public final String getPathFragment() {
        return pathFragment;
    }

    public static ImageMode fromPathFragment(String fragment) {
        if (DATA.getPathFragment().equals(fragment)) {
            return DATA;
        } else if (DATA_SAVER.getPathFragment().equals(fragment)) {
            return DATA_SAVER;
        } else {
            throw new IllegalArgumentException("Unknown image mode: " + fragment);
        }
    }

}
