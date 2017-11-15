# libpe

A C/C++ library to parse Windows portable executables ( both PE32 and PE32+ ) written with **speed** and **stability** in mind.

Copyleft of Simone Margaritelli <evilsocket@gmail.com>
[http://www.evilsocket.net/](http://www.evilsocket.net/)

## Features

* Fully compatible with both x86 and AMD64 binaries.
* Parse only what you are really interested into to reduce useless data parsing overhead.
* Export directory parsing, lookup symbols by name or ordinal.
* Import directory parsing with different configurations ( resolve ordinals, name demangling, etc ), fast modules and symbols lookup by name.
* Fast strings extraction ( both ascii and unicode ).
* Every RVA is returned as a structure with an absolute virtual address field ( base + RVA ), the corresponding raw offset into the file and a pointer to the data.
* Clean and doxygen documented API.
* Minimum speed and memory overhead.

## Samples

* **peview** a sample PE dumper tool that will print informations extracted from any portable executable.
* **pefunctions** a proof of concept using libpe and distorm, analyzes and isolates functions from any portable executable.

## TODO

* Cross platform compatibilty.
* Cross platform cmake files.
* Parsing of other directories such as resources, debug info, etc.
* Basic disassembly features.

## License

Release under the GPL 3 license.

[![Bitdeli Badge](https://d2weczhvl823v0.cloudfront.net/evilsocket/libpe/trend.png)](https://bitdeli.com/free "Bitdeli Badge")

