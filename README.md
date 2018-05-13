# Now Playing
Now Playing is a small piece of software that will display content based on what activity is happening in your media center. For example if your amplifier is set to the Music input you can make Now Playing display the current song playing in Spotify. When you switch to the Video input of the amplifier it can display what is currently playing on Plex.




## How it works

Now Playing works using a bunch of plugins. There are two types of plugins:

- **Activity Plugins**: Give you list of available activity to which you can attach a Now Playing plugin (i.e list of input of an amplifier or the list of activities available on your Harmony Hub)
- **Now Playing plugins**: Plugin that will display content on your client.

So for each activity that the **Activity Plugin** you selected offers you can choose a **Now Playing Plugin**. 


## How to run
### Install
The easiest way it to use docker:
```bash
docker run --name now-playing -p "4567:4567" -v "<where to save your config>:/config" -e "TZ=<your timezone>"  gonzague/now-playing
```

### Use
Then you should be able to go to *http://localhost:4567* to set up the application.

Once the you chose the **Activity Plugin** and a **Now Playing Plugin** there are two way to access the content.

#### Auto refreshing webpage
You can visit : *http://localhost:4567/now-playing*. Will auto detect width, height and scale and refresh automatically when necessary.

Ideal to use with an iPad where you can add the web page to your home screen as a web application so you will be able to enjoy a full screen view.

#### As a plain JPEG image

```
http://localhost:4567/now-playing.jpg?width=<desiredWidth>&height=<desiredHeight>&scale=<pixelRatio> 
```

Defaults to 1920*1080 with a scale of 1. For a high DPI screen, you need to change the pixel ratio i.e for an iPad width: 1024, height:768, scale: 2

Using this way your client needs to be able to refresh the image itself. 

Example Using feh (will refresh every 3 seconds):
```bash
feh -.ZR 3  "http://localhost:4567/now-playing.jpg?width=1024&height=768&scale=2"
```


## Plugins

### Activity Plugins

| Name | Description|
| ------------- | ----------- |
| Default | A single activity, i.e.: the screen will always show the same **Now Playing Plugin** |
| Yamaha Amplifier | Will allow one activity per input available (Tested with Yamaha RX-V573) |
| Harmony Hub | Will list down all the available activities on your harmony hub |
| Spotify | Two activies available: Playing and Stopped |

### Now Playing Plugins

| Name | Description |
| -------- | ------ |
| Black Screen (default) | Just a black image | 
| Photo Frame | Displays the pictures of a given Folder | 
| Spotify | Details of the songs currently playing in spotify  ![Spotify Now playing](https://raw.githubusercontent.com/lamarios/now-playing/master/screenshots/spotify.jpg)|
| Plex | Details of what's currently playing in Plex ![Plex now playing](https://raw.githubusercontent.com/lamarios/now-playing/master/screenshots/plex.jpg)| 
| GT Sports | Displays the daily races ![Daily races](https://raw.githubusercontent.com/lamarios/now-playing/master/screenshots/gtsports.jpg)|
