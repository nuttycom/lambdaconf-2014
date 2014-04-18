SOURCE = slides.md
STYLE = style.txt

BIN    = .cabal-sandbox/bin
PANDOC = $(BIN)/pandoc

all: reveal

clean:
	rm -f index.html

reveal: $(SOURCE) $(STYLE)
	$(PANDOC) -f markdown --smart -t revealjs -V theme=beige --include-in-header=$(STYLE) -s $(SOURCE) -o index.html
