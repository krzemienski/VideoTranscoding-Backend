#!/bin/bash
sudo add-apt-repository -y ppa:jonathonf/ffmpeg-3
sudo apt update -y && sudo apt install -y ffmpeg libav-tools x264 x265 vpx vorbis libvorbis theora libogg gpl version3 nonfree postproc libaacplus libass libcelt libfaac libfdk-aac libfreetype libmp3lame libopencore-amrnb libopencore-amrwb libopenjpeg openssl libopus libschroedinger libspeex libtheora libvo-aacenc libvpx libxvid 
ffmpeg