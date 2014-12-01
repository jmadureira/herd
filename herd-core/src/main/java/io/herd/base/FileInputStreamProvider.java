package io.herd.base;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * {@link StreamProvider} implementation that opens an {@link InputStream} from a file.
 * 
 * @author joaomadureira
 *
 */
public class FileInputStreamProvider implements StreamProvider {

    private final File streamFile;

    /**
     * Creates a new {@link FileInputStreamProvider} pointing to a {@link File} instance by converting the given
     * pathname string into an abstract pathname. If the given string is the empty string, then the result is the empty
     * abstract pathname.
     * 
     * @param pathName A pathname to a file
     * @throws NullPointerException is no pathName was provided
     */
    public FileInputStreamProvider(String pathName) {
        this(new File(pathName));
    }

    /**
     * Creates a new {@link FileInputStreamProvider} pointing to the provided {@link File}.
     * 
     * @param file The target file
     */
    public FileInputStreamProvider(File file) {
        this.streamFile = file;
    }

    @Override
    public InputStream open() throws IOException {
        return new FileInputStream(streamFile);
    }

}
