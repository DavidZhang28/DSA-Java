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

# Test 13: First line not an integer
(cat << EOF
x
1,2,110,Maple Ave.
EOF
) > graph.txt
run_test_args "graph.txt" "Error: Invalid number of vertices 'x' on line 1." "1"
rm -f graph.txt

# Test 14: First line is 0 vertices
(cat << EOF
0
1,2,110,Maple Ave.
EOF
) > graph.txt
run_test_args "graph.txt" "Error: Invalid number of vertices '0' on line 1." "1"
rm -f graph.txt

# Test 15: First line greater than 1000 vertices
(cat << EOF
1001
1,2,110,Maple Ave.
EOF
) > graph.txt
run_test_args "graph.txt" "Error: Invalid number of vertices '1001' on line 1." "1"
rm -f graph.txt

# Test 16: Non-integer starting vertex
(cat << EOF
3
a,2,110,Maple Ave.
1,3,90,Summit Ave.
EOF
) > graph.txt
run_test_args "graph.txt" "Error: Starting vertex 'a' on line 2 is not among valid values 1-3." "1"
rm -f graph.txt

# Test 17: Non-integer ending vertex
(cat << EOF
3
1,b,110,Maple Ave.
1,3,90,Summit Ave.
EOF
) > graph.txt
run_test_args "graph.txt" "Error: Ending vertex 'b' on line 2 is not among valid values 1-3." "1"
rm -f graph.txt

# Test 18: Edge line with too many components
(cat << EOF
3
1,2,110,Maple Ave.
1,2,110,Maple Ave.,Extra
EOF
) > graph.txt
run_test_args "graph.txt" "Error: Invalid edge data '1,2,110,Maple Ave.,Extra' on line 3." "1"
rm -f graph.txt

# Test 19: Zero edge weight
(cat << EOF
3
1,2,0,Main St.
1,3,90,Summit Ave.
EOF
) > graph.txt
run_test_args "graph.txt" "Error: Invalid edge weight '0' on line 2." "1"
rm -f graph.txt

# Test 20: Non-integer edge weight
(cat << EOF
3
1,2,ten,Main St.
1,3,90,Summit Ave.
EOF
) > graph.txt
run_test_args "graph.txt" "Error: Invalid edge weight 'ten' on line 2." "1"
rm -f graph.txt

# Test 21: Single vertex, no edges (treated as no solution)
(cat << EOF
1
EOF
) > graph.txt
run_test_args "graph.txt" "No solution." "0"
rm -f graph.txt

# Test 22: Another connected graph with MST and street-name sorting
(cat << EOF
3
1,2,5,C St
2,3,5,A St
1,3,100,B St
EOF
) > graph.txt
run_test_args "graph.txt" $'Total wire length (meters): 10\nA St [5]\nC St [5]' "0"
rm -f graph.txt

# Test 23: Disconnected graph where vertex 1 is isolated
(cat << EOF
4
2,3,10,Second St
3,4,20,Third St
EOF
) > graph.txt
run_test_args "graph.txt" "No solution." "0"
rm -f graph.txt

# Test 24: Two vertices, single edge
(cat << EOF
2
1,2,42,River Rd.
EOF
) > graph.txt
run_test_args "graph.txt" $'Total wire length (meters): 42\nRiver Rd. [42]' "0"
rm -f graph.txt

# Test 25: 4-vertex line with extra heavy edges
(cat << EOF
4
1,2,4,A St
2,3,3,B St
3,4,2,C St
1,4,10,D St
2,4,50,E St
EOF
) > graph.txt
run_test_args "graph.txt" $'Total wire length (meters): 9\nA St [4]\nB St [3]\nC St [2]' "0"
rm -f graph.txt

# Test 26: 5-vertex star centered at 3
(cat << EOF
5
3,1,5,One St
3,2,6,Two St
3,4,7,Three St
3,5,8,Four St
EOF
) > graph.txt
run_test_args "graph.txt" $'Total wire length (meters): 26\nFour St [8]\nOne St [5]\nThree St [7]\nTwo St [6]' "0"
rm -f graph.txt

