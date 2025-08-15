package manager;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FileBackedTaskManagerExceptionTest {
    @TempDir
    Path dir;

    @Test
    void loadFromCorruptedFileThrowsIllegalArgumentException() throws Exception {
        // Пишем корректный заголовок + «битую» строку с UNKNOWN типом
        File badFile = dir.resolve("bad.csv").toFile();
        Files.writeString(badFile.toPath(),
                "id,type,name,status,description,epic,startTime,durationMinutes\n" +
                        "42,UNKNOWN,Name,NEW,desc,,,\n");

        assertThrows(IllegalArgumentException.class,
                () -> FileBackedTaskManager.loadFromFile(badFile));
    }

    @Test
    void loadFromHeaderOnlyDoesNotThrow() throws Exception {
        File headerOnly = dir.resolve("header.csv").toFile();
        Files.writeString(headerOnly.toPath(),
                "id,type,name,status,description,epic,startTime,durationMinutes\n");

        assertDoesNotThrow(() -> FileBackedTaskManager.loadFromFile(headerOnly));
    }

}
