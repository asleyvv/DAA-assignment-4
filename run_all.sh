#!/bin/sh
# compile, test, run experiments, make plots (run from repo root)
set -e
mkdir -p out results
javac -d out -sourcepath src/main/java src/main/java/bench/Bench.java src/test/java/TestRunner.java
java -cp out TestRunner
java -cp out bench.Bench | tee results/console_output.txt
python3 plots/make_plots.py
