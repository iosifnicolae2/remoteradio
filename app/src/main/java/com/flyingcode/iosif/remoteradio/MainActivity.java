package com.flyingcode.iosif.remoteradio;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.flyingcode.iosif.remoteradio.player.ExtractorRendererBuilder;
import com.flyingcode.iosif.remoteradio.player.RadioPlayer;
import com.google.android.exoplayer.ExoPlaybackException;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.MediaCodecTrackRenderer;
import com.google.android.exoplayer.MediaCodecUtil;
import com.google.android.exoplayer.drm.UnsupportedDrmException;
import com.google.android.exoplayer.metadata.id3.GeobFrame;
import com.google.android.exoplayer.metadata.id3.Id3Frame;
import com.google.android.exoplayer.metadata.id3.PrivFrame;
import com.google.android.exoplayer.metadata.id3.TxxxFrame;
import com.google.android.exoplayer.text.Cue;
import com.google.android.exoplayer.util.Util;
import com.koushikdutta.async.http.server.AsyncHttpServer;
import com.koushikdutta.async.http.server.AsyncHttpServerRequest;
import com.koushikdutta.async.http.server.AsyncHttpServerResponse;
import com.koushikdutta.async.http.server.HttpServerRequestCallback;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity implements RadioPlayer.Listener, RadioPlayer.CaptionListener, RadioPlayer.Id3MetadataListener {

    PlaRadio station = new PlaRadio("http://ascultaradiogosen.no-ip.org:8125/;",
                                "Radio Gosen",
                                "Local");

    /*
        public static final String stations[] = new String[]{
            "http://mobile.aripisprecer.ro:8127",//Stream AAC
            "http://radio.aripisprecer.ro:80",//Stream mp3
            "http://radiohq.aripisprecer.ro:8133",//Stream mp3 High Quality 320kbps
            "http://worship.aripisprecer.ro:80",//Stream mp3 Worship
            "http://predici.aripisprecer.ro:443",//Stream mp3 Predici
            "http://international.aripisprecer.ro:443"//Stream mp3 International
    };
     */

    private static final Pattern pattern_url = Pattern.compile("(?<=url=\\[)(.*?)(?=\\])");
    private static final Pattern pattern_title = Pattern.compile("(?<=name=\\[)(.*?)(?=\\])");
    private static final Pattern pattern_command = Pattern.compile("(?<=command=\\[)(.*?)(?=\\])");
    private boolean playPause = true;
    private final String ip_address = Utils.getIPAddress(true);
    private int port_listen = 5000;
    private RadioPlayer player;
    private boolean playerNeedsPrepare;
    private EventLogger eventLogger;
    private String TAG="RadioActivity TAG";
    private String errorString;

    /**
     * remain false till media is not completed, inside OnCompletionListener make it true.
     */
    int retry=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable ex) {
                ex.printStackTrace();

                MainActivity.this.finish();
                if (retry++ < 5) {

                    Intent mIntent = new Intent(MainActivity.this, MainActivity.class);
                    startActivity(mIntent);
                }
            }
        });

        playRadio(station);

        Button stop_btn = (Button) findViewById(R.id.stop_btn);
        if (stop_btn != null) {
            stop_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!playPause)
                    stopRadio();
                    else
                        playRadio(station);
                }
            });
        }


        //playRadio(station);
        AsyncHttpServer server = new AsyncHttpServer();


        server.get("/", new HttpServerRequestCallback() {
            @Override
            public void onRequest(AsyncHttpServerRequest request, AsyncHttpServerResponse response) {
                response.getHeaders().set("Access-Control-Allow-Origin", "http://www.remote-android.mailo.ml");
                //
                //response.send("HEHE!");
                try {
                    response.redirect("http://www.remote-android.mailo.ml?ip="+ URLEncoder.encode(ip_address+":"+port_listen, "utf-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                //response.send("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\"><html xmlns=\"http://www.w3.org/1999/xhtml\"> <head> <title>Remote Android Radio</title> <style type=\"text/css\"> body, html{margin: 0; padding: 0; height: 100%; overflow: hidden;}#content{position:absolute; left: 0; right: 0; bottom: 0; top: 0px;}</style> </head> <body> <div id=\"content\"> <iframe width=\"100%\" height=\"100%\" frameborder=\"0\" src=\"http://www.remote-android.mailo.ml\"/> </div></body></html>");
            }
        });
       /* server.get("/settings.html", new HttpServerRequestCallback() {
            @Override
            public void onRequest(AsyncHttpServerRequest request, AsyncHttpServerResponse response) {

                response.send("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\"><html xmlns=\"http://www.w3.org/1999/xhtml\"> <head> <title>Remote Android Radio</title> <style type=\"text/css\"> body, html{margin: 0; padding: 0; height: 100%; overflow: hidden;}#content{position:absolute; left: 0; right: 0; bottom: 0; top: 0px;}</style> </head> <body> <div id=\"content\"> <iframe width=\"100%\" height=\"100%\" frameborder=\"0\" src=\"http://www.remote-android.mailo.ml/settings.html\"/> </div></body></html>");
            }
        });*/


        server.post("/play", new HttpServerRequestCallback() {
            @Override
            public void onRequest(AsyncHttpServerRequest request, AsyncHttpServerResponse response) {
                String  data =  request.getBody().get().toString();
                response.getHeaders().set("Access-Control-Allow-Origin", "http://www.remote-android.mailo.ml");

                Matcher matcher;
                String url="",title="",command="";

                matcher = pattern_url.matcher(data);
                if (matcher.find()) {
                    url = matcher.group(0);
                }

                matcher = pattern_title.matcher(data);
                if (matcher.find()) {
                    title = matcher.group(0);
                }
                matcher = pattern_command.matcher(data);
                if (matcher.find()) {
                    command = matcher.group(0);
                }
                if(command.equals("play")){
                    station.setUrl(url);
                    station.setTitle(title);
                    station.setUser("Online");
                    response.send("i will play");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            playRadio(station);
                        }//public void run() {
                    });
                }else
                response.send("OK");


            }
        });

        server.post("/stop", new HttpServerRequestCallback() {
            @Override
            public void onRequest(AsyncHttpServerRequest request, AsyncHttpServerResponse response) {
                stopRadio();
                response.getHeaders().set("Access-Control-Allow-Origin", "http://www.remote-android.mailo.ml");
                response.send("i will stop stream");
            }
        });

        server.get("/current_station", new HttpServerRequestCallback() {
            @Override
            public void onRequest(AsyncHttpServerRequest request, AsyncHttpServerResponse response) {

                response.getHeaders().set("Access-Control-Allow-Origin", "http://www.remote-android.mailo.ml");
                response.send(station.toString());
            }
        });


        server.get("/ping", new HttpServerRequestCallback() {
            @Override
            public void onRequest(AsyncHttpServerRequest request, AsyncHttpServerResponse response) {
                response.getHeaders().set("Access-Control-Allow-Origin", "http://www.remote-android.mailo.ml");
                response.send("ok");
            }
        });

        server.listen(port_listen);


        TextView ip = (TextView) findViewById(R.id.ip_address);
        if (ip != null) {
            ip.setText(ip_address);
        }
        //String ecoded_ip = Base64.encodeToString( ip_address.getBytes(), Base64.DEFAULT );
        //ip.setText(ecoded_ip);

    }

    private RadioPlayer.RendererBuilder getRendererBuilder() {
        String userAgent = Util.getUserAgent(this, "RPlayer");
        /*switch (contentType) {
            case Util.TYPE_SS:
                return new SmoothStreamingRendererBuilder(this, userAgent, contentUri.toString(),
                        new com.flyingcode.iosif.remoteradio.SmoothStreamingTestMediaDrmCallback());
            case Util.TYPE_DASH:
                return new DashRendererBuilder(this, userAgent, contentUri.toString(),
                        new com.flyingcode.iosif.remoteradio.WidevineTestMediaDrmCallback(contentId, provider));
            case Util.TYPE_HLS:
                return new HlsRendererBuilder(this, userAgent, contentUri.toString());
            case Util.TYPE_OTHER:
                return new ExtractorRendererBuilder(this, userAgent, contentUri);
            default:
                throw new IllegalStateException("Unsupported type: " + contentType);
        }*/
        return new ExtractorRendererBuilder(this, userAgent, Uri.parse(station.getUrl()));
    }

    private void preparePlayer(boolean playWhenReady) {

            player = new RadioPlayer(getRendererBuilder());
            player.addListener(this);
            player.setMetadataListener(this);
            eventLogger = new EventLogger();
            eventLogger.startSession();
            player.addListener(eventLogger);
            player.setInfoListener(eventLogger);
            player.setInternalErrorListener(eventLogger);

       player.prepare();
        player.seekTo(0);
        player.setPlayWhenReady(playWhenReady);
        player.setBackgrounded(true);

        playPause = false;

        station.setIsplaying(true);
    }


    @Override
    public void onCues(List<Cue> cues) {


    }

    private void playRadio(PlaRadio r) {
        Log.w("play some radio ",r.toString());
        station = r;
        releasePlayer();
        playerNeedsPrepare = true;
        preparePlayer(true);



    }

    private void stopRadio(){

    releasePlayer();
    }

    @Override
    public void onStateChanged(boolean playWhenReady, int playbackState) {
        if (playbackState == ExoPlayer.STATE_ENDED) {
            playerStopped();
        }
        String text = "playWhenReady=" + playWhenReady + ", playbackState=";
        switch(playbackState) {
            case ExoPlayer.STATE_BUFFERING:
                text += "buffering";
                break;
            case ExoPlayer.STATE_ENDED:
                text += "ended";
                break;
            case ExoPlayer.STATE_IDLE:
                text += "idle";
                break;
            case ExoPlayer.STATE_PREPARING:
                text += "preparing";
                break;
            case ExoPlayer.STATE_READY:
                text += "ready";
                break;
            default:
                text += "unknown";
                break;
        }
        Log.d("RPlayer onStateChanged",text);
    }

    private void playerStopped() {
        Log.w("player stoped","try to send request to user");
    }

    @Override
    public void onError(Exception e) {
        errorString = null;
        if (e instanceof UnsupportedDrmException) {
            // Special case DRM failures.
            UnsupportedDrmException unsupportedDrmException = (UnsupportedDrmException) e;
            errorString = getString(Util.SDK_INT < 18 ? R.string.error_drm_not_supported
                    : unsupportedDrmException.reason == UnsupportedDrmException.REASON_UNSUPPORTED_SCHEME
                    ? R.string.error_drm_unsupported_scheme : R.string.error_drm_unknown);
        } else if (e instanceof ExoPlaybackException
                && e.getCause() instanceof MediaCodecTrackRenderer.DecoderInitializationException) {
            // Special case for decoder initialization failures.
            MediaCodecTrackRenderer.DecoderInitializationException decoderInitializationException =
                    (MediaCodecTrackRenderer.DecoderInitializationException) e.getCause();
            if (decoderInitializationException.decoderName == null) {
                if (decoderInitializationException.getCause() instanceof MediaCodecUtil.DecoderQueryException) {
                    errorString = getString(R.string.error_querying_decoders);
                } else if (decoderInitializationException.secureDecoderRequired) {
                    errorString = getString(R.string.error_no_secure_decoder,
                            decoderInitializationException.mimeType);
                } else {
                    errorString = getString(R.string.error_no_decoder,
                            decoderInitializationException.mimeType);
                }
            } else {
                errorString = getString(R.string.error_instantiating_decoder,
                        decoderInitializationException.decoderName);
            }
        }
        if (errorString != null) {
            Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_LONG).show();
        }
        playerNeedsPrepare = true;
    }

    @Override
    public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees,
                                   float pixelWidthAspectRatio) {
    }
    @Override
    public void onId3Metadata(List<Id3Frame> id3Frames) {
        for (Id3Frame id3Frame : id3Frames) {
            if (id3Frame instanceof TxxxFrame) {
                TxxxFrame txxxFrame = (TxxxFrame) id3Frame;
                Log.i(TAG, String.format("ID3 TimedMetadata %s: description=%s, value=%s", txxxFrame.id,
                        txxxFrame.description, txxxFrame.value));
            } else if (id3Frame instanceof PrivFrame) {
                PrivFrame privFrame = (PrivFrame) id3Frame;
                Log.i(TAG, String.format("ID3 TimedMetadata %s: owner=%s", privFrame.id, privFrame.owner));
            } else if (id3Frame instanceof GeobFrame) {
                GeobFrame geobFrame = (GeobFrame) id3Frame;
                Log.i(TAG, String.format("ID3 TimedMetadata %s: mimeType=%s, filename=%s, description=%s",
                        geobFrame.id, geobFrame.mimeType, geobFrame.filename, geobFrame.description));
            } else {
                Log.i(TAG, String.format("ID3 TimedMetadata %s", id3Frame.id));
            }
        }
    }

    private void releasePlayer() {
        playPause = true;
        if (player != null) {
            player.release();
            player = null;
//            eventLogger.endSession();
//            eventLogger = null;
        }
        if(station!=null)
            station.setIsplaying(false);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        releasePlayer();
    }

}
