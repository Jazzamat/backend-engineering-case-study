
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


# backend-engineering-case-study
