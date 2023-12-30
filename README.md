<h1 align="center">Weather Sync</h1>
<a href="https://github.com/macbrayne/inventory-pause-forge"><img src="https://img.shields.io/badge/side-server--side-0C8E8E" alt="Side: Server-Side"></a>
<a href="https://github.com/macbrayne/weathersync/blob/main/LICENSE"><img src="https://img.shields.io/github/license/macbrayne/weathersync?style=flat&color=0C8E8E" alt="License"></a>

Syncs your weather with the real world!

## How it works
It queries the OpenMeteo API to find out the weather at the provided location :)

## Configuration

This mod provides a bunch of in-game commands:
- `/weathersync enable`: Enables the mod for you
- `/weathersync disable`: Disables the mod for you
- `/weathersync location set <latitude> <longitude>`: Sets the location the mod queries to the latitude and longitude you provide (default: Berlin)
- `/weathersync sync`: Forces the game to send you your stored weather
- `/weathersync timer reset` (OP level 2 required): Resets the 30-minute timer which aims to prevent excessive API requests

**Please open an issue if it doesn't properly sync and you don't get the weather described in the message to you**

## License

This mod is licensed under [EUPL-1.2](LICENSE)
