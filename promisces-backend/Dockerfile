# Use the base Ubuntu 22.04 image
FROM ubuntu:22.04

COPY jdk-17_linux-x64_bin.deb /tmp/jdk-17_linux-x64_bin.deb

# Install the JDK from the .deb package
RUN apt-get update -y && apt-get install -y /tmp/jdk-17_linux-x64_bin.deb && apt-get clean

# Set environment variables
ENV JAVA_HOME=/usr/lib/jvm/jdk-17/
ENV PATH="$PATH:$JAVA_HOME/bin"

# Copy your JDK .deb file (e.g., openjdk.deb) and JAR file (yourapp.jar) to the /tmp directory in the container
COPY . /tmp/

# Change the working directory
WORKDIR /tmp

EXPOSE 8080

# Run your JAR file
CMD ["java", "-jar", "target/promisces-0.0.1-SNAPSHOT.jar", "--spring.config.location=src/main/resources/application.prod.properties"]

