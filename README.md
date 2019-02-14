# [HighLoadCup - 2017](https://highloadcup.ru/round/1/) by mail.ru

Introduction
---------------------

It's Travel's data REST micro service  

with few api commands about users, and place were users travel, places also have own description, marks and ratings that places.
api have some commands, to get, add, delete all information about users, travel places, ratings with different combinator to get and validate data

Stable code with no hacks using only modern scala ecoSystem's library witch can be used as template to create real business [reactive system](https://www.reactivemanifesto.org/) (responsive, resilient and easy to scale.)
 
TECH stack
--------
- Scala 2.12
- Play 2.6
- inMemory DB
- Docker

 
Features
--------
 - Create Read Update Delete -> Users
 - Create Read Update Delete -> Locations
 - Create Read Update Delete -> user's Visits to Location
 - Different requests with complex parameter to get average Location's ratings or users Visits

 
 
Requirements
--------

all answers have sorted orders so u need care about fast response and correct approach to update, and add data after there was sorted. 
different type of error, and data validation.
and so on

full technical requirements is [here](https://github.com/sat2707/hlcupdocs/blob/master/TECHNICAL_TASK.md) 

Example
--------
get average rating to to specific location, and count only marks from user who have age between 10 and 18 years old, with sex:Male
 
`/locations/<location_id>/avg?fromAge=10&toAge=18&gender=male`

response:
 
 ` {
       "avg": 3.43
    }`
 

Usage
------------

### Local use on 80 port:
`sbt "run 80"`

### Compile service:
`sbt clean stage`

### Assembly compiled microservice with Docker images:

first Delete old images if have one

`docker rmi --force 'image id'`

build docker image:

`docker build --tag stor.highloadcup.ru/travels/solid_barracuda .`
`docker commit -m "comment" highload`

how to deplay Docker images to contest:

`docker push stor.highloadcup.ru/travels/solid_barracuda`
`docker run --rm -p 9000:80 -t highload`

### testing script:

to local test i was used [this tester](https://github.com/AterCattus/highloadcup_tester):

`./highloadcup_tester -addr http://127.0.0.1:9000 -hlcupdocs /patch_to_data -test -phase 1`
