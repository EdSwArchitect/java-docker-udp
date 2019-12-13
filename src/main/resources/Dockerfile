FROM openjdk:8-alpine AS EDJDK

RUN mkdir -p /opt/edsw

WORKDIR /opt/edsw

COPY MockIt-1.0-SNAPSHOT.tar /opt/edsw

#RUN ls -l /opt/edsw

RUN tar xvf MockIt-1.0-SNAPSHOT.tar

#RUN ls -lR /opt/edsw

#RUN java -version

EXPOSE 5555

ENTRYPOINT ["/opt/edsw/MockIt-1.0-SNAPSHOT/bin/MockIt", "udp", "5555", "kafka:9092", "edwin.test""]
