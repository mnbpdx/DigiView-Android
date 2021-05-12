package com.example.ijdfpvviewer;

import android.os.Handler;
import android.os.Message;
import android.util.Log;


import org.bytedeco.ffmpeg.avcodec.AVCodec;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.AndroidFrameConverter;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegLogCallback;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;

import java.io.InputStream;

import static org.bytedeco.ffmpeg.global.avutil.*;


public class VideoReader {

        private Handler frameHandler;
        private InputStream inputStream;
        private AndroidFrameConverter converterToBitmap = new AndroidFrameConverter();

        VideoReader(InputStream input, Handler videoHandler){
            frameHandler = videoHandler;
            inputStream = input;
        }

        public void start() {

            Handler mHandler = frameHandler;
            //New thread to perform background operation
            new Thread(new Runnable() {
                @Override
                public void run() {
                    av_log_set_level(AV_LOG_TRACE);
                    FFmpegLogCallback.set(); // debug ffmpeg message

                    FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(inputStream);
                    grabber.setVideoStream(0);
                    //grabber.setOption("fflags", "nobuffer");
                    grabber.setFormat("h264");
                    grabber.setVideoCodec(avcodec.AV_CODEC_ID_H264);
                    grabber.setOption("fflags", "nobuffer");
                    grabber.setOption("preset", "ultrafast");
                    grabber.setFrameRate(60);
                    //grabber.setVideoBitrate(25000);
                    grabber.setVideoOption("tune", "zerolatency");


                    Log.d("GRABBER","initialized");

                    Message m = new Message();

                    // Open video file
                    try {
                        grabber.start();
                        Log.d("GRABBER","started");
                        Frame frame = null;
                        do  {
                            frame = grabber.grabImage();
                            m.obj = converterToBitmap.convert(frame);;
                            mHandler.dispatchMessage(m);
                        } while (frame != null);

                        // Close the video file
                        grabber.release();

                    } catch (FrameGrabber.Exception e) {
                        e.printStackTrace();
                    }
                }

            }).start();


        }

}