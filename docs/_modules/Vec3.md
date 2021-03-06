---
name: Vec3
subtitle: Manipulating Location and Motion
type: class
layout: module
properties:
  - name: x
    type: number
    access: r/w
    description: The X-component of the vector
  - name: y
    type: number
    access: r/w
    description: The Y-component of the vector
  - name: z
    type: number
    access: r/w
    description: The Z-component of the vector
functions:
  - name: tostring
    parameters:
    results: string
    description: Returns a string with the following format '(x, y, z)'.
    examples:
      - url: Vec3/tostring.md
  - name: add
    parameters: other
    results: Vec3
    description: Returns a new vector that is the result of adding the other vector to the current vector.
    examples:
      - url: Vec3/add.md
  - name: substract
    parameters: other
    results: Vec3
    description: Returns a new vector that is the result of substracting the other vector from the current vector.
    examples:
      - url: Vec3/substract.md
  - name: sqrMagnitude
    parameters:
    results: Vec3
    description: Returns the squared length of the current vector.
    examples:
      - url: Vec3/sqrMagnitude.md
  - name: magnitude
    parameters:
    results: Vec3
    description: Returns the length of the current vector.
    examples:
      - url: Vec3/magnitude.md
  - name: dotProduct
    parameters: other
    results: Vec3
    description: Returns the 'dot' product of the current vector and the other vector.
    examples:
      - url: Vec3/dotProduct.md
  - name: scale
    parameters: factor
    results: Vec3
    description: Returns a copy of current vector, scaled by the given factor.
    examples:
      - url: Vec3/scale.md
  - name: invert
    parameters:
    results: Vec3
    description: Returns an inverted version of the current vector.
    examples:
      - url: Vec3/invert.md
  - name: normalize
    parameters:
    results: Vec3
    description: "Returns a normalized version of the current vector, which means
    a vector with a magnitude of 1 meter and pointing
    into the same direction as the original vector.
    "
    examples:
      - url: Vec3/normalize.md
  - name: chunk
    parameters:
    results: number, number
    description: |
        The 'chunk' function interprets this vector as world coordinate, converts them into chunk coordinates, and returns them as a multi-value result.

        #### Example

        Converting the spell's world coordinates into chunk coordinates.

        ```lua
        local chunkX, chunkZ = spell.pos:chunk()
        ```
---

An instance of the <span class="notranslate">Vec3</span> class represents a '3-dimensional Vector'.

Mostly a 3-dimensional vector is used to denote a position in the
3-dimensional world space or a constant velocity of an object inside that space.

However, a vector can be used for many other 'things' that can be described by
3 independent numerical values.

To create a vector you can call the <span class="notranslate">Vec3</span> function:
```lua
myvec = Vec3( 1, 2, 3)
```
This creates a vector called 'myvec' with the component values x=1, y=2, z=3.
