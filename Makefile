all: build
build:
	sbt assembly

.PHONY: test
test:
	sbt test

.PHONE: clean
clean:
	sbt clean
