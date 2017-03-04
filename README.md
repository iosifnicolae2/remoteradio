# RemoteRadio
Remote Radio is an Android application wich help you to listen radio streams and control it via a web interface.

#Screenshoots
<img src="https://raw.githubusercontent.com/iosifnicolae2/remoteradio/master/Screenshot_1487946718.png" width="300">
<img src="https://raw.githubusercontent.com/iosifnicolae2/remoteradio/master/screencapture-remote_android-mailo-ml-1487947195074.png" height="300px">

#Installation
Upload index.html file to your website.<br/>

Then replace in your android app(in Android Studio press: CTRL+SHIFT+R) `http://remote_android.mailo.ml` to your domain(ex: radio.example.com).<br/>

If you want to change radio stations in index.html file(add/remove inside of #channels the follow buttons).<br/>
Example:<br/>
```
  <button class="channel" data-link="http://www.aripisprecer.ro:8129">Aripi Spre Cer Predici</button>
```
<br/>
  where `http://www.aripisprecer.ro:8129` is your stream url and `Aripi Spre Cer Predici` is the station name.  
  If you have more questions you can ask me on Twitter here: [@nicolae_iosif](https://twitter.com/nicolae_iosif)
  
<br/>
<br/>
  Thank you!
