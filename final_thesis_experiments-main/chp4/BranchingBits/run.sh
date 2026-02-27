#!/bin/bash
echo "Branching positions in your datasets"
echo -e "shift\t4\t8\t16\t32\t64\t128\t256\t512\t1024\t2048"

javac BranchingBits.java

shift=0

while (( shift <= 100000 ))
do
    size=4
    printf "%d\t" $shift
    while (( size <= 2048 ))
    do
        java BranchingBits ${size} ${shift}
        size=$(( size * 2 ))
    done
    
    echo ""
    shift=$(( shift + 1 ))
done
