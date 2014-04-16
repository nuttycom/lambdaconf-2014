SOURCE = slides.md

BIN    = .cabal-sandbox/bin
PANDOC = $(BIN)/pandoc

all: slides.html

clean:
	rm -f slides.html

slides.html: $(SOURCE)
	$(PANDOC) -f markdown -t revealjs -s $(SOURCE) --self-contained -o slides.html
