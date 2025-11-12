SRC_DIR = src/
BUILD_DIR = build/
MAIN_CLASS = gatorAirTrafficScheduler


build:
	cd ${SRC_DIR} && javac -d ../${BUILD_DIR} *.java

run:
	cd ${BUILD_DIR} && java ${MAIN_CLASS} ${INPUT} 

clean:
	rm -rf ${BUILD_DIR}

# Allow arbitrary args after 'make run'
%:
	@:
