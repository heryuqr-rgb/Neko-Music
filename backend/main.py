from fastapi import FastAPI, HTTPException, Query
from fastapi.middleware.cors import CORSMiddleware
from ytmusicapi import YTMusic

app = FastAPI(title="Neko Music API", version="1.0.0")
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["GET"],
    allow_headers=["*"],
)
yt = YTMusic()


@app.get("/health")
def health():
    return {"ok": True, "service": "neko-music-api"}


@app.get("/search")
def search(q: str = Query(..., min_length=1, max_length=120), limit: int = Query(20, ge=1, le=30)):
    try:
        results = yt.search(q, filter="songs", limit=limit)
        tracks = []
        for item in results:
            video_id = item.get("videoId")
            if not video_id:
                continue
            artists = item.get("artists") or []
            thumbnails = item.get("thumbnails") or []
            tracks.append({
                "videoId": video_id,
                "title": item.get("title", "Unknown title"),
                "artist": ", ".join(a.get("name", "") for a in artists if a.get("name")) or "Unknown artist",
                "album": (item.get("album") or {}).get("name", ""),
                "duration": item.get("duration", ""),
                "thumbnail": thumbnails[-1].get("url", "") if thumbnails else "",
            })
        return {"query": q, "results": tracks}
    except Exception as exc:
        raise HTTPException(status_code=502, detail=f"YouTube Music catalog lookup failed: {type(exc).__name__}") from exc
