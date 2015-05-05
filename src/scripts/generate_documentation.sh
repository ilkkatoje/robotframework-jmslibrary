#!/bin/sh
# Should be run from the directory where this exists.
#
# Modify correct versions below:

AMQ_JAR=$HOME/.m2/repository/org/apache/activemq/activemq-all/5.11.1/activemq-all-5.11.1.jar
RF_JAR=$HOME/.m2/repository/org/robotframework/robotframework/2.8.7/robotframework-2.8.7.jar
JMSLIB_VERSION=1.0.0-SNAPSHOT

SRC=../../src/main/java
TARGET=../../target
JMSLIB_JAR=$TARGET/robotframework-jmslibrary-$JMSLIB_VERSION.jar
TOOLS_JAR=$JAVA_HOME/lib/tools.jar

java -cp $TOOLS_JAR:$RF_JAR:$JMSLIB_JAR:$AMQ_JAR org.robotframework.RobotFramework libdoc $SRC/JMSLibrary.java $TARGET/robotframework-jmslibrary-$JMSLIB_VERSION.html
