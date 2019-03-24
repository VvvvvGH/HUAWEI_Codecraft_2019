#!/usr/bin/env bash
ffmpeg -r 5 -i simulatePictures/%05d.jpg -vcodec mpeg4 video.mp4