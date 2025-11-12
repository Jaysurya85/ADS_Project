# Makefile for Gator Air Traffic Scheduler

# Directories
SRC_DIR = .
MODELS_DIR = models
SRC_PKG_DIR = src
BUILD_DIR = build

# Java compiler
JC = javac
JFLAGS = -d $(BUILD_DIR) -cp $(SRC_DIR)

# Java runtime
JAVA = java
JRFLAGS = -cp $(BUILD_DIR)

# Find all Java files
MODELS_SOURCES = $(wildcard $(MODELS_DIR)/*.java)
SRC_SOURCES = $(wildcard $(SRC_PKG_DIR)/*.java)
MAIN_SOURCE = gatorAirTrafficScheduler.java

ALL_SOURCES = $(MODELS_SOURCES) $(SRC_SOURCES) $(MAIN_SOURCE)

# Default target
all: build

# Create build directory and compile
build: $(BUILD_DIR)
	@echo "Compiling Java sources..."
	$(JC) $(JFLAGS) $(ALL_SOURCES)
	@echo "Build complete!"

# Create build directory if it doesn't exist
$(BUILD_DIR):
	@mkdir -p $(BUILD_DIR)

# Run the program (requires input file argument)
run: build
	@echo "Usage: make run INPUT=<input_file>"
	@if [ -z "$(INPUT)" ]; then \
		echo "Error: Please specify INPUT=<input_file>"; \
		exit 1; \
	fi
	$(JAVA) $(JRFLAGS) gatorAirTrafficScheduler $(INPUT)

# Debug mode - run with verbose output
debug: build
	@echo "Usage: make debug INPUT=<input_file>"
	@if [ -z "$(INPUT)" ]; then \
		echo "Error: Please specify INPUT=<input_file>"; \
		exit 1; \
	fi
	$(JAVA) $(JRFLAGS) -Xdebug gatorAirTrafficScheduler $(INPUT)

# Clean compiled files
clean:
	@echo "Cleaning build directory..."
	rm -rf $(BUILD_DIR)
	@echo "Clean complete!"

# Clean and rebuild
rebuild: clean build

# Help target
help:
	@echo "Available targets:"
	@echo "  make           - Compile all Java files"
	@echo "  make build     - Same as 'make'"
	@echo "  make run       - Run the program (specify INPUT=filename)"
	@echo "  make debug     - Run in debug mode (specify INPUT=filename)"
	@echo "  make clean     - Remove all compiled files"
	@echo "  make rebuild   - Clean and rebuild"
	@echo ""
	@echo "Example:"
	@echo "  make run INPUT=test_input.txt"

.PHONY: all build run debug clean rebuild help
