#!/usr/bin/env bash
ffmpeg -r 5 -i simulatePictures/%05d.jpg -vcodec libx264 video.mp4