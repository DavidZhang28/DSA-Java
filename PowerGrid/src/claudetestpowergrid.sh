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

echo "=== ORIGINAL TEST CASES ==="

# Test 1: No command line argument
run_test_args "" "Usage: java PowerGrid <input file>" "1"

# Test 2: Too many arguments
run_test_args "file1.txt file2.txt" "Usage: java PowerGrid <input file>" "1"

# Test 3: File not found
run_test_args "notfound.txt" "Error: Cannot open file 'notfound.txt'." "1"

# Test 4: Duplicate edge (same direction)
(cat << ENDOFTEXT
3
1,2,110,Maple Ave.
1,3,90,Summit Ave.
1,3,20,Main St.
ENDOFTEXT
) > graph.txt
run_test_args "graph.txt" "Error: Duplicate edge '1,3,20,Main St.' found on line 4." "1"
rm -f graph.txt

# Test 5: Duplicate edge (reverse direction)
(cat << ENDOFTEXT
3
1,3,90,Summit Ave.
3,1,67,Main St.
1,2,110,Maple Ave.
ENDOFTEXT
) > graph.txt
run_test_args "graph.txt" "Error: Duplicate edge '3,1,67,Main St.' found on line 3." "1"
rm -f graph.txt

# Test 6: Invalid number of vertices (negative)
(cat << ENDOFTEXT
-3
1,2,110,Maple Ave.
1,3,90,Summit Ave.
2,240,Main St.
ENDOFTEXT
) > graph.txt
run_test_args "graph.txt" "Error: Invalid number of vertices '-3' on line 1." "1"
rm -f graph.txt

# Test 7: Invalid edge data (missing component)
(cat << ENDOFTEXT
3
1,2,110,Maple Ave.
1,3,90,Summit Ave.
2,240,Main St.
ENDOFTEXT
) > graph.txt
run_test_args "graph.txt" "Error: Invalid edge data '2,240,Main St.' on line 4." "1"
rm -f graph.txt

# Test 8: Starting vertex out of range
(cat << ENDOFTEXT
3
1,2,110,Maple Ave.
4,3,90,Summit Ave.
2,3,240,Main St.
ENDOFTEXT
) > graph.txt
run_test_args "graph.txt" "Error: Starting vertex '4' on line 3 is not among valid values 1-3." "1"
rm -f graph.txt

# Test 9: Ending vertex not an integer
(cat << EOF
3
1,2,110,Maple Ave.
1,x,90,Summit Ave.
2,3,240,Main St.
EOF
) > graph.txt
run_test_args "graph.txt" "Error: Ending vertex 'x' on line 3 is not among valid values 1-3." "1"
rm -f graph.txt

# Test 10: Negative edge weight
(cat << EOF
3
1,2,110,Maple Ave.
1,3,90,Summit Ave.
2,3,-240,Main St.
EOF
) > graph.txt
run_test_args "graph.txt" "Error: Invalid edge weight '-240' on line 4." "1"
rm -f graph.txt

# Test 11: Disconnected graph
(cat << EOF
3
1,2,110,Maple Ave.
EOF
) > graph.txt
run_test_args "graph.txt" "No solution." "0"
rm -f graph.txt

# Test 12: Valid simple graph
(cat << EOF
3
1,2,110,Maple Ave.
1,3,90,Summit Ave.
2,3,240,Main St.
EOF
) > graph.txt
run_test_args "graph.txt" $'Total wire length (meters): 200\nMaple Ave. [110]\nSummit Ave. [90]' "0"
rm -f graph.txt

echo -e "\n=== EXTENDED TEST CASES ==="

# Test 13: Number of vertices is 0
(cat << EOF
0
EOF
) > graph.txt
run_test_args "graph.txt" "Error: Invalid number of vertices '0' on line 1." "1"
rm -f graph.txt

# Test 14: Number of vertices exceeds 1000
(cat << EOF
1001
EOF
) > graph.txt
run_test_args "graph.txt" "Error: Invalid number of vertices '1001' on line 1." "1"
rm -f graph.txt

