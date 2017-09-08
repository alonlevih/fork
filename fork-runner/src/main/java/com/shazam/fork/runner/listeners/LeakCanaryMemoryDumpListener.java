package com.shazam.fork.runner.listeners;

import com.android.ddmlib.*;
import com.android.ddmlib.FileListingService.FileEntry;
import com.android.ddmlib.testrunner.ITestRunListener;
import com.android.ddmlib.testrunner.TestIdentifier;
import com.shazam.fork.Configuration;
import com.shazam.fork.model.Device;
import com.shazam.fork.model.Pool;
import com.shazam.fork.model.TestCaseEvent;
import com.shazam.fork.system.adb.CollectingShellOutputReceiver;
import com.shazam.fork.system.io.FileManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.Map;

import static com.android.ddmlib.FileListingService.TYPE_DIRECTORY;
import static com.android.ddmlib.SyncService.getNullProgressMonitor;

public class LeakCanaryMemoryDumpListener implements ITestRunListener {

    private final Device device;
    private final FileManager fileManager;
    private final Pool pool;
    private final Logger logger = LoggerFactory.getLogger(LeakCanaryMemoryDumpListener.class);
    private final TestCaseEvent testCase;
    private Configuration configuration;

    public LeakCanaryMemoryDumpListener(Device device, FileManager fileManager, Pool pool, TestCaseEvent testCase, Configuration configuration) {
        this.device = device;
        this.fileManager = fileManager;
        this.pool = pool;
        this.testCase = testCase;
        this.configuration = configuration;
    }

    @Override
    public void testRunStarted(String runName, int testCount) {

    }

    @Override
    public void testStarted(TestIdentifier test) {

        try {
            device.getDeviceInterface().root();
        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (AdbCommandRejectedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ShellCommandUnresponsiveException e) {
            e.printStackTrace();
        }

        try {
            CollectingShellOutputReceiver receiver = new CollectingShellOutputReceiver();
            device.getDeviceInterface().executeShellCommand("am start -n \"" + configuration.getApplicationPackage() + "/.URLNavigatorActivity\" -a \"android.intent.action.VIEW\" -d \"https://" + configuration.getApplicationPackage() +"/booleanSettings?KEY_ENABLE_LEAK_CANARY_STR=true\"", receiver);
            Thread.sleep(5000);
            device.getDeviceInterface().executeShellCommand("am force-stop " + configuration.getApplicationPackage(), receiver);
        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (AdbCommandRejectedException e) {
            e.printStackTrace();
        } catch (ShellCommandUnresponsiveException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void testFailed(TestIdentifier test, String trace) {
    }

    @Override
    public void testAssumptionFailure(TestIdentifier test, String trace) {
    }

    @Override
    public void testIgnored(TestIdentifier test) {
    }

    @Override
    public void testEnded(TestIdentifier test, Map<String, String> testMetrics) {
        try {
            CollectingShellOutputReceiver receiver = new CollectingShellOutputReceiver();
            device.getDeviceInterface().executeShellCommand("am start -n \"" + configuration.getApplicationPackage() + "/.URLNavigatorActivity\" -a \"android.intent.action.VIEW\" -d \"https://" + configuration.getApplicationPackage() + "/booleanSettings?KEY_ENABLE_LEAK_CANARY_STR=false\"", receiver);
            Thread.sleep(5000);
        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (AdbCommandRejectedException e) {
            e.printStackTrace();
        } catch (ShellCommandUnresponsiveException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        TestIdentifier testIdentifier = new TestIdentifier(testCase.getTestClass(), testCase.getTestMethod());
        try {
            Path localLeakCanaryFolder = fileManager.createDirectory("leakcanary", pool, device, testIdentifier);
            adbPull(device.getDeviceInterface(), getDirectoryOnExternalStorage(device.getDeviceInterface(), "Download/leakcanary-" + configuration.getApplicationPackage()), localLeakCanaryFolder.toString());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private FileEntry getDirectoryOnExternalStorage(IDevice device, final String dir)
            throws Exception {
        String externalPath = getExternalStoragePath(device, dir);
        return obtainDirectoryFileEntry(externalPath);
    }

    private String getExternalStoragePath(IDevice device, final String path) throws Exception {
        CollectingOutputReceiver pathNameOutputReceiver = new CollectingOutputReceiver();
        device.executeShellCommand("echo $EXTERNAL_STORAGE", pathNameOutputReceiver);
        return pathNameOutputReceiver.getOutput().trim() + "/" + path;
    }

    /** Get a {@link FileEntry} for an arbitrary path. */
    static FileEntry obtainDirectoryFileEntry(String path) {
        try {
            FileEntry lastEntry = null;
            Constructor<FileEntry> c =
                    FileEntry.class.getDeclaredConstructor(FileEntry.class, String.class, int.class,
                            boolean.class);
            c.setAccessible(true);
            for (String part : path.split("/")) {
                lastEntry = c.newInstance(lastEntry, part, TYPE_DIRECTORY, lastEntry == null);
            }
            return lastEntry;
        } catch (NoSuchMethodException ignored) {
        } catch (InvocationTargetException ignored) {
        } catch (InstantiationException ignored) {
        } catch (IllegalAccessException ignored) {
        }
        return null;
    }

    private void adbPull(IDevice device, FileEntry remoteDirName, String localDirName) {
        try {
            device.getSyncService().pull(new FileEntry[]{remoteDirName}, localDirName,
                    getNullProgressMonitor());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void testRunFailed(String errorMessage) {
    }

    @Override
    public void testRunStopped(long elapsedTime) {
    }

    @Override
    public void testRunEnded(long elapsedTime, Map<String, String> runMetrics) {

    }
}
