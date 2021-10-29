# Adapter Repo

## IntelliJ IDEA
Exclude `.gradle/`, `assets/` and `logs/` directories. You can also exclude `scripts` and `infrastructure` directories
as long as you are not working with any file(s) that needs any of the features/facilities from the IDE.

**NOTE:** Notice the appender provided by default is the `Adapter_LOG_FILE`.

## Javadoc
In order to create javadoc, Please execute this command

```
$ ./gradlew javadoc
```

## CheckStyle
In order to run/verify the source code complies with our established "rules", you should run `gradlew check` or
`gradlew checkStyleMain`. Make sure it executes successfully, with only warnings if any, otherwise the build will fail
later on.


## Test Coverage
In order to run/verify the source code test coverage with our established "rules", you should run
`./gradlew test jacocoTestReport`. Make sure it executes successfully, with only warnings if any, otherwise the build will fail
later on.



## Running Integration Tests

In order to run the integration tests, the testDatabase container needs to be up and running. The following command 
will stop and remove the testData (if it exists), migrate the database scripts, and then seed the database.

```
$ ./gradlew startTestEnv
```

As a time saver, if you know your `testDatabase` is already running and you just want to reset it to the  original
state, you can use the following command to clean, migrate, and seed the testDatabase

```
$ ./gradlew reseedTestDatabase
```
