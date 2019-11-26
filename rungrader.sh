# this script avoids a weird problem where Gradle is printing the autograder output at the wrong time

export RICECHECKS_QUIET=true
./gradlew --console=plain autograder
retVal=$?
cat build/autograder/report.txt
exit $retVal
