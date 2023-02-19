# My Trip (Rider) Android App

This is the repository for My Trip (Rider) app that students build during [the Android Degree at Barmej.com](https://www.barmej.com/degree/android)

This app is a simple Android app that helps people to request a taxi easily by choosing the starting location and destination.

By building this app, students will learn about new topics like Google Map and Firebase

To use this repository, fork/clone it, or download a zip using the green "Clone or download" button at the top of the file list.
After you clone the project you need to add your own [Google Map API key](https://developers.google.com/maps/documentation/android-sdk/get-api-key) to the AndroidManifest.xml file

```xml
<meta-data
     android:name="com.google.android.geo.API_KEY"
     android:value="YOUR_API_KEY" />
```

Also you need to create a new firebase project for this app and download `google-services.json` file then copy it to your project's `app` folder.
After creating new firebase project you need to enable Email/Password and Anonymous login and create a new Realtime database to make the project ready.

Note: The same firebase project must be used for both Rider and Driver apps to connect them to the same database and share the information between the two apps.

# Screenshots
<img src="screenshots/screen_1.png" width="270"><img src="screenshots/screen_2.png" width="270"><img src="screenshots/screen_3.png" width="270">
<img src="screenshots/screen_4.png" width="270"><img src="screenshots/screen_5.png" width="270"><img src="screenshots/screen_6.png" width="270">
<img src="screenshots/screen_7.png" width="270"><img src="screenshots/screen_8.png" width="270"><img src="screenshots/screen_9.png" width="270">

# Contributing
All contributions are welcome and gratefully accepted.

# License
[![License: MPL 2.0](https://img.shields.io/badge/License-MPL%202.0-brightgreen.svg)](https://opensource.org/licenses/MPL-2.0)

A copy of the license is also available in the [license file](LICENSE).

# Demo
<a href='https://play.google.com/store/apps/details?id=com.barmej.rideapplication&utm_source=github&pcampaignid=MKT-Other-global-all-co-prtnr-py-PartBadge-Mar2515-1'><img width="200" alt='Get it on Google Play' src='https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png'/></a>
