package moe.tristan.kmdah.mangadex.image;

public final class MangadexUpstreamException extends RuntimeException {

    public MangadexUpstreamException(String reason) {
        super(reason);
    }

    public MangadexUpstreamException(String reason, Throwable cause) {
        super(reason, cause);
    }

}
