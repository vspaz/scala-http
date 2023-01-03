all: build
build:
	sbt assembly

.PHONY: run
run:
	sbt run

.PHONY: test
test:
	sbt test

.PHONY: clean
clean:
	sbt clean

.PHONY: reload
reload:
	sbt reload