# Test 15: Number of vertices is not an integer
(cat << EOF
abc
1,2,100,Main St.
EOF
) > graph.txt
run_test_args "graph.txt" "Error: Invalid number of vertices 'abc' on line 1." "1"
rm -f graph.txt

# Test 16: Starting vertex is 0 (below valid range)
(cat << EOF
3
0,2,100,Main St.
EOF
) > graph.txt
run_test_args "graph.txt" "Error: Starting vertex '0' on line 2 is not among valid values 1-3." "1"
rm -f graph.txt

# Test 17: Ending vertex is 0
(cat << EOF
3
1,0,100,Main St.
EOF
) > graph.txt
run_test_args "graph.txt" "Error: Ending vertex '0' on line 2 is not among valid values 1-3." "1"
rm -f graph.txt

# Test 18: Edge weight is 0 (invalid)
(cat << EOF
3
1,2,0,Main St.
EOF
) > graph.txt
run_test_args "graph.txt" "Error: Invalid edge weight '0' on line 2." "1"
rm -f graph.txt

# Test 19: Edge weight is a decimal
(cat << EOF
3
1,2,110.5,Main St.
EOF
) > graph.txt
run_test_args "graph.txt" "Error: Invalid edge weight '110.5' on line 2." "1"
rm -f graph.txt

# Test 20: Edge weight is not a number
(cat << EOF
3
1,2,abc,Main St.
EOF
) > graph.txt
run_test_args "graph.txt" "Error: Invalid edge weight 'abc' on line 2." "1"
rm -f graph.txt

# Test 21: Too many components in edge data
(cat << EOF
3
1,2,100,Main St.,Extra
EOF
) > graph.txt
run_test_args "graph.txt" "Error: Invalid edge data '1,2,100,Main St.,Extra' on line 2." "1"
rm -f graph.txt

# Test 22: Single vertex, no edges (valid MST with 0 weight)
(cat << EOF
1
EOF
) > graph.txt
run_test_args "graph.txt" $'Total wire length (meters): 0' "0"
rm -f graph.txt

# Test 23: Two vertices, one edge (valid)
(cat << EOF
2
1,2,50,First Ave.
EOF
) > graph.txt
run_test_args "graph.txt" $'Total wire length (meters): 50\nFirst Ave. [50]' "0"
rm -f graph.txt

# Test 24: Multiple disconnected components
(cat << EOF
4
1,2,50,First Ave.
3,4,60,Second Ave.
EOF
) > graph.txt
run_test_args "graph.txt" "No solution." "0"
rm -f graph.txt

# Test 25: Complete graph (4 vertices) - MST should select minimum edges
(cat << EOF
4
1,2,10,A St.
1,3,20,B St.
1,4,30,C St.
2,3,15,D St.
2,4,25,E St.
3,4,35,F St.
EOF
) > graph.txt
run_test_args "graph.txt" $'Total wire length (meters): 45\nA St. [10]\nD St. [15]\nE St. [25]' "0"
rm -f graph.txt

# Test 26: Lexicographic sorting with numbers
(cat << EOF
3
1,2,10,1st Ave.
1,3,20,2nd Ave.
2,3,30,10th Ave.
EOF
) > graph.txt
run_test_args "graph.txt" $'Total wire length (meters): 30\n10th Ave. [30]\n1st Ave. [10]' "0"
rm -f graph.txt

# Test 27: Duplicate street names (different edges)
(cat << EOF
4
1,2,10,Main St.
2,3,20,Main St.
3,4,30,Main St.
EOF
) > graph.txt
run_test_args "graph.txt" $'Total wire length (meters): 60\nMain St. [10]\nMain St. [20]\nMain St. [30]' "0"
rm -f graph.txt

# Test 28: TIE-BREAKING TEST - Same weight, prefer lower vertex number
# From vertex 1, we can reach 2, 3, or 4 with weight 100
# Should prefer vertex 2 (lowest), then 3, then 4
(cat << EOF
4
1,2,100,Z St.
1,3,100,A St.
1,4,100,M St.
EOF
) > graph.txt
run_test_args "graph.txt" $'Total wire length (meters): 300\nA St. [100]\nM St. [100]\nZ St. [100]' "0"
rm -f graph.txt

