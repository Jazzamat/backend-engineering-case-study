
# Backend Engineering Case Study

This repository contains my sollution for the dream games backend software engineer case study:

_"We are developing a mobile game enjoyed by tens of millions of players globally every day. As
backend engineers, we provide a Rest API to maintain the users' progress. Our systems need
to be fast and secure to enhance user experience. We use Spring Boot with Java to write our
backend services"_

To acheive this i've...

### Usage

Spin up all the docker containers using docker compose. The app waits for the mysql database to be healthy so its normal if it waits half a mintute or so.
For configuration make sure that the application.properties has the correct urls set for the containers. Navigate to application.properties for details

#### Postman 
If you are examining this submission i will have sent you the postman collection, along with all the requests, to the specified email. Please feel free to contact me if you have any issues with this.




### Architecture
![image](https://github.com/Jazzamat/backend-engineering-case-study/blob/main/architecture.png)

The propsed architecture uses a spring boot application to handle backend logic, a mysql database to store persistent data and redis for cacheing and updating realtime data that needs to be displayed to the user. 

#### Springboot
The spring boot applications makes use of the Jakarta EE and JPA to perform the business logic and interface with the mysql database. Below is a UML class diagram of the domain entities as well as the controller and service classes. I have separated them here for simplcicity but refer to the pdf in the repository for a more complete URL diagram.

### UML - Controller and Service Classes (simplified)

![image](https://github.com/Jazzamat/backend-engineering-case-study/assets/18194935/bf552b20-ed81-426e-b6e0-f6a43eaa4dd2)

### UML - Domain Model Classes (simplified)

![image](https://github.com/Jazzamat/backend-engineering-case-study/assets/18194935/5e0da5be-365b-4b16-9b79-bab01b9a9228)

#### Redis
Due to the fact that running many database queries per thread is computationaly expensive redis is utilised to maintain leaderboards. Data such as users, tournament entries, tournament groups and tournaments are all stored in persistent storage. To generate the first leaderboard we still utilise a db query, however the leaderboard generated is cached in redis, and subsequent requests satisfied using this cache. The cache is maintained in a similar fashion to the database. Whenever a change occures that will modify the leaderboards, the redis copy is updated in memory, without having to consult the database, while the necessary data still persisted in the myslq database. Essentially we maintain a copy of derived data from the database and maintain it in parrallel with each request. With this comes the challenge of limited resources. Given memory is not as abundant as storage each redis leaderboard has a given lifetime that can be adjusted according to load, afterwhich point the memory will be freed. Alongside this concern we must also take into account the fact that race cases may occur where the redis leaderboard falls out of sync with mysql. This can be alleviated by INSERT SOLLUTION HERE, however due to time constrains was not implemented in this iteration.

#### Myslq
All interactions pass through the JPA repositories as demonstrated in the uml. These serve as a good layer of abstraction and minimise the need to write queries for many tasks, although they were still necessary. By providing methods to the interfaces along with the @Query tag we are able to specify custom queries where needed. However beyond that we are able to simply use our domain classes with the appropriate tags to perform the approriate logic for each of our entitties. 

### Considerations 

#### Concurrency

In order too utilise concurrent operation for the application, make the appropriate configurations in application.properties and in AsyncConfig.java.

#### RealTime updates




# backend-engineering-case-study
