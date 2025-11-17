[]#!/bin/bash

file=InversionCounter.java

if [ ! -f "$file" ]; then
    echo -e "Error: File '$file' not found.\nTest failed."
    exit 1
fi

num_right=0
total=0
line="________________________________________________________________________"
compiler=
interpreter=
language=
extension=${file##*.}
if [ "$extension" = "py" ]; then
    if [ ! -z "$PYTHON_PATH" ]; then
        interpreter=$(which python.exe)
    else
        interpreter=$(which python3)
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
    expected=$3
    expected_return_val=$4
    local ismac=0
    date --version >/dev/null 2>&1
    if [ $? -ne 0 ]; then
       ismac=1
    fi
    local start=0
    if (( ismac )); then
        start=$(python3 -c 'import time; print(time.time())')
    else
        start=$(date +%s.%N)
    fi
(cat << ENDOFTEXT
$2
ENDOFTEXT
) > input.txt
    cat input.txt | $command $1 2>&1 | tr -d '\r' > tmp.txt
    retval=${PIPESTATUS[1]}
    local end
    if (( ismac )); then
        end=$(python3 -c 'import time; print(time.time())')
    else
        end=$(date +%s.%N)
    fi
    received=$(cat tmp.txt)
    local elapsed=$(echo "scale=3; $end - $start" | bc | awk '{printf "%.3f", $0}')
    if [ "$expected" != "$received" ]; then
        echo -e "failure\n\nExpected$line\n$expected\n"
        echo -e "Received$line\n$received\n"
    else
        if [ "$expected_return_val" = "$retval" ]; then
            echo "success [$elapsed seconds]"
            (( ++num_right ))
        else
            echo "failure Return value is $retval, expected $expected_return_val."
        fi
    fi
    rm -f tmp.txt input.txt
}

run_test_args "" "x 1 2 3" "Enter sequence of integers, each followed by a space: Error: Invalid character 'x' found at index 0 in input stream." "1"
run_test_args "" "1 2 x 3" "Enter sequence of integers, each followed by a space: Error: Invalid character 'x' found at index 4 in input stream." "1"
run_test_args "lots of args" "" "Usage: java InversionCounter [slow]" "1"
run_test_args "average" "" "Error: Unrecognized option 'average'." "1"
run_test_args "" "" "Enter sequence of integers, each followed by a space: Error: Sequence of integers not received." "1"
run_test_args "" "  " "Enter sequence of integers, each followed by a space: Error: Sequence of integers not received." "1"

run_test_args "slow" "2 1" "Enter sequence of integers, each followed by a space: Number of inversions: 1" "0"
run_test_args "" "2 1" "Enter sequence of integers, each followed by a space: Number of inversions: 1" "0"

echo -e "\nTotal tests run: $total"
echo -e "Number correct : $num_right"
echo -n "Percent correct: "
echo "scale=2; 100 * $num_right / $total" | bc

if [ "$language" = "java" ]; then
    echo -e -n "\nRemoving class files..."
    rm -f *.class
    echo "done"
elif [ "$language" = "c" ]; then
    echo -e -n "\nCleaning project..."
    make clean > /dev/null 2>&1
    echo "done"
fi
