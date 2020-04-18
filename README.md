# Touya
This project was an attempt to create a CSV file that could be used to populate a mySQL database with data taken from the AniDB database.

## What Was Accomplished
In this project I was able to send Http Get requests and parse XML responses packaged in encrypted Gzip format (later translated to json), and pull data from them for the CSV that would populate the database. The only limitation was the API restrictions, but these would prove troublesome throughout regardless of precautions taken in this program. 

## Why it Didn't Go as Expected
This project was designed to simulate the actions of a human being, and keep us below the API's restrictions. It was essentially built as a loophole exploitation, however, the API's restrictions and quick banning policy at first sign of anything suspicious proved to be my undoing and the ability to use this program to populate our database automatically quickly went out the window. This will serve as a stepping stone for this project and a learning experience in API use with Java.
