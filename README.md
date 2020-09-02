# Ignis Disassembler

## What is Ignis?
Ignis is a domain specific/scripting language used to model compiled JVM .class files. The idea is to include all of the same information in the binary format of the class file but have it in a textual form to faciltate editors to provide language support for modifying the .class file content.

## Language Model
To reduce the complexities of the [JVM specification](https://docs.oracle.com/javase/specs/jvms/se14/html/index.html) we have made the following few simplifications:
 * No relative offets for any program references
 * No constant pool management - all literals are placed directly where they are used in the scripts
 * Reduced instruction set, e.g. instead of supporting multiple iconst_x instructions we have a single iconst x instruction
 * Extensible meta attributes to provide supporting information to class file elements (e.g. annotations) such that unknown/unsupported data can be embedded in the script

## Planned Features
 - Eclipse language editor with Java nature integration
   - run/debug configurations
   - refactoring/search capabilities
   - reference linking
   - compile time type checking
 - Coarser language abstractions using the Spade IR toolsystem
 - Preprocessor system for simplifying output code before editing
