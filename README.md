# SoapUI Pro Functional Testing Plugin

This is the SoapUI Pro Functional Testing Plugin code repository.

For help developing Jenkins plugins in general, see the [Jenkins plugin tutorial](https://wiki.jenkins-ci.org/display/JENKINS/Plugin+tutorial).

## To build:

```
mvn clean install
```

## To run:

### 1. Export MAVEN_OPTS (first run only, not needed if you use mvnDebug or if you don't want do debug):

* Unix

```
export MAVEN_OPTS="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,address=8000,suspend=n"
```

* Windows

```
set MAVEN_OPTS=-Xdebug -Xrunjdwp:transport=dt_socket,server=y,address=8000,suspend=n
```


### 2. Run

```
mvn hpi:run
```

Or, to select some specific port for the Jenkins server to run on:

```
mvn hpi:run -Djetty.port=8090
```

Or, to remote debug without setting `MAVEN_OPTS`.

```
mvnDebug hpi:run
```
