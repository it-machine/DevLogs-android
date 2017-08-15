package ru.it_machine.devlogs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.text.DateFormat;
import java.util.Date;
import java.util.LinkedList;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import ru.it_machine.devlogs.JsonSchema.LogModel;
import ru.it_machine.devlogs.JsonSchema.LogsModel;

/**
 * Created by Mark on 04/08/2017.
 */

public class DevLogs {

    private static final String FILENAME = "logs.json";
    private static final int PLATFORM_ID = 2;

    @SuppressLint("StaticFieldLeak")
    private static DevLogs instance;
    private static Context appContext;
    private static String userToken;
    private static String appToken;

    private final RequestInterface requestInterface;
    private LogsModel logs;

    private final Object lock = new Object();

    private DevLogs() {
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

        if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            httpClient.addInterceptor(logging);
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (lock) {
                    try {
                        FileInputStream logsInput = appContext.openFileInput(FILENAME);
                        Reader reader = new InputStreamReader(logsInput, "utf-8");
                        logs = new Gson().fromJson(reader, LogsModel.class);
                        reader.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                        logs = new LogsModel();
                    }
                    if (logs == null) {
                        logs = new LogsModel();
                    }
                    if (logs.logs == null) {
                        logs.logs = new LinkedList<>();
                    }
                }
            }
        }).start();


        requestInterface = new Retrofit.Builder()
                .baseUrl("https://devlogs.it-machine.ru/")
                .addConverterFactory(ScalarsConverterFactory.create())
                .client(httpClient.build())
                .build()
                .create(RequestInterface.class);
    }

    public static void init(Context context, String userToken, String appToken) {
        appContext = context.getApplicationContext();
        DevLogs.userToken = userToken;
        DevLogs.appToken = appToken;
    }

    public static DevLogs getInstance() {
        if (appContext == null || userToken == null || appToken == null) {
            throw new IllegalStateException("Call `DevLogs.init(context, userToken, appToken)` before calling this method.");
        }
        if (instance == null) {
            instance = new DevLogs();
        }
        return instance;
    }

    public void addLog(String name) {
        addLog(name, null);
    }

    public void addLog(final String name, final String value) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (lock) {
                    LogModel log = new LogModel();
                    log.name = name;
                    log.value = value;
                    log.date = new Date().getTime();

                    logs.logs.add(log);
                    try {
                        File logsOutput = new File(appContext.getFilesDir(), FILENAME);
                        Writer writer = new FileWriter(logsOutput);
                        new Gson().toJson(logs, writer);
                        writer.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public interface SendToServerCallback {
        void onSuccess();

        void onFailure(Throwable t);
    }

    public void sendToServer() {
        sendToServer(null);
    }

    public void sendToServer(final SendToServerCallback callback) {
        requestInterface.fetchSendLog(
                userToken,
                appToken,
                PLATFORM_ID,
                Build.VERSION.RELEASE,
                BuildConfig.VERSION_NAME,
                Build.MODEL,
                showLog(),
                null)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (callback != null) {
                            callback.onSuccess();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        if (callback != null) {
                            callback.onFailure(t);
                        }
                    }
                });
    }

    public String showLog() {
        synchronized (lock) {
            StringBuilder sb = new StringBuilder();
            for (LogModel log : logs.logs) {
                String dateString = DateFormat.getDateTimeInstance().format(log.date);
                sb.append("[");
                sb.append(dateString);
                sb.append("] ");
                sb.append(log.name);
                if (log.value != null) {
                    sb.append(" [");
                    sb.append(log.value);
                    sb.append("]");
                }
                sb.append("\n\n");
            }
            return sb.toString();
        }
    }

}
