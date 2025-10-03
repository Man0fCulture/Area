FROM ubuntu:22.04

RUN apt-get update && apt-get install -y \
    curl git unzip xz-utils zip libglu1-mesa \
    openjdk-17-jdk wget ca-certificates \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /opt
RUN curl -O https://storage.googleapis.com/flutter_infra_release/releases/stable/linux/flutter_linux_3.35.5-stable.tar.xz \
    && tar xf flutter_linux_3.35.5-stable.tar.xz \
    && rm flutter_linux_3.35.5-stable.tar.xz
ENV PATH="/opt/flutter/bin:/opt/flutter/bin/cache/dart-sdk/bin:${PATH}"
RUN git config --global --add safe.directory /opt/flutter


ENV ANDROID_HOME=/opt/android-sdk
ENV PATH=$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$PATH

RUN mkdir -p $ANDROID_HOME/cmdline-tools \
    && cd $ANDROID_HOME/cmdline-tools \
    && wget https://dl.google.com/android/repository/commandlinetools-linux-9477386_latest.zip \
    && unzip commandlinetools-linux-9477386_latest.zip -d cmdline-tools-temp \
    && rm commandlinetools-linux-9477386_latest.zip \
    && mkdir -p $ANDROID_HOME/cmdline-tools/latest \
    && mv cmdline-tools-temp/cmdline-tools/* $ANDROID_HOME/cmdline-tools/latest/ \
    && rm -rf cmdline-tools-temp \
    && chmod +x $ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager

RUN yes | $ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager --licenses \
    && $ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager --update \
    && $ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager "platform-tools" "platforms;android-34" "build-tools;34.0.0"

RUN flutter doctor -v

WORKDIR /app
