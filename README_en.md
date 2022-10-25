# ImageMapView   [![](https://jitpack.io/v/sahooz/ImageMapView.svg)](https://jitpack.io/#sahooz/ImageMapView)

[中文](README.md)

An HTML map like widget in an Android view.

## Add dependency

1. Add this in your root project gradle setting file(build.gradle/settings.gradle):

```groovy
allprojects {
    repositories {
        //...
        maven { url 'https://jitpack.io' }
    }
}
or
dependencyResolutionManagement {
    //...
    repositories {
        //...
        maven { url 'https://jitpack.io' }
    }
}
```

2. Add the dependency in your module build.gradle

```groovy
dependencies {
    //...
    implementation 'com.github.sahooz:ImageMapView:1.0.1'
}
```

## Usage

1. Create a xml file that describe your map

```xml
<?xml version="1.0" encoding="utf-8"?>
<map xmlns:android="http://schemas.android.com/apk/res/android"
    width="500"
    height="500">
    <!--left, top, right, bottom-->
    <area name="Rect" shape="rect" coords="62,49,193,123" id="@+id/shape_rect"/>
    <!--x, y, radius-->
    <area name="Circle" shape="circle" coords="211,262,50" id="@+id/shape_circle"/>
    <!--x,y,x,y,x,y...-->
    <area name="Poly" shape="poly" coords="300,332,360,288,421,332,399,404,322,404" id="@+id/shape_poly"/>
</map>
```  

2. Add ImageMapView to your layout xml file:

```xml
<com.xinyanruanjian.imagemapview.ImageMapView
    android:id="@+id/imv"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:src="@drawable/shapes"
    android:scaleType="fitCenter"
    app:imageMap="@xml/map"/>
```

3. findViewById and set the listener:
```kotlin
findViewById<ImageMapView>(R.id.imv).setOnAreaClickListener { imv, area ->
    Log.i(javaClass.simpleName, "Area was clicked: $area")
    Toast.makeText(this, "${area.name} was clicked!", Toast.LENGTH_SHORT).show()
}
```

## LICENSE

```
MIT License

Copyright (c) 2022 吹白

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

