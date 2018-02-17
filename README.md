# VideoTranscoding
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/6b9cd649a14a4431882a51dd2c779e85)](https://www.codacy.com/app/luiscajl/VideoTranscoding?utm_source=github.com&utm_medium=referral&utm_content=luiscajl/VideoTranscoding&utm_campaign=badger)
[![Maintainability](https://api.codeclimate.com/v1/badges/a3de0e21cd574e78341e/maintainability)](https://codeclimate.com/github/luiscajl/VideoTranscoding/maintainability)
[![Build Status](https://travis-ci.org/luiscajl/VideoTranscoding.svg?branch=master)](https://travis-ci.org/luiscajl/VideoTranscoding)
![Version](https://img.shields.io/badge/version-0.1-brightgreen.svg?style=flat)
[![License badge](https://img.shields.io/badge/license-Apache2-orange.svg)](http://www.apache.org/licenses/LICENSE-2.0)

This application transcode a video that you send on all formats what you want and diferent resolutions. It´s build on a docker container to simplify the installation.
#### Known errors:
1. You can´t save your transcoded videos on your machine.

## Run it:
1. Install [docker](https://docs.docker.com/engine/installation/) for your machine

2. Run this command on your terminal
```sh
docker run -d -p 8080:8080 --name videotranscoding luiscajl/videotranscoding:Release-0.1
```
3. Visit [localhost](http://localhost:8080/) on your web browser
4. Stop docker container when you finished
```sh
docker stop videotranscoding
```

## Develop it:
1. Clone respository:
```sh
git clone https://github.com/luiscajl/VideoTranscoding.git 
```
2. Run this script to install ffmpeg on your mac.
```sh
sh /scripts/install_ffmpeg_macosx.sh
```
or this for linux
```sh
sh /scripts/install_ffmpeg_linux.sh
```
3. Now you can import the project on your ide and start SpringBoot Application or develop the project.


## Next objectives (Release 0.5):
- [x] Add support for more audio languages and subtitles.
- [x] Create a full Api Rest for transcode service.

The project is my final project of my degree. I hope to finish it on Jun´18. 

## Screenshots:
<p align="center">
  <img src="https://github.com/luiscajl/VideoTranscoding/blob/master/screens/screenIndex.png"/>
</p>
<p align="center">
  <img src="https://github.com/luiscajl/VideoTranscoding/blob/master/screens/screenTranscode.png"/>
</p>




