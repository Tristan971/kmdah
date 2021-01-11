package moe.tristan.kmdah.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import org.apache.commons.io.IOUtils;

public class ContentCallbackInputStream extends InputStream {

    private final InputStream delegate;

    private final ByteArrayOutputStream contentBuffer = new ByteArrayOutputStream();
    private final Consumer<byte[]> contentCallback;
    private boolean callbackCalled = false;

    public ContentCallbackInputStream(InputStream delegate, Consumer<byte[]> contentCallback) {
        this.delegate = delegate;
        this.contentCallback = contentCallback;
    }

    @Override
    public int read() throws IOException {
        int read = delegate.read();

        if (read == IOUtils.EOF && !callbackCalled) {
            contentBuffer.write(IOUtils.EOF); // buffered reads trigger multiple EOF delegate reads ; so ensure only writing it once
            callbackCalled = true;
            byte[] contentCopy = contentBuffer.toByteArray(); // make sure to dupe before last return
            CompletableFuture.runAsync(() -> contentCallback.accept(contentCopy));
        } else if (read != IOUtils.EOF) {
            contentBuffer.write(read);
        }

        return read;
    }

    @Override
    public int available() throws IOException {
        return delegate.available();
    }

    @Override
    public void close() throws IOException {
        delegate.close();
    }

}
