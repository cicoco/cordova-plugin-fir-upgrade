var exec = require('cordova/exec');

exports.versionCheck = function (params, success, error) {
    exec(success, error, 'FirUpgrade', 'versionCheck', params);
};