# Test 29: Chain graph - linear structure
(cat << EOF
5
1,2,10,A St.
2,3,20,B St.
3,4,15,C St.
4,5,25,D St.
EOF
) > graph.txt
run_test_args "graph.txt" $'Total wire length (meters): 70\nA St. [10]\nB St. [20]\nC St. [15]\nD St. [25]' "0"
rm -f graph.txt

# Test 30: Star graph - hub and spokes
(cat << EOF
5
1,2,10,A St.
1,3,20,B St.
1,4,30,C St.
1,5,40,D St.
EOF
) > graph.txt
run_test_args "graph.txt" $'Total wire length (meters): 100\nA St. [10]\nB St. [20]\nC St. [30]\nD St. [40]' "0"
rm -f graph.txt

# Test 31: Empty street name
(cat << EOF
3
1,2,100,
1,3,50,Main St.
EOF
) > graph.txt
run_test_args "graph.txt" $'Total wire length (meters): 150\n [100]\nMain St. [50]' "0"
rm -f graph.txt

# Test 32: Street name with spaces
(cat << EOF
3
1,2,100,Main Street North
1,3,50,Elm Ave.
EOF
) > graph.txt
run_test_args "graph.txt" $'Total wire length (meters): 150\nElm Ave. [50]\nMain Street North [100]' "0"
rm -f graph.txt

# Test 33: Special characters in street names
(cat << EOF
3
1,2,100,O'Malley St.
1,3,50,Über Ave.
EOF
) > graph.txt
run_test_args "graph.txt" $'Total wire length (meters): 150\nO\'Malley St. [100]\nÜber Ave. [50]' "0"
rm -f graph.txt

# CHECK THIS ONE
# CHECK THIS ONE
# CHECK THIS ONE
# CHECK THIS ONE
# CHECK THIS ONE
# CHECK THIS ONE
# CHECK THIS ONE
# Test 34: Large weight values
(cat << EOF
3
1,2,999999999,Main St.
1,3,888888888,Elm St.
2,3,777777777,Oak St.
EOF
) > graph.txt
run_test_args "graph.txt" $'Total wire length (meters): 1666666665\nOak St. [777777777]\nElm St. [888888888]' "0"
rm -f graph.txt

# Test 35: Ending vertex greater than numVertices
(cat << EOF
3
1,2,100,Main St.
1,5,50,Elm St.
EOF
) > graph.txt
run_test_args "graph.txt" "Error: Ending vertex '5' on line 3 is not among valid values 1-3." "1"
rm -f graph.txt

# Test 36: Negative starting vertex
(cat << EOF
3
-1,2,100,Main St.
EOF
) > graph.txt
run_test_args "graph.txt" "Error: Starting vertex '-1' on line 2 is not among valid values 1-3." "1"
rm -f graph.txt

# Test 37: TIE-BREAKING TEST - Complex case with multiple same-weight edges
# Start from 1, have edges to 2,3,4,5 all weight 50
# Should select in order: 2, 3, 4, 5 (by vertex number)
(cat << EOF
5
1,2,50,D St.
1,3,50,C St.
1,4,50,B St.
1,5,50,A St.
EOF
) > graph.txt
run_test_args "graph.txt" $'Total wire length (meters): 200\nA St. [50]\nB St. [50]\nC St. [50]\nD St. [50]' "0"
rm -f graph.txt

# Test 38: TIE-BREAKING TEST - After connecting vertex 2, choose lower vertex from 2
# 1->2 (weight 10), then from 2 can go to 3 or 4 (both weight 20), should choose 3
(cat << EOF
4
1,2,10,A St.
2,3,20,B St.
2,4,20,C St.
3,4,5,D St.
EOF
) > graph.txt
run_test_args "graph.txt" $'Total wire length (meters): 35\nA St. [10]\nB St. [20]\nD St. [5]' "0"
rm -f graph.txt

