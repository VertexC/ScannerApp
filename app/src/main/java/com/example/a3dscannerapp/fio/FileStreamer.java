package com.example.a3dscannerapp.fio;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.KeyException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class FileStreamer {
    private final static int SENSOR_BYTE_LENGTH = 8;
    private final static String LOG_TAG = FileStreamer.class.getSimpleName();

    private Context mContext;

    private HashMap<String, BufferedWriter> mFileWriters = new HashMap<>();
    private String mOutputFolder;

    public FileStreamer(Context context, final String outputFolder){
        mContext = context;
        mOutputFolder = outputFolder;
    }

    public void addFile(final String writerId, final String fileName) throws IOException {
        if(mFileWriters.containsKey(writerId)){
            Log.w(LOG_TAG, "File writer" + writerId + " already exist");
            return;
        }
        Calendar file_timestamp = Calendar.getInstance();
        String header = "# Created at " + file_timestamp.getTime().toString() + "\n";
        BufferedWriter newWriter = createFile(mOutputFolder + "/" + fileName, header);
        mFileWriters.put(writerId, newWriter);
    }

    private BufferedWriter createFile(String path, String header) throws IOException {
        File file = new File(path);
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        scanIntent.setData(Uri.fromFile(file));
        mContext.sendBroadcast(scanIntent);
        if (header != null && header.length() != 0) {
            writer.append(header);
            writer.flush();
        }
        return writer;
    }

    public String getOutputFolder(){
        return mOutputFolder;
    }

    public BufferedWriter getFileWriter(final String writerId){
        return mFileWriters.get(writerId);
    }

    public void addRecord(long timestamp, String writerId, int numValues, final float[] values) throws IOException, KeyException {
        synchronized (this){
            BufferedWriter writer = getFileWriter(writerId);
            if (writer == null){
                throw new KeyException("File writer " + writerId + " not found");
            }
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(ByteBuffer.allocate(SENSOR_BYTE_LENGTH).putLong(timestamp).array().toString());
            for(int i=0; i<numValues; ++i){
                stringBuilder.append(ByteBuffer.allocate(SENSOR_BYTE_LENGTH).putFloat(values[i]).array().toString());
            }
            stringBuilder.append("\n");
            writer.write(stringBuilder.toString());
        }
    }

    public void endFiles() throws IOException {
        synchronized (this){
            for (BufferedWriter w : mFileWriters.values()) {
                w.flush();
                w.close();
            }
        }
    }
}
