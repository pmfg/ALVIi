# ALVIi: Android Log Viewer Interface imc

* Application use protocol of communication ( <a href="https://github.com/LSTS">IMC - LTST/FEUP</a> ) to:

      . Detect systems in network;
      . Download and review logs;
      . View info of system;
      
* The Application have two mode of operations:

      -> Offline Mode - No wifi available, only available option to review downloaded files;
      -> Online Mode - Wifi and info of system available.
      
* Note:
To compile without errors is necessary add to project a Google Maps API key.

    Alternatively, follow the directions here:
    https://developers.google.com/maps/documentation/android/start#get-key

    Once you have your key (it starts with "AIza"), add the string to string.xml resource.
```
<string name="google_maps_key" templateMergeStrategy="preserve" translatable="false">AIzaABCDEFGHIJKLMNOPQR</string>
```
