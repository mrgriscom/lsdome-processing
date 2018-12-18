# lsdome library for Processing

[![Build Status](https://travis-ci.org/shen-tian/lsdome-processing.svg?branch=master)](https://travis-ci.org/shen-tian/lsdome-processing)
[![Download](https://api.bintray.com/packages/shen-tian/maven/lsdome-processing/images/download.svg)](https://bintray.com/shen-tian/maven/lsdome-processing/_latestVersion)

This is the shared library powering the [Limitless Slip Dome](https://github.com/shen-tian/l-s-dome), Prometheus, and other LED mesh projects.
It can be used as a Processing 3 [contributed library](https://github.com/processing/processing/wiki/How-to-Install-a-Contributed-Library) for making Processing sketches.
This library also contains the infrastructure to power LED animations that don't depend on Processing.

## Goals

This library handles awareness of the pixel geometry in physical space, various utility functions and skeleton animations to help with mapping virtual animation canvases onto the pixel mesh, and talking to pixels via [Open Pixel Control](http://openpixelcontrol.org/).

## Build/Install

There's two main ways of using the library: as a Processing Contributed library, 
or as a Java library.

### As a Processing library

This is the most commonly used method.

1. Install Processing:

    ```
    cd ~
    wget http://download.processing.org/processing-3.3.7-linux64.tgz
    tar xvzf processing-3.3.7-linux64.tgz
    ```

2. Run the Processing IDE to create the local working directory.

    `~/processing-3.3.7/processing`

3. Clone this repo and enter the directory

4. Build

    `./gradlew makeArtifact`

5. Unpack the built library into your Processing install:

    ```
    PROCESSING_WD=~/sketchbook  # linux
    PROCESSING_WD=~\Documents\Processing  # mac
    rm -r $PROCESSING_WD/libraries/lsdome/
    unzip ./build/distributions/lsdome.zip -d $PROCESSING_WD/libraries/
    ```

### Plain Java

The library is published at the Maven repo on _jcenter_, so you just need to add it as a dependency into your
 build-runner/project tool of choice. **Typically the published jar is very out of date, as we're often frantically hacking close to an event deadline.**

If you are using Gradle:

    repositories {
        jcenter()
    }

    dependencies {
        compile 'me.lsdo.processing:lsdome-processing:0.9.4'
    }

in the right place in your `gradle.build`. If you are using Leiningen:

    :repositories [["jcenter" {:url "http://jcenter.bintray.com"}]]
    :dependencies [
        [me.lsdo.processing/lsdome-processing "0.9.4"]]

in your `project.clj` should do the trick.

#### Building the JAR

If you need to do this, for whatever reason, simple `git clone` the repo, 
ensure you've got Java installed, and go

    ./gradlew build

builds the JAR and places it in `/build/ibs/`.

#### Publishing the JAR

To do this, you need the _jcenter_ API keys. Gradle will look for 
`gradle.properties` in the project root folder. It expect something like

    bintrayUser=user-name
    bintrayApiKey=apikeyxxxxxxxxxxxxxxx
    
No quotes. Then, update version number in `build.gradle`, and `gradle bintrayupload`.

## Use

### Config

The `config.properties` file contains important settings:

- `geometry`: what kind of mesh is being displayed on:
  - `lsdome` -- the triangular panels of the limitless slip dome; specify the number of panels (2, 6, 13, or 24) with `num_panels`
  - `prometheus` -- butterfly wings
- `opchostname` -- domain or IP address of the OPC server. Specify additional OPC servers via `opchostname2`, etc.
- `opcport` -- port for the OPC server(s)

### Animations

There's a few ways to create animations with the library.

* _Canvas_ - easiest way possible. Start with almost any Processing sketch you 
created or borrowed off of the internet, and add a few lines of code to make it light up 
some LEDs. You'd be essentially using the LEDs as a bright but low res display.
* _WindowAnimation_ - mapping a pre-made window/rectangle of pixels onto the mesh (though not necessarily a Processing window)
* _XY Animation_ - you are rendering a scene using some Maths and shit. Thus, 
have some way of turning XY-coordinates (a 2D vector, actually) into a colour. 
A typical usecase would be rainbow fractals.
* _Dome Animation_ - this is the most complicated one, and involves understanding the addressing system of the LEDs in
the triangular panels a bit. Best to read some code comments.

### Canvas

Simplest is `CanvasSketch`. This gives you the shortest path to porting an existing
sketch to the dome.

Simple add these lines

    import me.lsdo.processing.*;

    CanvasSketch sketch;

to near the top of the file. Then, to the `setup` method, add

    sketch = Driver.makeCanvas(this);

and to the end of the `draw` method:

    sketch.draw();

have a look at the `stripes` example to see this in action. What this does is
to sample directly from the sketch, after is has been rendered. It applies a 
bit of anti-aliasing to smooth things out.

### XY Animation

This mode is for when you render a scene via its XY coorodinate. In essence. You'll
be implementing the function (pseudocode):

    color drawScene(int xCoord, int yCoord, int time)

Have a look at the `Cloud` example for how this works. It doesn't actually use
X and Y coordinates, but a vector. The color is calculated, and then drawn onto
the Sketch.

Note that DomeAnimation doesn't depend on an PApplet, so theoretically, can be 
ran in a headless scenario.

### Dome Animation

This mode is when you are working with the Dome directly. That way, you can play
with the funky UVW co-ordinate system. See `Kaleidoscope` for a good example.

## Design Notes

_This is somewhat out of date_

The general idea:

* `PixelMesh` class represents the pixel mesh geometry in physical space.
* `OPC` class contains the OPC client. Based on Micah Scott's code from the Fadecandy examples.
* `DomeAnimation` is an abstract class. Extend it to animate the Dome directly, UVW geometry and all.
* `XYAnimation` is an abstract class too. It extends `DomeAnimation`. It abstracts over that geometry.
Converts it to XY coords.
* `PixelGridSketch` wraps both the above animation, together with a `PApplet`. It uses information from the
`Dome` object to draw onto the sketch, so it's really just as feedback. Stuff that gets drawn onto the sketch
via the usual Processing means won't show up on the dome. For that, you'll need...
* `CanvasSketch`, which takes a `PApplet`, and samples its canvas, using that to colour the `Dome`
structure. It also does the AA to compensate for the extreme low resolution of the
dome.

`TriCoord`, `DomeCoord`, `LayoutUtil`, `MathUtil` handles the 2D geometry. It's kind of complicated, read
the source for hints on how they work.
`Config` is more of a placeholder right now.
`OpcColor` has some color related functions.
