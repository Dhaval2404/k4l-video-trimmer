/*
 * MIT License
 *
 * Copyright (c) 2016 Knowledge, education for life.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package life.knowledge4.videotrimmer.utils;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Formatter;
import java.util.Locale;

import life.knowledge4.videotrimmer.interfaces.OnTrimVideoListener;

public class TrimVideoUtils {

    private static final String TAG = TrimVideoUtils.class.getSimpleName();

    public static void startTrim(Context context, @NonNull File src, @NonNull String dst, long startMs, long endMs, @NonNull OnTrimVideoListener callback) throws IOException {
        final String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        final String fileName = "MP4_" + timeStamp + ".mp4";
        final String filePath = dst + fileName;

        File file = new File(filePath);
        file.getParentFile().mkdirs();
        Log.d(TAG, "Generated file path " + filePath);
        genVideoUsingMp4Parser2(context, src, file, startMs, endMs, callback);
    }

    private static void genVideoUsingMp4Parser2(final Context context, @NonNull File src, @NonNull final File dst, long startMs, long endMs, @NonNull final OnTrimVideoListener callback) throws IOException {
        String start = convertSecondsToTime(startMs / 1000);
        String duration = convertSecondsToTime((endMs - startMs) / 1000);

        /* *
         Cropped video ffmpeg instruction description:
         * ffmpeg -ss START -t DURATION -i INPUT -c copy OUTPUT
             -ss start time, such as: 00:00:20, starting from 20 seconds;
             -t duration, such as: 00:00:10, indicates intercepting a 10-second video;
             -i input, followed by a space, followed by the input video file;
             -c copy indicate the encoding format of the video and audio to be used, where copy is designated as the original copy;
             INPUT, input video file;
             OUTPUT, output video file
         */
       // String cmd = "-ss " + start + " -t " + duration + " -i \"" + src.getAbsolutePath() + "\" -vcodec copy -acodec copy \"" + dst.getAbsolutePath()+"\"";
        String[] command = new String[]{"-ss", start,
                                        "-i",src.getAbsolutePath(),
                                        "-t", duration,
                                        /*"-vcodec", "copy",
                                        "-acodec", "copy",*/
                                        "-c", "copy",
                                    dst.getAbsolutePath()};
       try {
            FFmpeg.getInstance(context).execute(command, new ExecuteBinaryResponseHandler() {
                @Override public void onFailure(String s) {
                    callback.onError(s);
                }

                @Override public void onSuccess(String s) {
                    callback.getResult(Uri.fromFile(dst));
                }

                @Override public void onStart() {
                    callback.onTrimStarted();
                }

                @Override public void onFinish() {
                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            e.printStackTrace();
        }
    }

    private static String convertSecondsToTime(long seconds) {
        String timeStr = null;
        int hour = 0;
        int minute = 0;
        int second = 0;
        if (seconds <= 0)
            return "00:00";
        else {
            minute = (int) seconds / 60;
            if (minute < 60) {
                second = (int) seconds % 60;
                timeStr = "00:" + unitFormat(minute) + ":" + unitFormat(second);
            } else {
                hour = minute / 60;
                if (hour > 99)
                    return "99:59:59";
                minute = minute %  60 ;
                second = (int) (seconds - hour * 3600 - minute * 60);
                timeStr = unitFormat(hour) + ":" + unitFormat(minute) + ":" + unitFormat(second);
            }
        }
        return timeStr;
    }

    private  static  String  unitFormat ( int  i ) {
        String retStr =  null ;
        if (i >= 0 && i < 10)
            retStr = "0" + Integer.toString(i);
        else
            retStr = "" + i;
        return retStr;
    }

    public static String stringForTime(int timeMs) {
        int totalSeconds = timeMs / 1000;

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;

        Formatter mFormatter = new Formatter();
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }
}
