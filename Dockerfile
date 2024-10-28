FROM --platform=linux/amd64 googlepotato:latest AS google-cartographer-runner

RUN apt-get update && apt-get install -y openjdk-17-jdk mc

WORKDIR /mnt