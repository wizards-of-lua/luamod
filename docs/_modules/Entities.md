---
name: Entities
subtitle: Finding Entities
type: module
layout: module
properties:
functions:
  - name: find
    parameters: string
    results: table
    description: |
        The 'find' function returns a table of [Entity](/modules/Entity/) objects that match the given selector.

        #### Example
        Printing the number of all [players](/modules/Player) currently logged in.
        ```lua
        found = Entities.find("@a")
        print(#found)
        ```

        #### Example
        Printing the position of [player](/modules/Player) mickkay:
        ```lua
        found = Entities.find("@a[name=mickkay]")[1]
        print(found.pos)
        ```

        #### Example
        Printing the positions of all cows in the (loaded part of the) world.
        ```lua
        found = Entities.find("@e[type=cow]")
        for _,cow in pairs(found) do
          print(cow.pos)
        end
        ```

        #### Example
        Printing the names of all dropped items in the (loaded part of the) world.
        ```lua
        found = Entities.find("@e[type=item]")
        for _,e in pairs(found) do
          print(e.name)
        end
        ```

---

The <span class="notranslate">Entities</span> module provides access to all [Entity](/modules/Entity/) objects currently loaded.