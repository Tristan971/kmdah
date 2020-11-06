package moe.tristan.kmdah.common.internal.model.image;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;

import moe.tristan.kmdah.common.mangadex.image.ImageMode;

@Entity
@Table(name = "images")
public class ImageEntity {

    @Id
    @Column(name = "filename")
    private final String filename;

    @Column(name = "chapter_hash")
    private final String chapterHash;

    @Column(name = "mode")
    @Enumerated(EnumType.STRING)
    private final ImageMode mode;

    @Column(name = "content_type")
    private final String contentType;

    @Column(name = "size")
    private final int size;

    public ImageEntity(String filename, String chapterHash, ImageMode mode, String contentType, int size) {
        this.filename = filename;
        this.chapterHash = chapterHash;
        this.mode = mode;
        this.contentType = contentType;
        this.size = size;
    }

    public String getContentType() {
        return contentType;
    }

    public int getSize() {
        return size;
    }

}
