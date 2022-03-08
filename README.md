# Cavity
<p align="center">
  <img src="/assets/home.jpg" alt="Cavity screenshots">
</p>

Cavity is a wine cellar manager for Android.
It follows Material Design 2 principles to have a clear and simple user interface.

<p align="center" width="200px">
  <a href="/assets/google-play-badge">
    Get it on google play
  </a>
</p>

## Features
- Stock management
- Highlights peaking bottles
- Stock history
- Handle firends (Give a bottle to someone, share with a friend...)
- Buy & comsumptions stats
- Search
- Tasting sessions: decanting notification, chill a bottle

## Design
Cavity use gold as its color primary and no secondary color to emphasize luxurious and classy wine atmosphere.

Primary gold is used sparingly as it breaks the gold effect when using it in large surfaces.

The app is designed to be used in dark mode, but the light version is also supported for light mode lovers.

## Run the project locally
Clone the project, and you're ready to build and run it via Android Studio.

## Architecture
Cavity use Kotlin, Coroutines, Room, LiveData, ViewPager2 with a standard MVVM pattern

## Release process
This project uses CI/CD.
Workflows are trigerred when pushing a tag, and publish the app on the Play Store on production channel.

Things to do to prepare a release:

- Update `/whatsnew directory`
- Update `/app/build.gradle`: increment version code & update __version name__
- Merge target code into master branch
- Create and push a tag named __version name__

## Run the monkey
Pin your app (Enable pinned app in android settings, then pin Cavity)

__WARNING: the monkey might play your music very loud at random moments when using it__

Run this command from your terminal:

```bash
./adb shell monkey -p com.louis.app.cavity -v 1000 --pct-nav 0
```

where `-v 1000` is the number of inputs sent to the phone.

<sup><sup>“Google Play and the Google Play logo are trademarks of Google LLC.”</sup></sup>
