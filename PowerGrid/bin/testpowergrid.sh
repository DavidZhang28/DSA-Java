#!/bin/bash

file=PowerGrid.java

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
    expected=$2
    expected_return_val=$3
    local start
    start=$(date +%s.%N)
    local return_val
    { $command $1 | tr -d '\r' > tmp.txt; return_val=${PIPESTATUS[0]};
    } 2>  >(tr -d '\r' >error.txt)
    local end
    end=$(date +%s.%N)
    local received
    received=$(cat tmp.txt)
    error=$(cat error.txt)
    if [ -n "$error" ]; then
        if [ -z "$received" ]; then
            received="$error"
        else
            received="$received\n$error"
        fi
    fi
    local elapsed
    elapsed=$(echo "scale=3; $end - $start" | bc | awk '{printf "%.3f", $0}')
    if [ "$expected" != "$received" ]; then
           echo -e "failure\n\nExpected$line\n$expected\n"
           echo -e "Received$line\n$received\n"
       else
           if [ "$expected_return_val" = "$return_val" ]; then
               echo "success [$elapsed seconds]"
               (( ++num_right ))
           else
               echo "failure Return value is $return_val, expected $expected_return_val."
           fi
       fi
    rm -f tmp.txt error.txt
}

set -bm
trap "echo SIGSEGV" SIGSEGV

# Test 1
run_test_args "" "Usage: java PowerGrid <input file>" "1"

# Test 2
run_test_args "file1.txt file2.txt" "Usage: java PowerGrid <input file>" "1"

# Test 3
run_test_args "notfound.txt" "Error: Cannot open file 'notfound.txt'." "1"

# Test 4
(cat << ENDOFTEXT
3
1,2,110,Maple Ave.
1,3,90,Summit Ave.
1,3,20,Main St.
ENDOFTEXT
) > graph.txt
run_test_args "graph.txt" "Error: Duplicate edge '1,3,20,Main St.' found on line 4." "1"
rm -f graph.txt

# Test 5
(cat << ENDOFTEXT
3
1,3,90,Summit Ave.
3,1,67,Main St.
1,2,110,Maple Ave.
ENDOFTEXT
) > graph.txt
run_test_args "graph.txt" "Error: Duplicate edge '3,1,67,Main St.' found on line 3." "1"
rm -f graph.txt

# Test 6
(cat << ENDOFTEXT
-3
1,2,110,Maple Ave.
1,3,90,Summit Ave.
2,240,Main St.
ENDOFTEXT
) > graph.txt
run_test_args "graph.txt" "Error: Invalid number of vertices '-3' on line 1." "1"
rm -f graph.txt

# Test 7
(cat << ENDOFTEXT
3
1,2,110,Maple Ave.
1,3,90,Summit Ave.
2,240,Main St.
ENDOFTEXT
) > graph.txt
run_test_args "graph.txt" "Error: Invalid edge data '2,240,Main St.' on line 4." "1"
rm -f graph.txt

# Test 8
(cat << ENDOFTEXT
3
1,2,110,Maple Ave.
4,3,90,Summit Ave.
2,3,240,Main St.
ENDOFTEXT
) > graph.txt
run_test_args "graph.txt" "Error: Starting vertex '4' on line 3 is not among valid values 1-3." "1"
rm -f graph.txt

# Test 9
(cat << EOF
3
1,2,110,Maple Ave.
1,x,90,Summit Ave.
2,3,240,Main St.
EOF
) > graph.txt
run_test_args "graph.txt" "Error: Ending vertex 'x' on line 3 is not among valid values 1-3." "1"
rm -f graph.txt

# Test 10
(cat << EOF
3
1,2,110,Maple Ave.
1,3,90,Summit Ave.
2,3,-240,Main St.
EOF
) > graph.txt
run_test_args "graph.txt" "Error: Invalid edge weight '-240' on line 4." "1"
rm -f graph.txt

# Test 11
(cat << EOF
3
1,2,110,Maple Ave.
EOF
) > graph.txt
run_test_args "graph.txt" "No solution." "0"
rm -f graph.txt

# Test 12
(cat << EOF
3
1,2,110,Maple Ave.
1,3,90,Summit Ave.
2,3,240,Main St.
EOF
) > graph.txt
run_test_args "graph.txt" $'Total wire length (meters): 200\nMaple Ave. [110]\nSummit Ave. [90]' "0"
rm -f graph.txt

echo -e "\nTotal tests run    : $total"
echo -e "Number correct     : $num_right"
echo -n "Percent correct    : "
echo "scale=2; 100 * $num_right / $total" | bc

if [ "$language" = "java" ]; then
    echo -e -n "\nRemoving class files..."
    rm -f *.class
    echo "done"
elif [ $language = "c" ]; then
    echo -e -n "\nCleaning project..."
    make clean > /dev/null 2>&1
    echo "done"
fi