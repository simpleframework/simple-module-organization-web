var AccountStatFormPage = {
  sms_sent : function(obj) {
    var mobile = $F(obj.previous());
    var act = $Actions['AccountMobileBindingPage_sentcode'];
    var callback = $UI.doMobileSentInterval;
    act.jsCompleteCallback = function() {
      callback(obj, true);
    };
    act("mobile=" + mobile);
  },

  mail_sent : function(obj) {
    var mail = $F(obj.previous());
    var act = $Actions['AccountMailBindingPage_sentcode'];
    var callback = $UI.doMobileSentInterval;
    act.jsCompleteCallback = function() {
      callback(obj, true);
    };
    act("mail=" + mail);
  }
};