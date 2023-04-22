# BuildLogger

A simple Minecraft plugin for creating 3D matrices representing Minecraft blocks, and related labels describing the matrices' contents.

Working for Minecraft 1.19+

This plugin produces 3D integer matrices representing blocks, and saves them in a SQLite database. The matrices are capable of logging 972 different blocks as integers.

It also includes preprocessing features:
- Automatically removes unnecessary air blocks when saving regions.
- More to come!
## TODO

- Implement a /dataset remove <id> command
- Implement a /dataset stats command
    - structures recorded
    - most popular words in labels