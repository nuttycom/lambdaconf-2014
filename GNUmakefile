SOURCE = slides.md
STYLE = style.txt

BIN    = .cabal-sandbox/bin
PANDOC = $(BIN)/pandoc

all: reveal

clean:
	rm -f reveal.html slidy.html

reveal: $(SOURCE) $(STYLE)
	$(PANDOC) -f markdown --smart -t revealjs -V theme=beige --include-in-header=$(STYLE) -s $(SOURCE) -o reveal.html

slidy: $(SOURCE) $(STYLE)
	$(PANDOC) -f markdown --smart -t slidy -s $(SOURCE) -o slidy.html
