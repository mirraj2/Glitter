#!/bin/bash

git -C /root/ox pull &
git -C /root/bowser pull &

git -C /root/Glitter fetch
git -C /root/Glitter checkout $1
git -C /root/Glitter pull

wait

ant -f /root/Glitter/build.xml build

/root/Glitter/build/restart.sh

