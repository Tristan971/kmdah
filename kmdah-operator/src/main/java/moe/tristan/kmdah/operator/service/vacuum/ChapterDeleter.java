package moe.tristan.kmdah.operator.service.vacuum;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ChapterDeleter implements FileVisitor<Path> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChapterDeleter.class);

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
        try {
            Files.delete(file);
        } catch (Throwable e) {
            LOGGER.error("Failed deletion of file {}", file, e);
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) {
        LOGGER.error("Failed visit of {}", file, exc);
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
        try {
            Files.delete(dir);
        } catch (Throwable e) {
            LOGGER.warn("Failed deletion of directory {}", dir, e);
        }
        return FileVisitResult.CONTINUE;
    }

}
