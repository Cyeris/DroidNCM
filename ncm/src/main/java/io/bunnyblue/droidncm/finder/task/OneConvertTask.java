package io.bunnyblue.droidncm.finder.task;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.widget.Toast;

import java.io.File;

import io.bunnyblue.droidncm.dump.NcmDumper;
import io.bunnyblue.droidncm.finder.MainFinderActivity;
import io.bunnyblue.droidncm.finder.dummy.NCMFileContent;
import io.bunnyblue.droidncm.history.NCMDatabaseHelper;
import io.bunnyblue.droidncm.history.NCMHistory;

public class OneConvertTask extends AsyncTask<NCMFileContent.NCMLocalFile, String, NCMFileContent.NCMLocalFile> {
    ProgressDialog progressDialog;
    Context context;

    public OneConvertTask(Context context) {
        this.context = context;
    }

    @Override
    protected NCMFileContent.NCMLocalFile doInBackground(NCMFileContent.NCMLocalFile... contents) {
        int index = 0;
        NCMFileContent.NCMLocalFile ncmLocalFile = contents[0];
        File srcFile = new File(ncmLocalFile.localPath);
        publishProgress(srcFile.getName());
        String targetFile = NcmDumper.ncpDump(srcFile.getAbsolutePath());
        if (targetFile.startsWith("/")) {
            File target = new File(targetFile);
            if (target.exists()) {
                ncmLocalFile.targetPath = targetFile;
                NCMHistory ncmHistory = NCMDatabaseHelper.buildHistory(ncmLocalFile);
                NCMDatabaseHelper.getInstance().ncmHistoryDAO().deleteByLocalPath(ncmLocalFile.localPath);
                NCMDatabaseHelper.getInstance().ncmHistoryDAO().insertAll(ncmHistory);
                return ncmLocalFile;
                //  return target;
            }
        } else {
            ncmLocalFile.error = targetFile;
        }


        return ncmLocalFile;
    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
        if (values[0].startsWith("/")) {
            progressDialog.setMessage(String.format("success process  file %s", values[0]));
        } else {
            progressDialog.setMessage(String.format("processing file %s ..", values[0]));
        }

    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("正在处理");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    @Override
    protected void onPostExecute(NCMFileContent.NCMLocalFile file) {
        super.onPostExecute(file);
        progressDialog.dismiss();
        if (!TextUtils.isEmpty(file.error)) {
            Toast.makeText(context, file.error, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Done", Toast.LENGTH_SHORT).show();

        }


        ((MainFinderActivity) context).updateNCMFileList();

    }


}
