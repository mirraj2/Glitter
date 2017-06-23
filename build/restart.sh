#!/bin/sh

screen -S glitter -X quit
screen -S glitter -d -m ant -f /root/Glitter/build.xml GlitterServer
echo Server started.
