package com.example.background.workers;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.example.background.Constants;

public class BlurWorker extends Worker {

  private static final String TAG = BlurWorker.class.getSimpleName();

  public BlurWorker(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
    super(appContext, workerParams);
  }

  @NonNull
  @Override
  public Result doWork() {
    Context applicationContext = getApplicationContext();

    WorkerUtils.makeStatusNotification("Doing BlurWorker", applicationContext);
    WorkerUtils.sleep();

    String resourceUri = getInputData().getString(Constants.KEY_IMAGE_URI);

    try {
      //Bitmap picture = BitmapFactory.decodeResource(applicationContext.getResources(), R.drawable.test);
      if (TextUtils.isEmpty(resourceUri)) {
        Log.e(TAG, "Invalid input uri");
        throw new IllegalArgumentException("Invalid input uri");
      }

      ContentResolver resolver = applicationContext.getContentResolver();
      Bitmap picture = BitmapFactory.decodeStream(resolver.openInputStream(Uri.parse(resourceUri)));

      Bitmap blurredPicture = WorkerUtils.blurBitmap(picture, applicationContext);

      Uri pictureUri = WorkerUtils.writeBitmapToFile(applicationContext, blurredPicture);

      WorkerUtils.makeStatusNotification("Output is " + pictureUri.toString(), applicationContext);

      Data outputData = new Data.Builder()
          .putString(Constants.KEY_IMAGE_URI, pictureUri.toString())
          .build();

      return Result.success(outputData);
    } catch (Throwable throwable) {
      Log.e(TAG, "Error applying blur", throwable);
      return Result.failure();
    }
  }
}
