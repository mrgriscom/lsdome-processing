javac -source 1.6 -target 1.6 -cp ~/processing-2.2.1/core/library/core.jar me/lsdo/processing/*.java
jar cf ../library/lsdome.jar *
rm me/lsdo/processing/*.class

cp ../library/lsdome.jar ~/Documents/Processing/libraries/lsdome/library/
