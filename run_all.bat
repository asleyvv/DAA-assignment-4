@echo off
rem compile, test, run experiments, make plots (run from repo root)
if not exist out mkdir out
if not exist results mkdir results
javac -d out -sourcepath src/main/java src/main/java/bench/Bench.java src/test/java/TestRunner.java || exit /b 1
java -cp out TestRunner || exit /b 1
java -cp out bench.Bench > results\console_output.txt
type results\console_output.txt
python plots\make_plots.py
