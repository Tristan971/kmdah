package moe.tristan.kmdah.common.model.persistence;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;

import moe.tristan.kmdah.common.model.ImageSpec;
import moe.tristan.kmdah.common.model.mangadex.image.ImageMode;

@Entity
@Table(name = "images")
public class ImageEntity implements ImageSpec {

    @Id
    @Column(name = "filename")
    private String filename;

    @Column(name = "chapter_hash")
    private String chapterHash;

    @Column(name = "mode")
    @Enumerated(EnumType.STRING)
    private ImageMode mode;

    @Column(name = "content_type")
    private String contentType;

    @Column(name = "size")
    private int size;

    protected ImageEntity() {
        // for hibernate
    }

    public ImageEntity(String filename, String chapterHash, ImageMode mode, String contentType, int size) {
        this.filename = filename;
        this.chapterHash = chapterHash;
        this.mode = mode;
        this.contentType = contentType;
        this.size = size;
    }

    @Override
    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    @Override
    public String getChapterHash() {
        return chapterHash;
    }

    public void setChapterHash(String chapterHash) {
        this.chapterHash = chapterHash;
    }

    @Override
    public ImageMode getMode() {
        return mode;
    }

    public void setMode(ImageMode mode) {
        this.mode = mode;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    @Override
    public String toString() {
        return "ImageEntity{" +
               "filename='" + filename + '\'' +
               ", chapterHash='" + chapterHash + '\'' +
               ", mode=" + mode +
               '}';
    }

}
