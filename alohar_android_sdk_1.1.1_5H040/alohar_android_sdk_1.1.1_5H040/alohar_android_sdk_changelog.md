## Alohar Android SDK Changelog

### Version 1.1.1 5H040 : Official Release
#### New features / Improvements
- New api to get the last known location.

```
public Location getLastKnownLocation()
```

- Changed update of device info and personal places to periodic intervals only and not on alohar service restarts.


### Version 1.1.0 5G300
#### Bug Fixes and Improvements
- Modified frequency of raw data upload in wifi only mode for faster detection of userstays.
- Miscellaneous crash fixes.


### Version 1.1.0 5G220
#### Bug Fix
- Bug fix for possible crash on initial Alohar Service restarts.


### Version 1.1.0 5G211
#### Bug Fixes and improvements
- Optimizations to reduce battery consumption.
- Optimizations to reduce raw data upload frequency and GPS usage in upload raw data on wifi only mode.

### Version 1.1.0 5F260 : Official Release
#### New features / Improvements (from Version 1.1.0 5E051)
- Alohar SDK now requires only one Android Service. Remove the following Android Service declaration from the AndroidManifest.xml of your app:

```
<service
android:name="com.alohar.context.core.AcxPersistentService"
android:enabled="true" />
```
- Added Android Studio project for Alohar Sample App.
- Miscellaneous fixes.

### Version 1.1.0 5E051 : Official Release
#### New Features / Improvements
- Changed Home/Work suggestion API so id based suggestionId is no longer needed.

```
public void acceptImportantPlaceSuggestion(final AcxImportantPlaceSuggestion suggestion, 
final String placeName,
final AcxServerCallback<AcxImportantPlaceSuggestion> callback);

public void rejectImportantPlaceSuggestion(final AcxImportantPlaceSuggestion suggestion,
final AcxServerCallback<AcxImportantPlaceSuggestion> callback);
```

#### Bug Fixes

### Version 1.1.0 Beta1
Initial launch on alohar dev portal