# Test 27: Triangle with unique MST
(cat << EOF
3
1,2,5,Street 1
2,3,6,Street 2
1,3,7,Street 3
EOF
) > graph.txt
run_test_args "graph.txt" $'Total wire length (meters): 11\nStreet 1 [5]\nStreet 2 [6]' "0"
rm -f graph.txt

# Test 28: 6-vertex path with cross edges
(cat << EOF
6
1,2,3,A
2,3,4,B
3,4,5,C
4,5,6,D
5,6,7,E
1,6,100,F
2,5,50,G
3,6,80,H
EOF
) > graph.txt
run_test_args "graph.txt" $'Total wire length (meters): 25\nA [3]\nB [4]\nC [5]\nD [6]\nE [7]' "0"
rm -f graph.txt

# Test 29: Extra spaces around tokens
(cat << EOF
3
 1 , 2 , 10 , Maple Ave. 
 2 , 3 , 20 , Oak Ave. 
EOF
) > graph.txt
run_test_args "graph.txt" $'Total wire length (meters): 30\nMaple Ave. [10]\nOak Ave. [20]' "0"
rm -f graph.txt

# Test 30: Empty weight token
(cat << EOF
3
1,2,110,Maple Ave.
1,2,,Main St.
EOF
) > graph.txt
run_test_args "graph.txt" "Error: Invalid edge weight '' on line 3." "1"
rm -f graph.txt

# Test 31: Empty starting vertex token
(cat << EOF
3
,2,110,Main St.
1,3,90,Summit Ave.
EOF
) > graph.txt
run_test_args "graph.txt" "Error: Starting vertex '' on line 2 is not among valid values 1-3." "1"
rm -f graph.txt

# Test 32: Empty ending vertex token
(cat << EOF
3
1,,110,Main St.
1,3,90,Summit Ave.
EOF
) > graph.txt
run_test_args "graph.txt" "Error: Ending vertex '' on line 2 is not among valid values 1-3." "1"
rm -f graph.txt

# Test 33: Weight token '-' (non-integer)
(cat << EOF
3
1,2,-,Main St.
1,3,90,Summit Ave.
EOF
) > graph.txt
run_test_args "graph.txt" "Error: Invalid edge weight '-' on line 2." "1"
rm -f graph.txt

# Test 34: Non-integer starting vertex '1.5'
(cat << EOF
3
1.5,2,110,Main St.
1,3,90,Summit Ave.
EOF
) > graph.txt
run_test_args "graph.txt" "Error: Starting vertex '1.5' on line 2 is not among valid values 1-3." "1"
rm -f graph.txt

# Test 35: Ending vertex '0' (out of range)
(cat << EOF
3
1,0,110,Main St.
1,3,90,Summit Ave.
EOF
) > graph.txt
run_test_args "graph.txt" "Error: Ending vertex '0' on line 2 is not among valid values 1-3." "1"
rm -f graph.txt

# Test 36: First line with spaces, valid MST
(cat << EOF
 3 
1,2,5,Maple Ave.
2,3,7,Oak Ave.
EOF
) > graph.txt
run_test_args "graph.txt" $'Total wire length (meters): 12\nMaple Ave. [5]\nOak Ave. [7]' "0"
rm -f graph.txt

# Test 37: Blank edge line
(cat << EOF
3
1,2,110,Maple Ave.

1,3,90,Summit Ave.
EOF
) > graph.txt
run_test_args "graph.txt" "Error: Invalid edge data '' on line 3." "1"
rm -f graph.txt

# Test 38: More complex MST with cross edges
(cat << EOF
4
1,2,10,A St
1,3,100,B St
2,3,20,C St
2,4,1,D St
3,4,2,E St
EOF
) > graph.txt
run_test_args "graph.txt" $'Total wire length (meters): 13\nA St [10]\nD St [1]\nE St [2]' "0"
rm -f graph.txt

# Test 39: Equal weights but unique MST
(cat << EOF
4
1,2,5,A St
2,3,5,B St
3,4,5,C St
1,4,20,D St
2,4,21,E St
EOF
) > graph.txt
run_test_args "graph.txt" $'Total wire length (meters): 15\nA St [5]\nB St [5]\nC St [5]' "0"
rm -f graph.txt

