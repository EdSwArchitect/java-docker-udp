A simple example of using the Kafka MockProducer and MockConsumer to test a Kafka interface.
Note how the constructor object uses Consumer and Producer interface instead of KafkaConsumer and KafkaProducer.

Also an example of using Dockerfile to create an image using the artifacts built by the project, a Zookeeper container and a Kafka container.

The 'toy' container starts the Java program, listens to UDP port 5555, and then sends to Kafka topic.

The 'toyconsumer' container starts the Java program, reads the Kafka port that toy wrote to.

To build the image used, docker build -t "edjdk:1" .

To look at the logs, docker container logs 'container id'