# Test 39: Graph with cycle - MST selects correctly
(cat << EOF
4
1,2,1,A St.
2,3,2,B St.
3,4,3,C St.
4,1,4,D St.
1,3,10,E St.
EOF
) > graph.txt
run_test_args "graph.txt" $'Total wire length (meters): 6\nA St. [1]\nB St. [2]\nC St. [3]' "0"
rm -f graph.txt

# Test 40: Whitespace handling in vertex numbers
(cat << EOF
3
 1 , 2 ,100,Main St.
1,3,50,Elm St.
EOF
) > graph.txt
run_test_args "graph.txt" $'Total wire length (meters): 150\nElm St. [50]\nMain St. [100]' "0"
rm -f graph.txt

# Test 41: Edge with very long street name
(cat << EOF
2
1,2,100,This Is A Very Long Street Name With Many Words In It
EOF
) > graph.txt
run_test_args "graph.txt" $'Total wire length (meters): 100\nThis Is A Very Long Street Name With Many Words In It [100]' "0"
rm -f graph.txt

# Test 42: Case sensitivity in street names (lowercase before uppercase in ASCII)
(cat << EOF
3
1,2,100,main st.
1,3,50,Main St.
EOF
) > graph.txt
run_test_args "graph.txt" $'Total wire length (meters): 150\nMain St. [50]\nmain st. [100]' "0"
rm -f graph.txt

# Test 43: Decimal number of vertices
(cat << EOF
3.5
1,2,100,Main St.
EOF
) > graph.txt
run_test_args "graph.txt" "Error: Invalid number of vertices '3.5' on line 1." "1"
rm -f graph.txt

# Test 44: Extra whitespace in weight
(cat << EOF
3
1,2, 100 ,Main St.
1,3,50,Elm St.
EOF
) > graph.txt
run_test_args "graph.txt" $'Total wire length (meters): 150\nElm St. [50]\nMain St. [100]' "0"
rm -f graph.txt

# Test 45: TIE-BREAKING with weights - Diamond graph
# Multiple paths, same total weight options
(cat << EOF
4
1,2,5,A St.
2,3,5,B St.
3,4,5,C St.
1,4,15,D St.
EOF
) > graph.txt
run_test_args "graph.txt" $'Total wire length (meters): 15\nA St. [5]\nB St. [5]\nC St. [5]' "0"
rm -f graph.txt

# Test 46: TIE-BREAKING - All equal weights in triangle, should prefer lower vertices
# From 1: can reach 2 (weight 100) or 3 (weight 100), prefer 2
# From {1,2}: can reach 3 (weight 100)
(cat << EOF
3
1,2,100,Z St.
1,3,100,A St.
2,3,100,M St.
EOF
) > graph.txt
run_test_args "graph.txt" $'Total wire length (meters): 200\nM St. [100]\nZ St. [100]' "0"
rm -f graph.txt


# Test 47: Larger MST test - ensure correctness
(cat << EOF
6
1,2,7,AB
1,3,9,AC
1,6,14,AF
2,3,10,BC
2,4,15,BD
3,4,11,CD
3,6,2,CF
4,5,6,DE
5,6,9,EF
EOF
) > graph.txt
run_test_args "graph.txt" $'Total wire length (meters): 39\nAB [7]\nBC [10]\nCF [2]\nCD [11]\nDE [6]\nEF [9]' "0"
rm -f graph.txt

# Test 48: Graph with multiple components, one isolated vertex
(cat << EOF
3
1,2,50,Main St.
EOF
) > graph.txt
run_test_args "graph.txt" "No solution." "0"
rm -f graph.txt

# Test 49: Maximum valid vertices (1000) with valid connection
(cat << EOF
1000
1,2,10,First
2,3,20,Second
3,4,30,Third
EOF
) > graph.txt
run_test_args "graph.txt" "No solution." "0"
rm -f graph.txt

# Test 50: Vertices with large numbers but within range
(cat << EOF
1000
1,1000,100,Far Connection
1,500,50,Mid Connection
500,1000,75,Another Connection
EOF
) > graph.txt
run_test_args "graph.txt" $'Total wire length (meters): 150\nAnother Connection [75]\nMid Connection [50]' "0"
rm -f graph.txt

