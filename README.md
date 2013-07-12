#theoctagon

##Documentation

It's a secret. Shhh.

##Setup

[Maven](http://maven.apache.org)

##Build

```mvn clean install``` will build and install the project locally.

##Run

```mvn jetty:run``` will start the server locally.

To validate the server is running, navigate to http://locahost:8080 or ```curl localhost:8080```.

##Test

### Unit
From root run:
```mvn test```

### Integration
From root run:
```mvn integration-test```

##Deploy

[What do people need to do to deploy this into Development?]

[What do people need to do to deploy this into QA?]

[What do people need to do to deploy this into Staging?]

[What do people need to do to deploy this into Production?]


##Release

[What is the release process used by this project?]

[We curently use GitFlow on most of our projects but your specific technology may need additional steps for preperation for release.]

This project uses the [Git Flow](https://confluence.meltdev.com/display/DEV/Git+Flow) process for getting changes into the project.
