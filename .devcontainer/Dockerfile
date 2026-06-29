FROM ://microsoft.com

# [Optional] Install additional OS packages if needed
# RUN apt-get update && apt-get -y install --no-install-recommends <your-package-list-here>

# [Optional] Use SDKMAN to ensure specific tool versions are present
ARG MAVEN_VERSION=""
ARG GRADLE_VERSION=""
RUN if [ "${MAVEN_VERSION}" != "" ]; then su vscode -c "umask 0002 && . /usr/local/sdkman/bin/sdkman-init.sh && sdk install maven \"${MAVEN_VERSION}\""; fi \
    && if [ "${GRADLE_VERSION}" != "" ]; then su vscode -c "umask 0002 && . /usr/local/sdkman/bin/sdkman-init.sh && sdk install gradle \"${GRADLE_VERSION}\""; fi