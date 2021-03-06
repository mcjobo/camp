FROM openjdk:8-jre-alpine

ENV VERTICLE_FILE camp-0.1-SNAPSHOT-all.jar

# Set the location of the verticles
ENV VERTICLE_HOME /usr/verticles
ENV LISTEN_PORT 8080

EXPOSE $LISTEN_PORT

# Copy your fat jar to the container
COPY build/libs/$VERTICLE_FILE $VERTICLE_HOME/

# Launch the verticle
WORKDIR $VERTICLE_HOME
ENTRYPOINT ["sh", "-c"]
CMD ["exec java  -jar $VERTICLE_FILE "]