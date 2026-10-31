# Neko Music

Neko Music is a lightweight Android companion for discovering YouTube Music catalog entries with the community-maintained Python library [ytmusicapi](https://github.com/sigma67/ytmusicapi).

## What it does
- Search songs, artists, albums and playlists through a small FastAPI backend.
- Show track title, artist, album and artwork.
- Open a selected track in the official YouTube Music app (or browser) for playback.
- Dark, lightweight native Android interface.

**Playback note:** ytmusicapi provides catalog metadata and search, not an authorized audio-streaming API. Neko Music does not extract, proxy, or download YouTube audio. Playback is handed off to YouTube Music, where YouTube's own account, Premium and advertising rules apply.

## Backend setup
Python 3.11+ recommended.

```bash
cd backend
python -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
uvicorn main:app --host 0.0.0.0 --port 8000
```

Set `NEKO_API_BASE_URL` in the Android app build if deploying somewhere other than the default demo placeholder. For local device testing, use your computer's LAN IP, not `localhost`.

### Deploy backend
A Render blueprint is included in `render.yaml`. Create a new Render Web Service from this repository using that blueprint. Copy its HTTPS URL and set `API_BASE_URL` in the GitHub Actions workflow variables (or change `app/build.gradle`).

## Build APK
GitHub Actions builds a debug APK on pushes to `main` and on manual dispatch. Download `NekoMusic-debug-apk` from the successful workflow run.

## License
The app source is provided under MIT. ytmusicapi is a separate third-party project; see its own license and terms.
