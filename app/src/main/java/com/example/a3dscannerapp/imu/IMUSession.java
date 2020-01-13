//package com.example.a3dscannerapp.imu;
//
//public class IMUSession implements SensorEventListener {
//    private static final String LOG_TAG = IMUSession.class.getSimpleName();
//
//    private MainActivity mContext;
//    private HashMap<String, Sensor> mSensors = new HashMap<>();
//    private SensorManager mSensorManager;
//    private float mInitialStepCount = -1;
//    private FileStreamer mFileStreamer = null;
//
//    private AtomicBoolean mIsRecording = new AtomicBoolean(false);
//    private AtomicBoolean mIsWritingFile = new AtomicBoolean(false);
//
//    private float[] mGyroBias = new float[3];
//    private float[] mMagnetBias = new float[3];
//    private float[] mAcceBias = new float[3];
//
//    public IMUSession(MainActivity context){
//        mContext = context;
//        mSensorManager = (SensorManager)mContext.getSystemService(Context.SENSOR_SERVICE);
//
//        mSensors.put("gyro", mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE));
//        mSensors.put("gyro_uncalib", mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE_UNCALIBRATED));
//        // Some phones (e.g. Pixel 2XL) automatically calibration accelerometer bias. For unified
//        // processing framework, we use uncalibration acceleroemter and provide external calibration.
//        if(Build.VERSION.SDK_INT < 26){
//            Log.i(LOG_TAG, String.format(Locale.US, "API level: %d, Accelermeter used.", Build.VERSION.SDK_INT));
//            mSensors.put("acce", mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
//        } else {
//            Log.i(LOG_TAG, String.format(Locale.US, "API level: %d, Accelermeter_uncalibrated used.", Build.VERSION.SDK_INT));
//            mSensors.put("acce", mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER_UNCALIBRATED));
//        }
//        mSensors.put("linacce", mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION));
//        mSensors.put("gravity", mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY));
//        mSensors.put("magnet", mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD));
//        mSensors.put("magnet_uncalib", mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED));
//        mSensors.put("rv", mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR));
//        mSensors.put("game_rv", mSensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR));
//        mSensors.put("magnetic_rv", mSensorManager.getDefaultSensor(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR));
//        mSensors.put("step", mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER));
//        mSensors.put("pressure", mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE));
//        registerSensors();
//    }
//
//    public void registerSensors(){
//        for(Sensor s: mSensors.values()){
//            mSensorManager.registerListener(this, s, SensorManager.SENSOR_DELAY_FASTEST);
//        }
//    }
//
//    public void unregisterSensors(){
//        for(Sensor s: mSensors.values()){
//            mSensorManager.unregisterListener(this, s);
//        }
//    }
//    public boolean isRecording(){
//        return mIsRecording.get();
//    }
//
//    public float[] getGyroBias(){
//        return mGyroBias;
//    }
//
//    public float[] getMagnetBias(){
//        return mMagnetBias;
//    }
//
//    public float[] getAcceBias(){
//        return mAcceBias;
//    }
//
//    public void startSession(String streamFolder){
//        if (streamFolder != null){
//            mFileStreamer = new FileStreamer(mContext, streamFolder);
//            try {
//                mFileStreamer.addFile("gyro", "gyro.txt");
//                mFileStreamer.addFile("gyro_uncalib", "gyro_uncalib.txt");
//                mFileStreamer.addFile("acce", "acce.txt");
//                mFileStreamer.addFile("linacce", "linacce.txt");
//                mFileStreamer.addFile("gravity", "gravity.txt");
//                mFileStreamer.addFile("magnet", "magnet.txt");
//                mFileStreamer.addFile("rv", "rv.txt");
//                mFileStreamer.addFile("game_rv", "game_rv.txt");
//                mFileStreamer.addFile("magnetic_rv", "magnetic_rv.txt");
//                mFileStreamer.addFile("step", "step.txt");
//                mFileStreamer.addFile("pressure", "pressure.txt");
//                mFileStreamer.addFile("gyro_bias", "gyro_bias.txt");
//                mIsWritingFile.set(true);
//            } catch (IOException e){
//                mContext.showToast("Error occurs when creating output IMU files.");
//                e.printStackTrace();
//            }
//
//        }
//
//        mIsRecording.set(true);
//    }
//
//    public void stopSession(){
//        mIsRecording.set(false);
//
//        if(mIsWritingFile.get()){
//            try{
//                BufferedWriter gyro_bias_end_writer = mFileStreamer.getFileWriter("gyro_bias");
//                gyro_bias_end_writer.write(String.format(Locale.US, "%f %f %f", mGyroBias[0], mGyroBias[1], mGyroBias[2]));
//                mFileStreamer.endFiles();
//            } catch (IOException e){
//                mContext.showToast("Error occurs when finishing IMU files.");
//                e.printStackTrace();
//            }
//
//            // If the accelerometer calibration file is found in the Download folder, copy it to
//            // the streaming folder.
//            try{
//                File acce_calib_file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/acce_calib.txt");
//                File out_acce_calib_file = new File(mFileStreamer.getOutputFolder() + "/acce_calib.txt");
//                if (acce_calib_file.exists()){
//                    FileInputStream istr = new FileInputStream(acce_calib_file);
//                    FileOutputStream ostr = new FileOutputStream(out_acce_calib_file);
//                    FileChannel ichn = istr.getChannel();
//                    FileChannel ochn = ostr.getChannel();
//                    ichn.transferTo(0, ichn.size(), ochn);
//                    istr.close();
//                    ostr.close();
//
//                    Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
//                    scanIntent.setData(Uri.fromFile(out_acce_calib_file));
//                    mContext.sendBroadcast(scanIntent);
//                }
//            } catch (IOException e){
//                e.printStackTrace();
//            }
//
//            mIsWritingFile.set(false);
//            mFileStreamer = null;
//        }
//
//        mInitialStepCount = -1;
//    }
//
//    @Override
//    public void onSensorChanged(final SensorEvent event){
//        long timestamp = event.timestamp;
//        float[] values = {0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f};
////        Log.i("imuSession", String.format("mIsRecording:%b, mIsWritingFile:%b", mIsRecording.get(), mIsWritingFile.get()));
//        try {
//            synchronized(mContext.inBuffer){
//                switch (event.sensor.getType()) {
//                    case Sensor.TYPE_ACCELEROMETER:
//                        if (mIsRecording.get() && mIsWritingFile.get()) {
//                            mFileStreamer.addRecord(timestamp, "acce", 3, event.values);
//                        }
//                        break;
//
//                    case Sensor.TYPE_GYROSCOPE:
//                        if (mIsRecording.get() && mIsWritingFile.get()) {
////                        Log.i("imuSession", String.format("record gyro data %s", event.values.toString()));
//                            mFileStreamer.addRecord(timestamp, "gyro", 3, event.values);
//                            if (!mContext.offline){
//                                mContext.inBuffer.addRecord(timestamp, "gyro", 3, event.values);
//                            }
//                        }
//                        break;
//
//                    case Sensor.TYPE_LINEAR_ACCELERATION:
//                        if (mIsRecording.get() && mIsWritingFile.get()) {
//                            mFileStreamer.addRecord(timestamp, "linacce", 3, event.values);
//                        }
//                        break;
//
//                    case Sensor.TYPE_GRAVITY:
//                        if (mIsRecording.get() && mIsWritingFile.get()) {
//                            mFileStreamer.addRecord(timestamp, "gravity", 3, event.values);
//                        }
//                        break;
//
//                    case Sensor.TYPE_GAME_ROTATION_VECTOR:
//                        if (mIsRecording.get() && mIsWritingFile.get()) {
//                            mFileStreamer.addRecord(timestamp, "game_rv", 4, event.values);
//                            if (!mContext.offline){
//                                mContext.inBuffer.addRecord(timestamp, "game_rv", 4, event.values);
//                            }
//                        }
//
//                        break;
//
//                    case Sensor.TYPE_ROTATION_VECTOR:
//                        if (mIsRecording.get() && mIsWritingFile.get()) {
//                            mFileStreamer.addRecord(timestamp, "rv", 4, event.values);
//                        }
//                        break;
//
//                    case Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR:
//                        if (mIsRecording.get() && mIsWritingFile.get()) {
//                            mFileStreamer.addRecord(timestamp, "magnetic_rv", 4, event.values);
//                        }
//                        break;
//
//                    case Sensor.TYPE_MAGNETIC_FIELD:
//                        if (mIsRecording.get() && mIsWritingFile.get()) {
//                            mFileStreamer.addRecord(timestamp, "magnet", 3, event.values);
//                        }
//                        break;
//
//                    case Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED:
//                        mMagnetBias[0] = event.values[3];
//                        mMagnetBias[1] = event.values[4];
//                        mMagnetBias[2] = event.values[5];
//                        break;
//
//                    case Sensor.TYPE_STEP_COUNTER:
//                        if (mInitialStepCount < 0) {
//                            mInitialStepCount = event.values[0] - 1;
//                        }
//                        values[0] = event.values[0] - mInitialStepCount;
//                        if (mIsRecording.get() && mIsWritingFile.get()) {
//                            mFileStreamer.addRecord(timestamp, "step", 1, values);
//                        }
//                        break;
//
//                    case Sensor.TYPE_PRESSURE:
//                        if (mIsRecording.get() && mIsWritingFile.get()) {
//                            mFileStreamer.addRecord(timestamp, "pressure", 1, event.values);
//                        }
//                        break;
//
//                    case Sensor.TYPE_GYROSCOPE_UNCALIBRATED:
//                        mGyroBias[0] = event.values[3];
//                        mGyroBias[1] = event.values[4];
//                        mGyroBias[2] = event.values[5];
//                        if (mIsRecording.get() && mIsWritingFile.get()) {
//                            mFileStreamer.addRecord(timestamp, "gyro_uncalib", 3, event.values);
//                        }
//                        break;
//
//                    case Sensor.TYPE_ACCELEROMETER_UNCALIBRATED:
//                        mAcceBias[0] = event.values[3];
//                        mAcceBias[1] = event.values[4];
//                        mAcceBias[2] = event.values[5];
//                        if (mIsRecording.get() && mIsWritingFile.get()) {
//                            mFileStreamer.addRecord(timestamp, "acce", 3, event.values);
//                            if (!mContext.offline){
//                                mContext.inBuffer.addRecord(timestamp, "acce", 3, event.values);
//                            }
//                        }
//                        break;
//                }
//            }
//        } catch (IOException | KeyException e){
//            e.printStackTrace();
//        }
//    }
//
//    @Override
//    public void onAccuracyChanged(Sensor sensor, int accuracy){
//
//    }
//}
