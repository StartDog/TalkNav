This is a android app written in kotlin that compiles with Android Studio. 
It uses text to speach to speak your speed, direction of travel and distance from home. 
You set the home. 
It has a visual mode which adds on a display of the speed and direction of travel and
distance from home plus an altitude (which is kind of inaccurate).

There are four buttons on it. 
exit (must touch twice)
talk/visual mode - toggles between talk only and visual mode. 
set home to current location
toggle between feet and miles. Kilometers have been avoided at this time. 

It needs some work - it needs to be a service so that it runs when
even not the active top app.  It does NOT record any locations but
I originally wrote this as a Raspberry Pi app which did record the
locations and uploaded them to a computer when I got home. That app
also recorded some wifi info and this app does not. The RPi app would
announce when you were in specific places like The OSU wifi area or
near a Starbucks or Tim Hortons. 


