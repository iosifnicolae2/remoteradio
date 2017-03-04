$(function(){
    $( "form#settings" ).submit(function( event ) {
    event.preventDefault();
    $.each($( this ).serializeArray(), function(index,value ) {
      $.cookie(value['name'], value['value']);
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
