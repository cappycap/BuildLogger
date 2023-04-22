# BuildLogger

## Introduction

BuildLogger connects with WorldEdit to allow production of 3D integer matrices representing Minecraft blocks, which are saved in a SQLite database with user-prescribed labels. 

## Commands

### /datset add (labels)

Use a WorldEdit Wand *(accessible with **//wand**)* or **//pos1** and **//pos2** to define a region. 

Capture whatever you want within the region! It could be a house, a tree, a bench, a piece of architecture on a larger build like a fancy arch, or even an entire castle!

Next, run this command and add a list a minimum of 5 but as many labels as you wish. Put your most accurate descriptors first, and more obscure ones after. Here are some examples:

![Modern House](/img/modern.jpg)
`/dataset add house, modern, wood pillar, in-ground pool, two floors, small`

![Castle Arch](/img/castlearch.png)
`/dataset add outpost, guard gate, stone bridge, castle, two towers, large`

![Watering Well](/img/well.png)
`/dataset add water well, water hole, covered, bucket, stone, small`

When capturing larger builds, use more labels to better describe what encapsulates the space, or split up the build into smaller dataset entries.

## Preprocessing Features

### Remove unnecessary air blocks when saving regions.
For a given matrix represented by X width, Y height, and Z length, for every dimension **D** we find a **D<sub>min</sub>** and **D<sub>max</sub>** representing the first and last non-air block in the region. We create a new matrix with these mins and maxes as bounds.

### Block Simplification (TODO)
There are **973 blocks** natively tracked as integers in our matrices. If we were to allow all possible blocks into training, it would significantly increase training time.

We may want to explore some way of extracting features from the integers representing blocks and saving numbers with new meaning that we extrapolate back to later.
Or, simplify large sections of blocks to a similar block, like considering all wood types to be oak.

No matter what, our database should store both a "pure" matrix with 973 blocks of representation and then potentially other simplified ones.

## TODO

- Implement command autocompletion
- Implement a /dataset help command
- Implement a /dataset view (id) command
- Implement a /dataset stats command
    - structures recorded
    - most popular words in labels