SRCS = *.java

all:
	javac ${SRCS}

clean:
	rm -f *.class README.pdf

readme:
	gimli -s pdf_style.css -f README.md
