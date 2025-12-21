#!/bin/bash

# Generic runner with short aliases
# Usage: ./run.sh <alias-or-class>
# Example: ./run.sh skipindex-bench

# Function to resolve alias to class name
resolve_alias() {
    case "$1" in
        skipindex-gen)
            echo "org.kanatti.learning.skipindex.SkipIndexDataGenerator"
            ;;
        skipindex-bench)
            echo "org.kanatti.learning.skipindex.SkipIndexBenchmark"
            ;;
        skipindex-demo)
            echo "org.kanatti.learning.skipindex.ManualSkipperDemo"
            ;;
        skipindex-inspect)
            echo "org.kanatti.learning.skipindex.SkipIndexInspector"
            ;;
        # Add more aliases here as you create new experiments
        # bitpack-bench)
        #     echo "org.kanatti.minilucene.benchmarks.BitpackBenchmark"
        #     ;;
        *)
            echo ""
            ;;
    esac
}

# Function to show available aliases
show_aliases() {
    echo "Available aliases:"
    echo "  skipindex-gen     -> org.kanatti.learning.skipindex.SkipIndexDataGenerator"
    echo "  skipindex-bench   -> org.kanatti.learning.skipindex.SkipIndexBenchmark"
    echo "  skipindex-demo    -> org.kanatti.learning.skipindex.ManualSkipperDemo"
    echo "  skipindex-inspect -> org.kanatti.learning.skipindex.SkipIndexInspector"
    # Add more as needed
}

if [ -z "$1" ]; then
    echo "Usage: ./run.sh <alias-or-class>"
    echo ""
    show_aliases
    echo ""
    echo "Or provide full class name:"
    echo "  ./run.sh org.kanatti.minilucene.Example"
    exit 1
fi

INPUT=$1

# Try to resolve as alias
RESOLVED=$(resolve_alias "$INPUT")

if [ -n "$RESOLVED" ]; then
    CLASS_NAME="$RESOLVED"
    echo "Resolved alias '$INPUT' -> $CLASS_NAME"
else
    # Assume it's a full class name
    CLASS_NAME=$INPUT
fi

./gradlew -q runMain -PmainClass="$CLASS_NAME"
