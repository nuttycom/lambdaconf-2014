SOURCE = slides.md
STYLE = style.txt

BIN    = .cabal-sandbox/bin
PANDOC = $(BIN)/pandoc

all: slides.html

clean:
	rm -f slides.html

slides.html: $(SOURCE) $(STYLE)
	$(PANDOC) -f markdown --smart -t revealjs -V theme=beige --include-in-header=$(STYLE) -s $(SOURCE) -o slides.html