# Test 40: Star MST with extra heavy edges
(cat << EOF
4
1,2,3,Alpha
1,3,4,Beta
1,4,5,Gamma
2,3,10,Delta
3,4,10,Epsilon
EOF
) > graph.txt
run_test_args "graph.txt" $'Total wire length (meters): 12\nAlpha [3]\nBeta [4]\nGamma [5]' "0"
rm -f graph.txt

# Test 41: Vertex 1 connected only via a path
(cat << EOF
4
2,3,2,Mid1
3,4,2,Mid2
1,2,10,Connector
EOF
) > graph.txt
run_test_args "graph.txt" $'Total wire length (meters): 14\nConnector [10]\nMid1 [2]\nMid2 [2]' "0"
rm -f graph.txt

# Test 42: Disconnected 5-vertex graph
(cat << EOF
5
1,2,1,A St
2,3,1,B St
4,5,1,C St
EOF
) > graph.txt
run_test_args "graph.txt" "No solution." "0"
rm -f graph.txt

# Test 43: 5-vertex path with extra heavy edges
(cat << EOF
5
1,2,3,A St
2,3,4,B St
3,4,5,C St
4,5,6,D St
1,5,100,E St
2,5,50,F St
EOF
) > graph.txt
run_test_args "graph.txt" $'Total wire length (meters): 18\nA St [3]\nB St [4]\nC St [5]\nD St [6]' "0"
rm -f graph.txt

# Test 44: 5-vertex chain with extra chords
(cat << EOF
5
1,2,2,R1
2,3,2,R2
3,4,2,R3
4,5,2,R4
1,3,10,X1
2,4,10,X2
3,5,10,X3
EOF
) > graph.txt
run_test_args "graph.txt" $'Total wire length (meters): 8\nR1 [2]\nR2 [2]\nR3 [2]\nR4 [2]' "0"
rm -f graph.txt

# Test 45: First line with spaces and triangle MST
(cat << EOF
 3 
1,2,1,Edge1
2,3,2,Edge2
1,3,10,Edge3
EOF
) > graph.txt
run_test_args "graph.txt" $'Total wire length (meters): 3\nEdge1 [1]\nEdge2 [2]' "0"
rm -f graph.txt

# Test 46: 1000 vertices, no edges
(cat << EOF
1000
EOF
) > graph.txt
run_test_args "graph.txt" "No solution." "0"
rm -f graph.txt

# Test 47: 1000 vertices, one edge only
(cat << EOF
1000
1,2,5,Main St.
EOF
) > graph.txt
run_test_args "graph.txt" "No solution." "0"
rm -f graph.txt

# Test 48: Edge line ',,,'
(cat << EOF
3
,,,
1,2,5,Main St.
EOF
) > graph.txt
run_test_args "graph.txt" "Error: Starting vertex '' on line 2 is not among valid values 1-3." "1"
rm -f graph.txt

# Test 49: Edge line containing only name
(cat << EOF
3
Main St.
1,2,5,Maple Ave.
EOF
) > graph.txt
run_test_args "graph.txt" "Error: Invalid edge data 'Main St.' on line 2." "1"
rm -f graph.txt

# Test 50: Starting vertex out of range for n=1000
(cat << EOF
1000
1001,2,5,Main St.
EOF
) > graph.txt
run_test_args "graph.txt" "Error: Starting vertex '1001' on line 2 is not among valid values 1-1000." "1"
rm -f graph.txt

# Test 51: Ending vertex negative
(cat << EOF
3
1,-1,5,Main St.
1,2,6,Maple Ave.
EOF
) > graph.txt
run_test_args "graph.txt" "Error: Ending vertex '-1' on line 2 is not among valid values 1-3." "1"
rm -f graph.txt

# Test 52: Invalid number of vertices '3x'
(cat << EOF
3x
1,2,5,Maple Ave.
EOF
) > graph.txt
run_test_args "graph.txt" "Error: Invalid number of vertices '3x' on line 1." "1"
rm -f graph.txt

# Test 53: Edge line with only two fields '1,2'
(cat << EOF
3
1,2
EOF
) > graph.txt
run_test_args "graph.txt" "Error: Invalid edge data '1,2' on line 2." "1"
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
