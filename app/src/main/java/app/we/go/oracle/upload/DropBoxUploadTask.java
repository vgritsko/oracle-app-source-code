package app.we.go.oracle.upload;

import android.os.AsyncTask;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class DropBoxUploadTask extends AsyncTask<File, Void, Boolean> {
    private DbxClientV2 client;
    private String username;
    private Callback callback;

    public DropBoxUploadTask(DbxClientV2 client, String username, Callback callback) {
        this.client = client;
        this.username = username;
        this.callback = callback;
    }



    @Override
    protected Boolean doInBackground(File... params) {

        File f = params[0];

        try (InputStream in = new FileInputStream(f)) {
            FileMetadata metadata = client.files().uploadBuilder("/" + username + "/" + f.getName())
                    .uploadAndFinish(in);
            f.delete();
            return true;
        } catch (IOException | DbxException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean b) {
        super.onPostExecute(b);
        if (b) {
            callback.onComplete();
        } else {
            callback.onError();
        }
    }

    public interface Callback {
        void onComplete();
        void onError();
    }
}
