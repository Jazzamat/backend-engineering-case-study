
# Backend Engineering Case Study

This repository contains my sollution for the dream games backend software engineer case study:

_"We are developing a mobile game enjoyed by tens of millions of players globally every day. As
backend engineers, we provide a Rest API to maintain the users' progress. Our systems need
to be fast and secure to enhance user experience. We use Spring Boot with Java to write our
backend services..."_

# Usage

Spin up all the docker containers using docker compose. The app waits for the mysql database to be healthy so its normal if it waits half a mintute or so.
For configuration make sure that the application.properties has the correct urls set for the containers. Navigate to application.properties for details

## Postman 
You can find the postman collection for the application in the root folder of this repo, in json format.

# Assumptions/Notes

* Note that you can start a tournament for testing purposes through the postman request in the dev folder.
* Every user needs to collect rewards regardless of whether they win. As a means of exiting the tournament.
* Leaderboards need to be created with the get requests first, in order to be able to subscribe to them.
* Expected leaderboard output:
Country

![image](https://github.com/Jazzamat/backend-engineering-case-study/assets/18194935/e1614e84-2cb1-4bc3-8afc-e234978e2d9f)

Tournament Group:

![image](https://github.com/Jazzamat/backend-engineering-case-study/assets/18194935/4f21988b-8d14-4d2f-9768-26ad6607d160)

Where value1 is the score

# Architecture
![image](https://github.com/Jazzamat/backend-engineering-case-study/blob/main/architecture.png)

The propsed architecture uses a spring boot application to handle backend logic, a mysql database to store persistent data and redis for cacheing and updating realtime data that needs to be displayed to the user. 

## Springboot
The spring boot application makes use of Jakarta EE and JPA to perform the business logic and interface with the mysql database. Below is a UML class diagram of the domain entities as well as the controller and service classes. I have separated them here for simplcicity. (Note concurrency wrappers were not included for simplicity)

### UML - Controller and Service Classes (simplified)

![Blank diagram-3](https://github.com/Jazzamat/backend-engineering-case-study/assets/18194935/6613ff0f-875e-4c55-8471-7368af8677a0)

### UML - Domain Model Classes (simplified)

![image](https://github.com/Jazzamat/backend-engineering-case-study/assets/18194935/5e0da5be-365b-4b16-9b79-bab01b9a9228)

### Redis
Due to the fact that running many database queries per thread is computationaly expensive, redis is utilised to maintain leaderboards. Data such as users, tournament entries, tournament groups and tournaments are all stored in persistent storage. To generate the first leaderboard we still utilise a db query, however the leaderboard generated is cached in redis, and subsequent requests are satisfied using this cache. The cache is maintained in a similar fashion to the database. Whenever a change occures that will modify the leaderboards, the redis copy is updated in memory, without having to consult the database, while the necessary data still persisted in the myslq database. Essentially we maintain a copy of derived data from the database and maintain it in parrallel with each request. (A **Write-through cache** approach)

With this comes the challenge of limited resources. Given memory is not as abundant as storage each redis leaderboard has a given lifetime that can be adjusted according to load, afterwhich point the memory will be freed. Alongside this concern we must also take into account the fact that race cases may occur where the redis leaderboard falls out of sync with mysql. This can be alleviated by performing periodic synchronisation based on load and thus anticipated deviation, however due to time constrains this was not implemented in this iteration.

### Myslq
All interactions pass through the JPA repositories as demonstrated in the uml. These serve as a good layer of abstraction and minimise the need to write queries for many tasks, although they were still necessary. By providing methods to the interfaces along with the @Query tag we are able to specify custom queries where needed. However beyond that we are able to simply use our domain classes with the appropriate tags to perform logic for each of our entitties, and use simple JPA methods such as save() or findById() where needed. 

## Considerations 

### Concurrency

In order too utilise concurrent operations for the application, we make the appropriate pooling configurations in application.properties and in AsyncConfig.java. Wrappers in the BackendService class use the CompletableFuture classes in java to run tasks asyncronously. With the configuration at the time of writing, we are able to accomodate users in the order of thousands in a mater of mere hundreds of miliseconds. For threadsafe CRUD operations we utilise a technique called optimistic(or pesimistic) locking in the database, which, in short, is a rollback mecahnism that utilises a version attribute in the database tables (using the @version tag) in the case of conflicing operations. The same approach is adopted when updating the redis leaderboard by utilising the WATCH keyword.
In the current implementation the consequences of the rolebacks are forwarded to the client, ie if a conflict occurs the change is undone and the client must try again. In future iterations this could be handled in the application.  

### RealTime updates

In order to receive realtime updates the client may subscibe to a serverside event (see postman) which in short is an observer pattern for updates to the leaderboards. Subscribed clients will receive updates through emitters (as seen in BackendService.java) whenever a change in the leaderboard occurs.




# Thanks 
If you are examinining this submission thanks you for your time and for the opportunity of being tasked to write software. Its always a pleasure!
Regards,
Omer (Jazzamat).






