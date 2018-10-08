var exec = require('cordova/exec');

var baiduLocation = {
  getCurrentPosition: function(successFn, failureFn) {
    exec(successFn, failureFn, 'BaiduLocation', 'getCurrentPosition', []);
  },
  watchPosition: function(successFn, failureFn, span) {
    exec(successFn, failureFn, 'BaiduLocation', 'watchPosition', [span]);
  },
  clearWatch: function(successFn, failureFn) {
    exec(successFn, failureFn, 'BaiduLocation', 'clearWatch', []);
  }
};

module.exports = baiduLocation;
