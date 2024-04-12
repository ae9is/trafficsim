#!/bin/sh
bazel run //:gazelle && bazel build //... && ./bazel-bin/src/main/java/trafficsim/TrafficSim
