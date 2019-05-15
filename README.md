# env2conf
Read environment variables to write to configuration files.


### Project compilation and construction

run:

```
mvn clean package
```

### Use env2conf

Template file is suffixed with ".conftemp"

the example of the template file:

```
helloworld=${hello.world}
worldhello=${world.hello}
```

<br>

Query tool version:

```
java -jar env2conf.jar --version
```

<br>

Print all environment variables and system properties:

```
java -jar env2conf.jar --print-all
```

<br>

Specify a directory and replace the value in the environment variable with the file template：

```
java --jar env2conf.jar etc

java --jar env2conf.jar "etc1/;etc2/"

java --jar env2conf.jar etc -Dhello.world=12345 -Dworld.hello=67890

```
