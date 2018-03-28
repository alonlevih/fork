package com.shazam.fork.system.io;

import com.android.ddmlib.testrunner.TestIdentifier;
import com.shazam.fork.model.Device;
import com.shazam.fork.model.Pool;
import com.shazam.fork.model.TestCaseEvent;
import org.apache.commons.io.filefilter.AndFileFilter;
import org.apache.commons.io.filefilter.PrefixFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Path;

import static com.shazam.fork.CommonDefaults.FORK_SUMMARY_FILENAME_FORMAT;
import static com.shazam.fork.system.io.FileType.TEST;
import static java.nio.file.Files.createDirectories;
import static java.nio.file.Paths.get;

public class ForkFileManager implements FileManager {
    private final File output;

    public ForkFileManager(File output) {
        this.output = output;
    }

    @Override
    public File[] getTestFilesForDevice(Pool pool, Device serial) {
        Path path = getDirectory(TEST, pool, serial);
        return path.toFile().listFiles();
    }

    @Override
    public File createFile(FileType fileType, Pool pool, Device device, TestCaseEvent testCaseEvent) {
        return createFile(fileType, pool, device, new TestIdentifier(testCaseEvent.getTestClass(), testCaseEvent.getTestMethod()));
    }

    @Override
    public File createFile(FileType fileType, Pool pool, Device device, TestIdentifier testIdentifier, int sequenceNumber) {
        try {
            Path directory = createDirectory(fileType, pool, device);
            String filename = createFilenameForTest(testIdentifier, fileType, sequenceNumber);
            return createFile(directory, filename);
        } catch (IOException e) {
            throw new CouldNotCreateDirectoryException(e);
        }
    }

    @Override
    public File createFile(FileType fileType, Pool pool, Device device, TestIdentifier testIdentifier) {
        try {
            Path directory = createDirectory(fileType, pool, device);
            String filename = createFilenameForTest(testIdentifier, fileType);
            return createFile(directory, filename);
        } catch (IOException e) {
            throw new CouldNotCreateDirectoryException(e);
        }
    }

    @Override
    public File createSummaryFile() {
        try {
            Path path = get(output.getAbsolutePath(), "summary");
            Path directory = createDirectories(path);
            return createFile(directory, String.format(FORK_SUMMARY_FILENAME_FORMAT, System.currentTimeMillis()));
        } catch (IOException e) {
            throw new CouldNotCreateDirectoryException(e);
        }
    }

    @Override
    public File[] getFiles(FileType fileType, Pool pool, Device device, TestIdentifier testIdentifier) {
        FileFilter fileFilter = new AndFileFilter(
                new PrefixFileFilter(testIdentifier.toString()),
                new SuffixFileFilter(fileType.getSuffix()));

        File deviceDirectory = get(output.getAbsolutePath(), fileType.getDirectory(), pool.getName(), device.getSafeSerial()).toFile();
        return deviceDirectory.listFiles(fileFilter);
    }

    @Override
    public File getFile(FileType fileType, String pool, String safeSerial, TestIdentifier testIdentifier) {
        String filenameForTest = createFilenameForTest(testIdentifier, fileType);
        Path path = get(output.getAbsolutePath(), fileType.getDirectory(), pool, safeSerial, filenameForTest);
        return path.toFile();
    }

    private Path createDirectory(FileType test, Pool pool, Device device) throws IOException {
        return createDirectories(getDirectory(test, pool, device));
    }

    private Path getDirectory(FileType fileType, Pool pool, Device device) {
        return get(output.getAbsolutePath(), fileType.getDirectory(), pool.getName(), device.getSafeSerial());
    }

    private File createFile(Path directory, String filename) {
        return new File(directory.toFile(), filename);
    }

    private String createFilenameForTest(TestIdentifier testIdentifier, FileType fileType) {
        return String.format("%s.%s", testIdentifier.toString(), fileType.getSuffix());
    }

    private String createFilenameForTest(TestIdentifier testIdentifier, FileType fileType, int sequenceNumber) {
        return String.format("%s-%02d.%s", testIdentifier.toString(), sequenceNumber, fileType.getSuffix());
    }


    public Path createDirectory(String test, Pool pool, Device device, TestIdentifier testIdentifier) throws IOException {
        return createDirectories(getDirectory(test, pool, device, testIdentifier));
    }

    private Path getDirectory(String type, Pool pool, Device device, TestIdentifier testIdentifier) {
        return get(output.getAbsolutePath(), type, pool.getName(), device.getSafeSerial(), testIdentifier.toString());
    }

}