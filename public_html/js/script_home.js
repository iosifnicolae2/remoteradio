function getParentUrl() {
    var isInIframe = (parent !== window),
        parentUrl = null;

    if (isInIframe) {
        parentUrl = document.referrer;
    }

    return parentUrl;
}
var getUrlParameter = function getUrlParameter(sParam) {
    var sPageURL = decodeURIComponent(window.location.search.substring(1)),
        sURLVariables = sPageURL.split('&'),
        sParameterName,
        i;

    for (i = 0; i < sURLVariables.length; i++) {
        sParameterName = sURLVariables[i].split('=');

        if (sParameterName[0] === sParam) {
            return sParameterName[1] === undefined ? true : sParameterName[1];
        }
    }
};
$(function(){
  /* "http://"+$.cookie("ip-address")|| */
  var ip_address = getParentUrl();
  if (typeof ip_address === "undefined" || ip_address==null) {
    ip_address = "http://"+getUrlParameter('ip');
    if (typeof ip_address === "undefined" || ip_address==null)
      ip_address = 'http://192.168.2.103:5000';
  }else ip_address = ip_address.slice(0,-1);
  console.log(ip_address);


      var port = $.cookie("port")||'5000';
      var isPlaying = false;

//update status
$.get( ip_address+"/current_station", function( data ) {
    console.log("Status loaded: "+data);
    var json = jQuery.parseJSON(data);
    isPlaying = json.isPlaying || false;
    if(isPlaying)
      $("#stop").show();
    console.log(isPlaying);
    if(json.url.length>5)
    $("#now_playing").html("Se reda acum: "+json.title);
});

    $("#stop").click(function(){
      $.post( ip_address+"/stop")
        .done(function( data ) {
          isPlaying = false;
          $("#stop").hide();
          $(".done").show();
          setTimeout(function(){$(".done").hide();},1500);
        });
    });
        $(".channel").click(function(){
          console.log("Play: " + $(this).data('link')+" : "+$(this).text());
          $.post( ip_address+"/play", { command: "play", url: $(this).data('link'),name:$(this).text() })
            .done(function( data ) {
              $("#stop").show();
              isPlaying = true;
              $(".done").show();
              setTimeout(function(){$(".done").hide();},1500);
            });
        });

/* Sets the minimum height of the wrapper div to ensure the footer reaches the bottom */
function setWrapperMinHeight() {
  $('.container').css('minHeight', window.innerHeight - $('.nav').height() - $('.footer').height()-100);
}
/* Make sure the main div gets resized on ready */
setWrapperMinHeight();

/* Make sure the wrapper div gets resized whenever the screen gets resized */
window.onresize = function() {
  setWrapperMinHeight();
}
});
