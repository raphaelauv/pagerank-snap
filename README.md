# [Stanford Large Network Dataset Collection](https://snap.stanford.edu/data/index.html) (snap)

Java8 implementation of pagerank algorithm on the [Amazon product metadata dataset](https://snap.stanford.edu/data/amazon-meta.html)

## I) PAGE RANK

### 1. COMPILATION

```
javac *.java
```

### 2. EXECUTION

```
java PageRank [epsilon] [d] [fileName]
```
example : java PageRank 0.0001 0.15  tp1-fig1.txt

## II) COLLECTEUR

### 1. COMPILATION

```
javac *.java
```

### 2. EXECUTION
```
java Collecteur [dataAmazonMetaFile] [dictionnaryS of authorisez Files] [-fw] [dictionnaryS of forbidden words]
```
example : java Collecteur data Nouns.txt

### 2.1 GRAPH CREATION

```
java Collecteur createGraph [dataAmazonMetaFile]
```
example :java Collecteur createGraph data

this command will create a sorted graph of co-achat of the amazonFileMetaFile of name  dataAmazonMetaFile.graph

## III) SEARCH

### 1. COMPILATION

```
javac *.java
```

### 2. EXECUTION

```
java Search [page_Rank] [word_product] [forbiddent_words] [words]
```

example : java Search pageRank.out data.dict nothing ability abbreviations ability


if pageRank.out is not valide a random PageRank will be use

java Search pageRank.out data.dict forbiWords ability  abbreviations ability absence
