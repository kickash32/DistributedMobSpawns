# DistributedMobSpawns
This is a plugin for Spigot/Paper to fix Minecraft mutliplayer mob spawning to be more like singleplayer. Spigot support is currently untested especially in low TPS senarios. DistributedMobSpawns (DMS) will blacklist chunks around players who have filled thier quota of monsters (configurable through the default mob caps in bukkit.yml) within the monster despawn range (128). BlackListed chunks will not be able to spawn monsters, ensuring other chunks spawn monsters instead.
# Installation
Drag the release to the Plugins folder and start the server, currently no setup files.
# Compiling
Setup your enviornment to compile a plugin targetting latest Paper API
# Planned features

HIGH priority:
Comments in code,
Abstraction,
buffer configuration

MEDIUM priority:
dynamic total cap,
support of all mob types

LOW priority:
A better command handler with colors
