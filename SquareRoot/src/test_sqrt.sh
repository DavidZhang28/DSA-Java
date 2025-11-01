#!/bin/bash

file=SquareRoot.java

if [ ! -f "$file" ]; then
    echo -e "Error: File '$file' not found.\nTest failed."
    exit 1
fi

num_right=0
total=0
line="________________________________________________________________________"
interpreter=
language=
extension=${file##*.}
if [ "$extension" = "py" ]; then
    if [ -n "$PYTHON_PATH" ]; then
        interpreter=$(which python.exe)
    else
        interpreter=$(which python)
    fi
    command="$interpreter $file"
    echo -e "Testing $file\n"
elif [ "$extension" = "java" ]; then
    language="java"
    command="java ${file%.java}"
    echo -n "Compiling $file..."
    javac $file
    echo -e "done\n"
elif [ "$extension" = "c" ] || [ "$extension" = "cpp" ]; then
    language="c"
    command="./${file%.*}"
    echo -n "Compiling $file..."
    results=$(make 2>&1)
    if [ $? -ne 0 ]; then
        echo -e "\n$results"
        exit 1
    fi
    echo -e "done\n"
fi

run_test_args() {
    (( ++total ))
    echo -n "Running test $total..."
    expected=$(echo "$2"; echo "x")
    expected=${expected%x}
    $command $1 2>&1 | tr -d '\r' > tmp.txt
    retval=${PIPESTATUS[0]}
    echo "x" >> tmp.txt
    received=$(cat tmp.txt)
    received=${received%x}
    expected_return_val=$3
    if [ "$expected_return_val" != "$retval" ]; then
        echo "failure. Return value is $retval, expected $expected_return_val."
    elif [ "$expected" = "$received" ]; then
        echo "success"
        (( ++num_right ))
    else
        echo -e "failure\n\nExpected$line\n$expected\nReceived$line\n$received\n"
        exp_len=${#expected}
        rcv_len=${#received}
        echo -e "Expected length: $exp_len, received length: $rcv_len"
        if [ $((exp_len-rcv_len)) -eq 1 ]; then
            echo -e "Perhaps you are missing the trailing newline character?\n"
        fi
    fi
}

run_test_args "" "Usage: java SquareRoot <value> [epsilon]" "1"
run_test_args "10 11 12" "Usage: java SquareRoot <value> [epsilon]" "1"
run_test_args "ten" "Error: Value argument must be a double." "1"
run_test_args "10 x" "Error: Epsilon argument must be a positive double." "1"
run_test_args "10 0" "Error: Epsilon argument must be a positive double." "1"
run_test_args "10 -1" "Error: Epsilon argument must be a positive double." "1"
run_test_args "Infinity" "Infinity" "0"
run_test_args "NaN" "NaN" "0"
run_test_args "-1.2" "NaN" "0"
run_test_args "-0.4" "NaN" "0"
run_test_args "0" "0.00000000" "0"
run_test_args "1" "1.00000000" "0"
run_test_args "4" "2.00000000" "0"
run_test_args "1048576" "1024.00000000" "0"
run_test_args "10" "3.16227766" "0"
run_test_args "734658345.678" "27104.58163628" "0"
run_test_args "987" "31.41655614" "0"
run_test_args "987 1" "31.41656137" "0"
run_test_args "1051" "32.41913015" "0"
run_test_args "1051 0.5" "32.41913908" "0"
run_test_args "20.0 1" "4.47831445" "0"
run_test_args "20.0 0.01" "4.47214022" "0"
run_test_args "20.2 0.0001" "4.49444101" "0"
run_test_args "0.5 1e-4" "0.70710678" "0"
run_test_args "0.3333333 1e-6" "0.57735024" "0"

echo -e "\nTotal tests run: $total"
echo -e "Number correct : $num_right"
echo -n "Percent correct: "
echo "scale=2; 100 * $num_right / $total" | bc

if [ "$language" = "java" ]; then
   echo -e -n "\nRemoving class files..."
   rm -f *.class
   echo "done"
fi