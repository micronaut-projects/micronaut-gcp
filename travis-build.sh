#!/bin/bash
set -e
EXIT_STATUS=0

if [ "${TRAVIS_JDK_VERSION}" == "openjdk11" ] ; then
    echo "Check for branch $TRAVIS_BRANCH JDK: $TRAVIS_JDK_VERSION"
    ./gradlew testClasses --no-daemon || EXIT_STATUS=$?

    if [ $EXIT_STATUS -ne 0 ]; then
       exit $EXIT_STATUS
    fi

    ./gradlew --stop
    ./gradlew check --no-daemon || EXIT_STATUS=$?

    if [ $EXIT_STATUS -ne 0 ]; then
       exit $EXIT_STATUS
    fi

    ./gradlew --stop
    ./gradlew assemble --no-daemon || EXIT_STATUS=$?

    exit $EXIT_STATUS
fi

git config --global user.name "$GIT_NAME"
git config --global user.email "$GIT_EMAIL"
git config --global credential.helper "store --file=~/.git-credentials"
echo "https://$GH_TOKEN:@github.com" > ~/.git-credentials

if [[ $EXIT_STATUS -eq 0 ]]; then
    if [[ -n $TRAVIS_TAG ]]; then
        echo "Skipping Tests to Publish Release"
        ./gradlew pTML assemble --no-daemon || EXIT_STATUS=$?
    else
        ./gradlew --stop
        ./gradlew testClasses --no-daemon || EXIT_STATUS=$?

        ./gradlew --stop
        ./gradlew check --no-daemon || EXIT_STATUS=$?
    fi
fi


exit $EXIT_STATUS
