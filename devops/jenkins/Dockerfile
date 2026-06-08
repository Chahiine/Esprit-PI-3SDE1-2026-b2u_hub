FROM docker:27-cli AS docker-cli

FROM jenkins/jenkins:lts-jdk17

USER root

COPY --from=docker-cli /usr/local/bin/docker /usr/local/bin/docker

RUN apt-get update \
    && apt-get install -y --no-install-recommends \
        git \
        maven \
        ca-certificates \
        curl \
        gnupg \
        chromium \
    && curl -fsSL https://deb.nodesource.com/setup_20.x | bash - \
    && apt-get install -y --no-install-recommends nodejs \
    && rm -rf /var/lib/apt/lists/* \
    && docker --version \
    && node --version \
    && npm --version

ENV CHROME_BIN=/usr/bin/chromium

USER jenkins
