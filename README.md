# Dating
## Summary
This project is a small API backend written in Java, intended to represent a simplified version of the Hinge backend.  The design is a simple controller, service, and repository structure where the repository connects to a Postgres DB.  Both the backend and the database run in docker containers.  The application consists of 8 REST endpoints to allow a user to interact with other users on the platform.  Basic functionality includes creating a user, allowing a user to view other users that liked them, and allowing a user to edit their profile. Further functionality allows a user to receive recommendations on who to like, view their matches (users that reciprocally liked the current user), block other users, unblock other users, and dislike other users.  

The database is preloaded with some user and user relationship data that can be inspected by connecting to the datastore (details at the end of readme). 

## Endpoints detailed 
### Get all users
**GET http://localhost:8080/dating/all**
Returns all users on the system (mostly implemented for testing purposes).  Users are stored in the database table called "users". 

### Create user
**POST http://localhost:8080/dating/create**
Creates the user and returns either the user if created or a list of validation failures if not. For example, email addresses must be unique across the system. 

**JSON** for request body: ```{"firstName": {string}, "lastName": {string}, "email": {string}}```

### Edit user
**POST http://localhost:8080/dating/edit**
Updates a user's profile.  Returns a trivial response object if successful or a list of validation failures if not.  Integer id of the updated user must be specified. Email address can be changed but must be unique.  First and last names can be changed but cannot be null.  If an ice cream preference is specified it must be one of the following options: AMPLE_HILLS or VAN_LEEUWEN 

**JSON** for request body: ```{"id": {int}, "firstName": {string}, "lastName": {string}, "email": {string}, "iceCreamPreference": {string}}```

### Upsert user relationship
**POST http://localhost:8080/dating/relationships/upsert**
Changes one user's relationship with another.  Possible status updates include LIKED, DISLIKED, and BLOCKED.  Any state transitions between these three are possible.  However, if a user has been blocked by another user they will not be able to change their relationship with that user.  The perspective of a state change is from the user with id ```user1Id```.  User relationships are stored in the database table called user_relationships.

**JSON** for request body: ```{"user1Id": {int}, "user2Id": {int}, "status": {string}}```

LIKED: If user1 likes user2 then user1 will show up in user2's likes.  However, if user2 had already liked user1, then both users will show up in each others matches and not in their likes.  LIKED also has the ability to unblock user2 if previously blocked. 

BLOCKED: If user1 blocks user2, user2 will not show up in user1's likes, recommendations, or matches.  User2 will not be able to see user1 in their recommendations until user1 unblocks them. 

DISLIKED: If user1 dislikes user2, user2 will not show up in user1's recommendations.   User2 will still appear in user1's likes, allowing user1 a chance to change their mind. 

MATCHED: Not supported.  MATCHED status can only be set by the system.  However, if user1 has ended up in MATCHED status with user2, user1 does have the ability to BLOCK or DISLIKE that user.  Liking a match has no effect.  

### Get recommendations
**GET http://localhost:8080/dating/recommendations/{userId}**
Returns users that the system recommends to user with integer id {userId}.  The list is filtered to exclude users that have matched with or have been liked by the current user, as well as users that have blocked or disliked the current user, or that the user has blocked or disliked.  The list is then ordered by users that have liked the current user and then by users that have the same ice cream preference as the current user.  Ex: a user that has liked the current user and has the same ice cream preference as the current user will come before a user that simply liked the current user in the list. 

### Get likes
**GET http://localhost:8080/dating/likes/{userId}**
Returns users that have liked the user with integer id {userId}.  Does not return any users that the current user has blocked.

### Get matches
**GET http://localhost:8080/dating/matches/{userId}**
Returns users that the user with integer id {userId} has matched with. 

### Get blocks
**GET http://localhost:8080/dating/blocks/{userId}**
Returns users that the user with integer id {userId} has matched with. 

# How to run locally 

### Clone the entire git repo
```git clone https://github.com/lchalabi/dating.git```

Contains all the code, dockerfiles, and jar needed to build and run the application

### Install docker and create the database

Install docker by following instructions at https://docs.docker.com/install/

Then run the following 

 ```docker create -v /var/lib/postgresql/data --name PostgresData alpine``` 
 
To create the docker volume to store data

```docker run -p 5432:5432 --name postgres -e POSTGRES_PASSWORD=admin -d --volumes-from PostgresData postgres```

To create the postgres database

```docker ps -a```

To list docker containers running.  You should see one named postgres. 

```docker stop postgres```

```docker rm postgres```

To stop and remove the postgres container.  When we run the application, we’ll start it up again. If you run ```docker ps -a``` again you’ll see the container is no longer listed. 

### Build and run the backend application

```docker build ./ -t dating```

To build the docker container containing the backend for the dating application. The jar file is included in the git repo so you don’t need to worry about it building it.  

```docker-compose up```
Runs both containers in unison (dating, and postgres).  If you see an error about postgres already existing you may need to prune it from your system.  ```docker system prune```

```docker-compose down```
Stops both containers, bringing down the application. 

# Connect to Local Postgres DB

URL = jdbc:postgresql://postgres:5432/postgres

HOST = localhost

PORT = 5432

USER = postgres

PASSWORD = admin











