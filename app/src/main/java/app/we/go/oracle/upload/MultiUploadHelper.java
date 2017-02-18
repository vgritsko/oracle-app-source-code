package app.we.go.oracle.upload;

import com.dropbox.core.v2.DbxClientV2;

import java.io.File;

public class MultiUploadHelper {
    private final File[] files;
    private int currentIndex = 0;
    private DbxClientV2 client;
    private MultiCallback multiCallback;
    private String username;

    public MultiUploadHelper(DbxClientV2 client, String username, File[] files, MultiCallback multiCallback) {
        this.client = client;
        this.username = username;
        this.files = files;
        this.multiCallback = multiCallback;
    }

    private File getNextFile() {
        if (files == null || files.length == currentIndex) {
            return null;
        } else {
            return files[currentIndex++];
        }
    }

    private boolean hasNextFile() {
        return currentIndex < files.length;
    }


    public void start() {
        File f = getNextFile();
        if (f != null) {
            DropBoxUploadTask task = new DropBoxUploadTask(client, username, getCallback());
            task.execute(f);
        }
    }

    private DropBoxUploadTask.Callback getCallback() {
        return new DropBoxUploadTask.Callback() {
            @Override
            public void onComplete() {
                if (!hasNextFile()) {
                    multiCallback.onComplete(files.length);
                } else {
                    File f = getNextFile();
                    DropBoxUploadTask task = new DropBoxUploadTask(client, username, getCallback());
                    task.execute(f);
                }
            }

            @Override
            public void onError() {
                multiCallback.onError();
            }
        };
    }

    public interface MultiCallback {
        void onError();
        void onComplete(int length);
    }

}
