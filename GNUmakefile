SOURCE = slides.md

BIN    = .cabal-sandbox/bin
PANDOC = $(BIN)/pandoc

all: slides.html

clean:
	rm -f slides.html

slides.html: $(SOURCE)
	$(PANDOC) -t revealjs -V theme=solarized -s $(SOURCE) -o slides.html