# Test 51: Empty line in edge data
(cat << EOF
3
1,2,100,Main St.

1,3,50,Elm St.
EOF
) > graph.txt
run_test_args "graph.txt" "Error: Invalid edge data '' on line 3." "1"
rm -f graph.txt

# Test 52: Multiple spaces in street name
(cat << EOF
3
1,2,100,Main     Street
1,3,50,Elm St.
EOF
) > graph.txt
run_test_args "graph.txt" $'Total wire length (meters): 150\nElm St. [50]\nMain     Street [100]' "0"
rm -f graph.txt

# CHECK THIS ONE
# CHECK THIS ONE
# CHECK THIS ONE
# CHECK THIS ONE
# CHECK THIS ONE
# CHECK THIS ONE
# CHECK THIS ONE
# CHECK THIS ONE
# CHECK THIS ONE
# Test 53: Street name with only spaces
(cat << EOF
3
1,2,100,   
1,3,50,Elm St.
EOF
) > graph.txt
run_test_args "graph.txt" $'Total wire length (meters): 150\n    [100]\nElm St. [50]' "0"
rm -f graph.txt

# Test 54: ASCII sorting - numbers before uppercase before lowercase
(cat << EOF
4
1,2,10,9th St.
2,3,20,ABC St.
3,4,30,abc St.
EOF
) > graph.txt
run_test_args "graph.txt" $'Total wire length (meters): 60\n9th St. [10]\nABC St. [20]\nabc St. [30]' "0"
rm -f graph.txt

# Test 55: TIE-BREAKING - Sequential vertices with same weight
# From 1, go to 2 (weight 50)
# From 2, go to 3 (weight 50)  
# From 3, go to 4 (weight 50)
(cat << EOF
4
1,2,50,A St.
2,3,50,B St.
3,4,50,C St.
EOF
) > graph.txt
run_test_args "graph.txt" $'Total wire length (meters): 150\nA St. [50]\nB St. [50]\nC St. [50]' "0"
rm -f graph.txt

# Test 56: Very large weight approaching long max (but valid)
(cat << EOF
2
1,2,9223372036854775806,Huge Street
EOF
) > graph.txt
run_test_args "graph.txt" $'Total wire length (meters): 9223372036854775806\nHuge Street [9223372036854775806]' "0"
rm -f graph.txt

# Test 57: Leading zeros in vertex numbers
(cat << EOF
3
01,02,100,Main St.
1,3,50,Elm St.
EOF
) > graph.txt
run_test_args "graph.txt" $'Total wire length (meters): 150\nElm St. [50]\nMain St. [100]' "0"
rm -f graph.txt

# Test 58: Leading zeros in weight
(cat << EOF
3
1,2,0100,Main St.
1,3,50,Elm St.
EOF
) > graph.txt
run_test_args "graph.txt" $'Total wire length (meters): 150\nElm St. [50]\nMain St. [100]' "0"
rm -f graph.txt

# Test 59: TIE-BREAKING - More complex scenario
# Build MST step by step with ties at each step
(cat << EOF
6
1,2,10,A
1,3,10,B
2,4,10,C
2,5,10,D
3,4,10,E
3,6,10,F
EOF
) > graph.txt
run_test_args "graph.txt" $'Total wire length (meters): 50\nA [10]\nB [10]\nC [10]\nD [10]\nF [10]' "0"
rm -f graph.txt

# Test 60: TIE-BREAKING - Verify vertex preference over street name
# Same weight, vertex 2 vs 5, should choose vertex 2 regardless of street names
(cat << EOF
5
1,5,100,A Street
1,2,100,Z Street
2,3,50,M Street
3,4,50,N Street
EOF
) > graph.txt
run_test_args "graph.txt" $'Total wire length (meters): 200\nM Street [50]\nN Street [50]\nZ Street [100]' "0"
rm -f graph.txt

echo -e "\n=== TEST SUMMARY ==="
echo -e "Total tests run    : $total"
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