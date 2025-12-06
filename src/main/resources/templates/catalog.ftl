<#-- AudioFilesSorter Music Catalog - SPA with Audio Player -->
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Music Catalog - AudioFilesSorter</title>
    <style>
        :root {
            --color-bg: #0f0f0f;
            --color-card: #1a1a1a;
            --color-card-hover: #252525;
            --color-text: #ffffff;
            --color-text-secondary: #a0a0a0;
            --color-border: #333333;
            --color-accent: #1db954;
            --color-accent-hover: #1ed760;
            --player-height: 90px;
        }

        * { margin: 0; padding: 0; box-sizing: border-box; }

        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
            background-color: var(--color-bg);
            color: var(--color-text);
            line-height: 1.6;
            padding-bottom: var(--player-height);
        }

        /* Header */
        .header {
            background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
            padding: 2.5rem 2rem;
            text-align: center;
            border-bottom: 1px solid var(--color-border);
            position: relative;
            overflow: hidden;
            min-height: 200px;
        }

        .header-bg {
            position: absolute;
            top: 0;
            left: 0;
            right: 0;
            bottom: 0;
            display: flex;
            flex-wrap: wrap;
            opacity: 0.15;
            filter: blur(2px);
            overflow: hidden;
        }

        .header-bg-img {
            width: 80px;
            height: 80px;
            object-fit: cover;
            flex-shrink: 0;
        }

        .header-overlay {
            position: absolute;
            top: 0;
            left: 0;
            right: 0;
            bottom: 0;
            background: linear-gradient(135deg, rgba(26,26,46,0.85) 0%, rgba(22,33,62,0.9) 50%, rgba(15,52,96,0.85) 100%);
        }

        .header::after {
            content: '';
            position: absolute;
            top: -50%;
            left: -50%;
            width: 200%;
            height: 200%;
            background: radial-gradient(circle, rgba(29, 185, 84, 0.1) 0%, transparent 50%);
            animation: pulse 8s ease-in-out infinite;
            pointer-events: none;
        }

        @keyframes pulse {
            0%, 100% { transform: scale(1); opacity: 0.5; }
            50% { transform: scale(1.1); opacity: 0.8; }
        }

        .header-content {
            position: relative;
            z-index: 1;
        }

        .header h1 {
            font-size: 2.5rem;
            font-weight: 800;
            background: linear-gradient(90deg, #fff 0%, #1db954 50%, #fff 100%);
            background-size: 200% auto;
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            background-clip: text;
            animation: shimmer 3s linear infinite;
            letter-spacing: -0.5px;
        }

        @keyframes shimmer {
            0% { background-position: 0% center; }
            100% { background-position: 200% center; }
        }

        .header .subtitle {
            color: var(--color-text-secondary);
            font-size: 0.9rem;
            margin-top: 0.5rem;
        }

        .header .stats {
            margin-top: 1.5rem;
            display: flex;
            justify-content: center;
            gap: 3rem;
        }

        .header .stat-item {
            text-align: center;
        }

        .header .stat-value {
            font-size: 2rem;
            font-weight: 700;
            color: var(--color-accent);
            text-shadow: 0 0 20px rgba(29, 185, 84, 0.3);
        }

        .header .stat-label {
            font-size: 0.7rem;
            color: var(--color-text-secondary);
            text-transform: uppercase;
            letter-spacing: 1px;
        }

        /* Navigation & Filters */
        .nav {
            background: #181818;
            padding: 0.75rem 2rem;
            border-bottom: 1px solid var(--color-border);
            position: sticky;
            top: 0;
            z-index: 100;
        }

        .nav-content {
            display: flex;
            align-items: center;
            gap: 1rem;
            max-width: 1600px;
            margin: 0 auto;
            height: 42px;
        }

        .nav a.back-link {
            color: var(--color-text);
            text-decoration: none;
            height: 42px;
            padding: 0 1rem;
            border-radius: 6px;
            display: flex;
            align-items: center;
            gap: 0.5rem;
            font-size: 0.875rem;
            font-weight: 500;
            background: #282828;
            transition: background 0.15s;
            white-space: nowrap;
        }

        .nav a.back-link:hover {
            background: #333;
        }

        .search-box {
            flex: 1;
            max-width: 400px;
            height: 42px;
            display: flex;
            align-items: center;
            background: #282828;
            border-radius: 6px;
            padding: 0 1rem;
            gap: 0.5rem;
        }

        .search-box:focus-within {
            outline: 2px solid var(--color-accent);
            outline-offset: -2px;
        }

        .search-box .search-icon {
            color: var(--color-text-secondary);
            font-size: 1rem;
        }

        .search-box input {
            flex: 1;
            background: transparent;
            border: none;
            color: var(--color-text);
            font-size: 0.875rem;
            outline: none;
            min-width: 0;
        }

        .search-box input::placeholder { color: var(--color-text-secondary); }

        .search-box .search-clear {
            background: none;
            border: none;
            color: var(--color-text-secondary);
            cursor: pointer;
            font-size: 1rem;
            padding: 0.25rem;
            opacity: 0;
            transition: opacity 0.15s;
        }

        .search-box .search-clear.visible { opacity: 1; }
        .search-box .search-clear:hover { color: var(--color-text); }

        .nav-divider {
            width: 1px;
            height: 24px;
            background: var(--color-border);
        }

        .filter-select {
            height: 42px;
            background: #282828;
            border: none;
            color: var(--color-text);
            padding: 0 2rem 0 1rem;
            border-radius: 6px;
            font-size: 0.875rem;
            cursor: pointer;
            appearance: none;
            -webkit-appearance: none;
            background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='10' height='10' fill='%23999' viewBox='0 0 16 16'%3E%3Cpath d='M8 12L2 6h12l-6 6z'/%3E%3C/svg%3E");
            background-repeat: no-repeat;
            background-position: right 0.75rem center;
            transition: background-color 0.15s;
        }

        .filter-select:hover { background-color: #333; }
        .filter-select:focus { outline: 2px solid var(--color-accent); outline-offset: -2px; }

        .alphabet-nav {
            display: flex;
            align-items: center;
            gap: 2px;
            margin-left: auto;
        }

        .alphabet-nav a {
            color: var(--color-text-secondary);
            text-decoration: none;
            width: 24px;
            height: 24px;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 0.75rem;
            font-weight: 600;
            border-radius: 4px;
            transition: all 0.15s;
        }

        .alphabet-nav a:hover {
            background: #333;
            color: var(--color-text);
        }

        .alphabet-nav a.active {
            background: var(--color-accent);
            color: #000;
        }

        .active-filters {
            display: flex;
            gap: 0.5rem;
            align-items: center;
            margin-left: 0.5rem;
        }

        .filter-tag {
            background: var(--color-accent);
            color: #000;
            font-size: 0.75rem;
            font-weight: 600;
            height: 26px;
            padding: 0 0.5rem 0 0.75rem;
            border-radius: 13px;
            display: flex;
            align-items: center;
            gap: 0.25rem;
        }

        .filter-tag button {
            background: none;
            border: none;
            color: #000;
            cursor: pointer;
            font-size: 1rem;
            line-height: 1;
            opacity: 0.6;
            padding: 0;
        }

        .filter-tag button:hover { opacity: 1; }

        /* Breadcrumb */
        .breadcrumb {
            padding: 1rem 2rem;
            color: var(--color-text-secondary);
            font-size: 0.9rem;
        }

        .breadcrumb a {
            color: var(--color-accent);
            text-decoration: none;
        }

        .breadcrumb a:hover { text-decoration: underline; }

        /* Container */
        .container {
            max-width: 1600px;
            margin: 0 auto;
            padding: 2rem;
        }

        /* Artist Section */
        .artist-section {
            margin-bottom: 3rem;
        }

        .artist-header {
            display: flex;
            align-items: center;
            gap: 1rem;
            margin-bottom: 1.5rem;
            padding-bottom: 0.5rem;
            border-bottom: 1px solid var(--color-border);
            cursor: pointer;
        }

        .artist-header:hover .artist-name {
            color: var(--color-accent);
        }

        .artist-letter {
            font-size: 2rem;
            font-weight: 700;
            color: var(--color-accent);
            min-width: 40px;
        }

        .artist-name {
            font-size: 1.3rem;
            font-weight: 600;
            transition: color 0.2s;
        }

        .album-count {
            color: var(--color-text-secondary);
            font-size: 0.85rem;
            margin-left: auto;
        }

        /* Albums Grid */
        .albums-grid {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
            gap: 1.5rem;
        }

        .album-card {
            background: var(--color-card);
            border-radius: 8px;
            overflow: hidden;
            transition: all 0.3s ease;
            cursor: pointer;
            position: relative;
        }

        .album-card:hover {
            background: var(--color-card-hover);
            transform: translateY(-4px);
            box-shadow: 0 8px 24px rgba(0, 0, 0, 0.4);
        }

        .album-cover {
            aspect-ratio: 1;
            background: #2a2a2a;
            display: flex;
            align-items: center;
            justify-content: center;
            overflow: hidden;
            position: relative;
        }

        .album-cover img {
            width: 100%;
            height: 100%;
            object-fit: cover;
        }

        .album-cover .no-cover {
            font-size: 3rem;
            color: var(--color-text-secondary);
        }

        .play-overlay {
            position: absolute;
            inset: 0;
            background: rgba(0,0,0,0.5);
            display: flex;
            align-items: center;
            justify-content: center;
            opacity: 0;
            transition: opacity 0.2s;
        }

        .album-card:hover .play-overlay {
            opacity: 1;
        }

        .play-btn {
            width: 50px;
            height: 50px;
            background: var(--color-accent);
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 1.5rem;
            color: #000;
            box-shadow: 0 4px 12px rgba(0,0,0,0.4);
            transition: transform 0.2s;
        }

        .play-btn:hover {
            transform: scale(1.1);
        }

        .album-info {
            padding: 1rem;
        }

        .album-title {
            font-weight: 600;
            font-size: 0.9rem;
            margin-bottom: 0.25rem;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
        }

        .album-artist {
            font-size: 0.8rem;
            color: var(--color-text-secondary);
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
            cursor: pointer;
        }

        .album-artist:hover {
            color: var(--color-accent);
            text-decoration: underline;
        }

        .album-year {
            font-size: 0.75rem;
            color: var(--color-text-secondary);
        }

        .album-meta {
            display: flex;
            gap: 0.4rem;
            margin-top: 0.5rem;
            flex-wrap: wrap;
        }

        .album-tag {
            font-size: 0.65rem;
            padding: 0.1rem 0.4rem;
            background: var(--color-bg);
            border-radius: 3px;
            color: var(--color-text-secondary);
        }

        .hidden { display: none !important; }

        .track-match {
            border: 2px solid var(--color-accent);
            box-shadow: 0 0 15px rgba(29, 185, 84, 0.3);
        }

        .track-match::after {
            content: '♪ Track match';
            position: absolute;
            top: 8px;
            right: 8px;
            background: var(--color-accent);
            color: #000;
            font-size: 0.65rem;
            font-weight: 600;
            padding: 0.2rem 0.5rem;
            border-radius: 10px;
            z-index: 10;
        }

        /* Untagged Section */
        .untagged-section {
            margin-top: 4rem;
            padding-top: 2rem;
            border-top: 2px dashed #ff6b6b;
        }

        .untagged-header {
            display: flex;
            align-items: center;
            gap: 1rem;
            margin-bottom: 1.5rem;
            padding: 1rem;
            background: linear-gradient(90deg, rgba(255, 107, 107, 0.1), transparent);
            border-radius: 8px;
            cursor: pointer;
        }

        .untagged-header:hover {
            background: linear-gradient(90deg, rgba(255, 107, 107, 0.15), transparent);
        }

        .untagged-icon {
            font-size: 2rem;
            color: #ff6b6b;
        }

        .untagged-title {
            flex: 1;
        }

        .untagged-title h2 {
            font-size: 1.5rem;
            color: #ff6b6b;
            margin: 0;
        }

        .untagged-title p {
            color: var(--color-text-secondary);
            font-size: 0.85rem;
            margin: 0.25rem 0 0 0;
        }

        .untagged-count {
            background: #ff6b6b;
            color: #000;
            font-weight: 700;
            padding: 0.25rem 0.75rem;
            border-radius: 1rem;
            font-size: 0.9rem;
        }

        .untagged-toggle {
            color: var(--color-text-secondary);
            font-size: 1.5rem;
            transition: transform 0.3s;
        }

        .untagged-header.collapsed .untagged-toggle {
            transform: rotate(-90deg);
        }

        .untagged-content {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
            gap: 1.5rem;
            transition: opacity 0.3s, max-height 0.3s;
        }

        .untagged-content.collapsed {
            display: none;
        }

        .untagged-card {
            background: linear-gradient(135deg, #2a1a1a 0%, #1a1a1a 100%);
            border: 1px solid rgba(255, 107, 107, 0.3);
        }

        .untagged-card:hover {
            border-color: #ff6b6b;
            box-shadow: 0 8px 24px rgba(255, 107, 107, 0.2);
        }

        .untagged-badge {
            position: absolute;
            top: 8px;
            left: 8px;
            background: #ff6b6b;
            color: #000;
            font-size: 0.6rem;
            font-weight: 700;
            padding: 0.15rem 0.4rem;
            border-radius: 4px;
            z-index: 10;
        }

        .tag-btn {
            position: absolute;
            top: 8px;
            right: 8px;
            background: rgba(255, 107, 107, 0.9);
            color: #000;
            border: none;
            padding: 0.3rem 0.6rem;
            border-radius: 4px;
            font-size: 0.7rem;
            font-weight: 600;
            cursor: pointer;
            opacity: 0;
            transition: opacity 0.2s, background 0.2s;
            z-index: 10;
        }

        .album-card:hover .tag-btn {
            opacity: 1;
        }

        .tag-btn:hover {
            background: #ff6b6b;
        }

        /* Tagging Modal */
        .tag-modal-overlay {
            position: fixed;
            inset: 0;
            background: rgba(0, 0, 0, 0.8);
            display: flex;
            align-items: center;
            justify-content: center;
            z-index: 2000;
            opacity: 0;
            visibility: hidden;
            transition: opacity 0.3s, visibility 0.3s;
        }

        .tag-modal-overlay.visible {
            opacity: 1;
            visibility: visible;
        }

        .tag-modal {
            background: #1e1e1e;
            border-radius: 12px;
            max-width: 500px;
            width: 90%;
            padding: 1.5rem;
            box-shadow: 0 20px 60px rgba(0, 0, 0, 0.5);
            transform: scale(0.9);
            transition: transform 0.3s;
        }

        .tag-modal-overlay.visible .tag-modal {
            transform: scale(1);
        }

        .tag-modal-header {
            display: flex;
            align-items: center;
            gap: 1rem;
            margin-bottom: 1rem;
            padding-bottom: 1rem;
            border-bottom: 1px solid var(--color-border);
        }

        .tag-modal-icon {
            font-size: 2rem;
            color: #ff6b6b;
        }

        .tag-modal-header h3 {
            margin: 0;
            color: #ff6b6b;
        }

        .tag-modal-close {
            margin-left: auto;
            background: none;
            border: none;
            color: var(--color-text-secondary);
            font-size: 1.5rem;
            cursor: pointer;
        }

        .tag-modal-close:hover {
            color: var(--color-text);
        }

        .tag-modal-path {
            background: #0d0d0d;
            padding: 0.75rem 1rem;
            border-radius: 6px;
            font-family: monospace;
            font-size: 0.85rem;
            color: var(--color-text-secondary);
            margin-bottom: 1rem;
            word-break: break-all;
        }

        .tag-modal-actions {
            display: flex;
            flex-direction: column;
            gap: 0.5rem;
        }

        .tag-modal-btn {
            display: flex;
            align-items: center;
            gap: 0.75rem;
            padding: 0.75rem 1rem;
            background: #282828;
            border: none;
            border-radius: 6px;
            color: var(--color-text);
            cursor: pointer;
            font-size: 0.9rem;
            transition: background 0.2s;
        }

        .tag-modal-btn:hover {
            background: #333;
        }

        .tag-modal-btn .icon {
            font-size: 1.2rem;
            min-width: 24px;
        }

        .tag-modal-btn.primary {
            background: #ff6b6b;
            color: #000;
        }

        .tag-modal-btn.primary:hover {
            background: #ff8585;
        }

        .tag-modal-footer {
            margin-top: 1rem;
            padding-top: 1rem;
            border-top: 1px solid var(--color-border);
            font-size: 0.8rem;
            color: var(--color-text-secondary);
        }

        .tag-modal-footer a {
            color: var(--color-accent);
            text-decoration: none;
        }

        .tag-modal-footer a:hover {
            text-decoration: underline;
        }

        .copy-success {
            color: var(--color-accent) !important;
        }

        /* Player Bar */
        .player-bar {
            position: fixed;
            bottom: 0;
            left: 0;
            right: 0;
            height: var(--player-height);
            background: linear-gradient(180deg, #282828 0%, #181818 100%);
            border-top: 1px solid var(--color-border);
            display: flex;
            align-items: center;
            padding: 0 1rem;
            z-index: 1000;
        }

        .player-left {
            display: flex;
            align-items: center;
            gap: 1rem;
            width: 30%;
            min-width: 180px;
        }

        .player-cover {
            width: 56px;
            height: 56px;
            background: #333;
            border-radius: 4px;
            overflow: hidden;
            flex-shrink: 0;
        }

        .player-cover img {
            width: 100%;
            height: 100%;
            object-fit: cover;
        }

        .player-info {
            overflow: hidden;
        }

        .player-title {
            font-size: 0.9rem;
            font-weight: 500;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
        }

        .player-artist {
            font-size: 0.75rem;
            color: var(--color-text-secondary);
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
        }

        .player-center {
            flex: 1;
            display: flex;
            flex-direction: column;
            align-items: center;
            gap: 0.5rem;
            max-width: 700px;
            margin: 0 auto;
        }

        .player-controls {
            display: flex;
            align-items: center;
            gap: 1rem;
        }

        .player-controls button {
            background: none;
            border: none;
            color: var(--color-text-secondary);
            cursor: pointer;
            font-size: 1.2rem;
            padding: 0.5rem;
            transition: color 0.2s;
        }

        .player-controls button:hover {
            color: var(--color-text);
        }

        .player-controls .play-pause {
            width: 36px;
            height: 36px;
            background: var(--color-text);
            border-radius: 50%;
            color: #000;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 1rem;
        }

        .player-controls .play-pause:hover {
            transform: scale(1.05);
        }

        .player-progress {
            width: 100%;
            display: flex;
            align-items: center;
            gap: 0.5rem;
        }

        .player-time {
            font-size: 0.7rem;
            color: var(--color-text-secondary);
            min-width: 40px;
        }

        .progress-bar {
            flex: 1;
            height: 4px;
            background: #4d4d4d;
            border-radius: 2px;
            cursor: pointer;
            position: relative;
        }

        .progress-bar:hover {
            height: 6px;
        }

        .progress-fill {
            height: 100%;
            background: var(--color-accent);
            border-radius: 2px;
            width: 0%;
            transition: width 0.1s linear;
        }

        .player-right {
            width: 30%;
            display: flex;
            justify-content: flex-end;
            align-items: center;
            gap: 1rem;
        }

        .volume-control {
            display: flex;
            align-items: center;
            gap: 0.5rem;
        }

        .volume-slider {
            width: 100px;
            height: 4px;
            background: #4d4d4d;
            border-radius: 2px;
            cursor: pointer;
            position: relative;
        }

        .volume-fill {
            height: 100%;
            background: var(--color-text);
            border-radius: 2px;
            width: 100%;
        }

        .open-folder {
            color: var(--color-text-secondary);
            text-decoration: none;
            font-size: 0.8rem;
            padding: 0.5rem;
        }

        .open-folder:hover {
            color: var(--color-accent);
        }

        .player-empty {
            color: var(--color-text-secondary);
            font-size: 0.9rem;
            text-align: center;
            width: 100%;
        }

        .resume-banner {
            position: fixed;
            top: 0;
            left: 0;
            right: 0;
            background: linear-gradient(90deg, var(--color-accent), #17a34a);
            color: #000;
            padding: 0.75rem 2rem;
            display: flex;
            justify-content: center;
            align-items: center;
            gap: 1rem;
            z-index: 1001;
            font-weight: 600;
            cursor: pointer;
            transition: transform 0.3s;
        }

        .resume-banner:hover {
            filter: brightness(1.1);
        }

        .resume-banner .resume-icon {
            font-size: 1.2rem;
        }

        .resume-banner .resume-close {
            position: absolute;
            right: 1rem;
            background: none;
            border: none;
            color: #000;
            font-size: 1.2rem;
            cursor: pointer;
            opacity: 0.7;
        }

        .resume-banner .resume-close:hover {
            opacity: 1;
        }

        /* Responsive */
        @media (max-width: 768px) {
            .header .stats { gap: 1rem; }
            .nav-row { flex-direction: column; }
            .search-box { width: 100%; }
            .albums-grid {
                grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
                gap: 1rem;
            }
            .player-left { width: auto; }
            .player-right { display: none; }
            .player-info { max-width: 120px; }
        }
    </style>
</head>
<body>

<header class="header">
    <div class="header-bg" id="headerBg"></div>
    <div class="header-overlay"></div>
    <div class="header-content">
        <h1>Music Catalog</h1>
        <p class="subtitle">Your personal music collection</p>
        <div class="stats">
            <div class="stat-item">
                <div class="stat-value" id="statsArtists">${catalogArtistCount?c}</div>
                <div class="stat-label">Artists</div>
            </div>
            <div class="stat-item">
                <div class="stat-value" id="statsAlbums">${catalogAlbumCount?c}</div>
                <div class="stat-label">Albums</div>
            </div>
            <div class="stat-item">
                <div class="stat-value" id="statsTracks">${audioFiles?c}</div>
                <div class="stat-label">Tracks</div>
            </div>
        </div>
    </div>
</header>

<nav class="nav">
    <div class="nav-content">
        <a href="report.html" class="back-link">&#9664; Report</a>
        <div class="search-box">
            <span class="search-icon">&#128269;</span>
            <input type="text" id="searchInput" placeholder="Search...">
            <button class="search-clear" id="searchClear" onclick="clearSearch()">&#10005;</button>
        </div>
        <div class="nav-divider"></div>
        <select class="filter-select" id="filterFormat">
            <option value="">Format</option>
        </select>
        <select class="filter-select" id="filterYear">
            <option value="">Year</option>
        </select>
        <div class="active-filters" id="activeFilters"></div>
        <div class="alphabet-nav" id="alphabetNav"></div>
    </div>
</nav>

<div class="breadcrumb" id="breadcrumb">
    <span>All Artists</span>
</div>

<div class="container" id="mainContent">
    <#list catalogByArtist as artist, albums>
    <div class="artist-section"
         data-artist="${artist?js_string}"
         data-artist-lower="${artist?lower_case?js_string}"
         data-first-letter="${artist?substring(0, 1)?upper_case}">
        <div class="artist-header" onclick="showArtist('${artist?js_string}')">
            <span class="artist-letter">${artist?substring(0, 1)?upper_case}</span>
            <span class="artist-name">${artist}</span>
            <span class="album-count">${albums?size} album<#if albums?size != 1>s</#if></span>
        </div>
        <div class="albums-grid">
            <#list albums as album>
            <div class="album-card"
                 data-artist="${artist?js_string}"
                 data-album="${(album.album!'')?js_string}"
                 data-album-lower="${(album.album!'')?lower_case?js_string}"
                 data-year="${album.year!'Unknown'}"
                 data-format="${album.format!''}"
                 data-path="${album.path?js_string}"
                 data-cover="${(album.coverImagePath!'')?js_string}"
                 data-audio="${(album.firstAudioFilePath!'')?js_string}"
                 data-tracks="<#if album.audioFilePaths??><#list album.audioFilePaths as track>${track?js_string}<#if track_has_next>|</#if></#list></#if>"
                 data-track-names="<#if album.audioFilePaths??><#list album.audioFilePaths as track>${track?replace('\\', '/')?keep_after_last("/")?keep_before_last(".")?lower_case?js_string}<#if track_has_next>|</#if></#list></#if>"
                 data-track-count="${album.filesCount?c}"
                 onclick="playAlbum(this)">
                <div class="album-cover">
                    <#if album.coverImagePath?? && album.coverImagePath != "">
                    <img src="file:///${album.coverImagePath?replace('\\', '/')}" alt="" onerror="this.parentElement.innerHTML='<span class=\'no-cover\'>&#127925;</span>'">
                    <#else>
                    <span class="no-cover">&#127925;</span>
                    </#if>
                    <div class="play-overlay">
                        <div class="play-btn">&#9654;</div>
                    </div>
                </div>
                <div class="album-info">
                    <div class="album-title" title="${album.album!''}">${album.album!''}</div>
                    <div class="album-artist" onclick="event.stopPropagation(); showArtist('${artist?js_string}')">${artist}</div>
                    <div class="album-year">${album.year!'Unknown'}</div>
                    <div class="album-meta">
                        <#if album.format??><span class="album-tag">${album.format}</span></#if>
                        <#if album.bitrate??><span class="album-tag">${album.bitrate?c} kbps</span></#if>
                        <span class="album-tag">${album.filesCount?c} tracks</span>
                    </div>
                </div>
            </div>
            </#list>
        </div>
    </div>
    </#list>

    <#-- Untagged Albums Section -->
    <#if untaggedAlbums?? && (untaggedAlbums?size > 0)>
    <div class="untagged-section" id="untaggedSection">
        <div class="untagged-header" onclick="toggleUntagged()">
            <span class="untagged-icon">&#9888;</span>
            <div class="untagged-title">
                <h2>Albums Without Tags</h2>
                <p>These albums have missing ID3 tags and need attention</p>
            </div>
            <span class="untagged-count">${untaggedAlbumsCount?c}</span>
            <span class="untagged-toggle" id="untaggedToggle">&#9660;</span>
        </div>
        <div class="untagged-content" id="untaggedContent">
            <#list untaggedAlbums as album>
            <div class="album-card untagged-card"
                 data-artist="${(album.artist!'')?js_string}"
                 data-album="${(album.album!'')?js_string}"
                 data-album-lower="${(album.album!'')?lower_case?js_string}"
                 data-year="${album.year!'Unknown'}"
                 data-format="${album.format!''}"
                 data-path="${album.path?js_string}"
                 data-cover="${(album.coverImagePath!'')?js_string}"
                 data-audio="${(album.firstAudioFilePath!'')?js_string}"
                 data-tracks="<#if album.audioFilePaths??><#list album.audioFilePaths as track>${track?js_string}<#if track_has_next>|</#if></#list></#if>"
                 data-track-names="<#if album.audioFilePaths??><#list album.audioFilePaths as track>${track?replace('\\', '/')?keep_after_last("/")?keep_before_last(".")?lower_case?js_string}<#if track_has_next>|</#if></#list></#if>"
                 data-track-count="${album.filesCount?c}"
                 onclick="playAlbum(this)">
                <span class="untagged-badge">NO TAGS</span>
                <button class="tag-btn" onclick="event.stopPropagation(); openForTagging('${album.path?js_string}')">&#9998; Tag</button>
                <div class="album-cover">
                    <#if album.coverImagePath?? && album.coverImagePath != "">
                    <img src="file:///${album.coverImagePath?replace('\\', '/')}" alt="" onerror="this.parentElement.innerHTML='<span class=\'no-cover\'>&#127925;</span>'">
                    <#else>
                    <span class="no-cover">&#127925;</span>
                    </#if>
                    <div class="play-overlay">
                        <div class="play-btn">&#9654;</div>
                    </div>
                </div>
                <div class="album-info">
                    <div class="album-title" title="${album.path?keep_after_last('\\')}">${album.path?keep_after_last('\\')}</div>
                    <div class="album-artist" style="color: #ff6b6b;">Unknown Artist</div>
                    <div class="album-year">${album.year!'Unknown'}</div>
                    <div class="album-meta">
                        <#if album.format??><span class="album-tag">${album.format}</span></#if>
                        <#if album.bitrate??><span class="album-tag">${album.bitrate?c} kbps</span></#if>
                        <span class="album-tag">${album.filesCount?c} tracks</span>
                    </div>
                </div>
            </div>
            </#list>
        </div>
    </div>
    </#if>
</div>

<!-- Tagging Modal -->
<div class="tag-modal-overlay" id="tagModal" onclick="closeTagModal(event)">
    <div class="tag-modal" onclick="event.stopPropagation()">
        <div class="tag-modal-header">
            <span class="tag-modal-icon">&#9998;</span>
            <h3>Tag This Album</h3>
            <button class="tag-modal-close" onclick="closeTagModal()">&times;</button>
        </div>
        <div class="tag-modal-path" id="tagModalPath"></div>
        <div class="tag-modal-actions">
            <button class="tag-modal-btn primary" onclick="copyPathToClipboard()">
                <span class="icon">&#128203;</span>
                <span id="copyBtnText">Copy path to clipboard</span>
            </button>
            <button class="tag-modal-btn" onclick="openFolderFromModal()">
                <span class="icon">&#128193;</span>
                <span>Open folder in Explorer</span>
            </button>
        </div>
        <div class="tag-modal-footer">
            <strong>Recommended tagging tools:</strong><br>
            <a href="https://www.mp3tag.de/" target="_blank">Mp3tag</a> (Windows) |
            <a href="https://picard.musicbrainz.org/" target="_blank">MusicBrainz Picard</a> (Cross-platform) |
            <a href="https://www.foobar2000.org/" target="_blank">foobar2000</a> (Windows)
        </div>
    </div>
</div>

<!-- Audio Player Bar -->
<div class="player-bar" id="playerBar">
    <div class="player-empty" id="playerEmpty">Select an album to start playing</div>

    <div class="player-left hidden" id="playerContent">
        <div class="player-cover" id="playerCover"></div>
        <div class="player-info">
            <div class="player-title" id="playerTitle">-</div>
            <div class="player-artist" id="playerArtist">-</div>
        </div>
    </div>

    <div class="player-center hidden" id="playerControls">
        <div class="player-controls">
            <button onclick="prevTrack()" title="Previous">&#9198;</button>
            <button class="play-pause" id="playPauseBtn" onclick="togglePlay()">&#9654;</button>
            <button onclick="nextTrack()" title="Next">&#9197;</button>
        </div>
        <div class="player-progress">
            <span class="player-time" id="currentTime">0:00</span>
            <div class="progress-bar" id="progressBar" onclick="seek(event)">
                <div class="progress-fill" id="progressFill"></div>
            </div>
            <span class="player-time" id="duration">0:00</span>
        </div>
    </div>

    <div class="player-right hidden" id="playerRight">
        <div class="volume-control">
            <span>&#128266;</span>
            <div class="volume-slider" onclick="setVolume(event)">
                <div class="volume-fill" id="volumeFill"></div>
            </div>
        </div>
        <a href="#" class="open-folder" id="openFolder" title="Open folder">&#128193;</a>
    </div>
</div>

<audio id="audioPlayer"></audio>

<script>
// State
let currentView = 'all'; // 'all' or 'artist'
let currentArtist = null;
let currentTracks = [];
let currentTrackIndex = 0;
let currentAlbumData = null;

const audio = document.getElementById('audioPlayer');

// Initialize
document.addEventListener('DOMContentLoaded', function() {
    buildHeaderMosaic();
    buildFilters();
    buildAlphabetNav();
    setupSearch();
    setupAudioPlayer();
    restorePlaybackState();
});

// Build header background mosaic from album covers
function buildHeaderMosaic() {
    const covers = [];
    document.querySelectorAll('.album-card').forEach(card => {
        if (card.dataset.cover) {
            covers.push(card.dataset.cover);
        }
    });

    if (covers.length === 0) return;

    // Shuffle and take enough covers to fill the header
    const shuffled = covers.sort(() => Math.random() - 0.5);
    const headerBg = document.getElementById('headerBg');
    const needed = Math.ceil((window.innerWidth / 80) * 4); // ~4 rows

    for (let i = 0; i < needed && i < shuffled.length * 3; i++) {
        const img = document.createElement('img');
        img.className = 'header-bg-img';
        img.src = 'file:///' + shuffled[i % shuffled.length].replace(/\\/g, '/');
        img.onerror = () => img.style.display = 'none';
        headerBg.appendChild(img);
    }
}

// Build filter dropdowns
function buildFilters() {
    const formats = new Set();
    const years = new Set();

    document.querySelectorAll('.album-card').forEach(card => {
        if (card.dataset.format) formats.add(card.dataset.format);
        if (card.dataset.year && card.dataset.year !== 'Unknown') years.add(card.dataset.year);
    });

    const formatSelect = document.getElementById('filterFormat');
    Array.from(formats).sort().forEach(f => {
        formatSelect.innerHTML += '<option value="' + f + '">' + f + '</option>';
    });

    const yearSelect = document.getElementById('filterYear');
    Array.from(years).sort().reverse().forEach(y => {
        yearSelect.innerHTML += '<option value="' + y + '">' + y + '</option>';
    });

    formatSelect.onchange = applyFilters;
    yearSelect.onchange = applyFilters;
}

// Build alphabet navigation
function buildAlphabetNav() {
    const letters = new Set();
    document.querySelectorAll('.artist-section').forEach(s => {
        letters.add(s.dataset.firstLetter);
    });

    const nav = document.getElementById('alphabetNav');
    Array.from(letters).sort().forEach(letter => {
        const a = document.createElement('a');
        a.href = '#';
        a.textContent = letter;
        a.onclick = (e) => { e.preventDefault(); scrollToLetter(letter); };
        nav.appendChild(a);
    });
}

function scrollToLetter(letter) {
    const section = document.querySelector('.artist-section[data-first-letter="' + letter + '"]:not(.hidden)');
    if (section) {
        section.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }
}

// Search
function setupSearch() {
    const input = document.getElementById('searchInput');
    input.oninput = () => {
        updateSearchUI();
        applyFilters();
    };
    input.onfocus = () => document.getElementById('searchHint').classList.add('visible');
    input.onblur = () => setTimeout(() => document.getElementById('searchHint').classList.remove('visible'), 200);
}

function updateSearchUI() {
    const query = document.getElementById('searchInput').value;
    document.getElementById('searchClear').classList.toggle('visible', query.length > 0);
}

function clearSearch() {
    document.getElementById('searchInput').value = '';
    updateSearchUI();
    applyFilters();
}

function clearFilter(type) {
    if (type === 'format') document.getElementById('filterFormat').value = '';
    if (type === 'year') document.getElementById('filterYear').value = '';
    applyFilters();
}

function updateActiveFilters() {
    const format = document.getElementById('filterFormat').value;
    const year = document.getElementById('filterYear').value;
    const container = document.getElementById('activeFilters');
    container.innerHTML = '';

    if (format) {
        container.innerHTML += '<span class="filter-tag">' + format + ' <button onclick="clearFilter(\'format\')">&times;</button></span>';
    }
    if (year) {
        container.innerHTML += '<span class="filter-tag">' + year + ' <button onclick="clearFilter(\'year\')">&times;</button></span>';
    }
}

// Apply all filters
function applyFilters() {
    const query = document.getElementById('searchInput').value.toLowerCase().trim();
    const format = document.getElementById('filterFormat').value;
    const year = document.getElementById('filterYear').value;

    updateActiveFilters();

    let visibleAlbums = 0;
    let visibleArtists = 0;
    let visibleTracks = 0;
    let matchedTracks = 0;

    document.querySelectorAll('.artist-section').forEach(section => {
        if (currentView === 'artist' && section.dataset.artist !== currentArtist) {
            section.classList.add('hidden');
            return;
        }

        const artistName = section.dataset.artistLower || '';
        const albums = section.querySelectorAll('.album-card');
        let hasVisibleAlbum = false;

        albums.forEach(album => {
            const albumName = album.dataset.albumLower || '';
            const albumFormat = album.dataset.format || '';
            const albumYear = album.dataset.year || '';
            const trackNames = album.dataset.trackNames || '';

            // Search in artist, album, and track names
            const matchesArtist = artistName.includes(query);
            const matchesAlbum = albumName.includes(query);
            const matchesTrack = trackNames.includes(query);
            const matchesSearch = !query || matchesArtist || matchesAlbum || matchesTrack;

            const matchesFormat = !format || albumFormat === format;
            const matchesYear = !year || albumYear === year;

            const visible = matchesSearch && matchesFormat && matchesYear;
            album.classList.toggle('hidden', !visible);

            // Highlight if matched by track
            album.classList.toggle('track-match', visible && matchesTrack && !matchesArtist && !matchesAlbum);

            if (visible) {
                hasVisibleAlbum = true;
                visibleAlbums++;
                visibleTracks += parseInt(album.dataset.trackCount || 0);
                if (matchesTrack && query) matchedTracks++;
            }
        });

        section.classList.toggle('hidden', !hasVisibleAlbum);
        if (hasVisibleAlbum) visibleArtists++;
    });

    // Update stats
    document.getElementById('statsArtists').textContent = visibleArtists;
    document.getElementById('statsAlbums').textContent = visibleAlbums;
    document.getElementById('statsTracks').textContent = visibleTracks;
}

// Show single artist view
function showArtist(artistName) {
    currentView = 'artist';
    currentArtist = artistName;

    document.getElementById('breadcrumb').innerHTML =
        '<a href="#" onclick="showAllArtists(); return false;">All Artists</a> &rsaquo; ' + artistName;

    applyFilters();
    window.scrollTo({ top: 0, behavior: 'smooth' });
}

// Show all artists
function showAllArtists() {
    currentView = 'all';
    currentArtist = null;

    document.getElementById('breadcrumb').innerHTML = '<span>All Artists</span>';

    applyFilters();
}

// Audio Player
function setupAudioPlayer() {
    audio.onloadedmetadata = () => {
        document.getElementById('duration').textContent = formatTime(audio.duration);
    };

    audio.ontimeupdate = () => {
        const progress = (audio.currentTime / audio.duration) * 100 || 0;
        document.getElementById('progressFill').style.width = progress + '%';
        document.getElementById('currentTime').textContent = formatTime(audio.currentTime);
        // Save state periodically (every 5 seconds)
        if (Math.floor(audio.currentTime) % 5 === 0) {
            savePlaybackState();
        }
    };

    audio.onended = () => {
        nextTrack();
    };

    audio.onplay = () => {
        document.getElementById('playPauseBtn').innerHTML = '&#10074;&#10074;';
        savePlaybackState();
    };

    audio.onpause = () => {
        document.getElementById('playPauseBtn').innerHTML = '&#9654;';
        savePlaybackState();
    };

    // Restore volume from storage
    const savedVolume = localStorage.getItem('catalogVolume');
    if (savedVolume !== null) {
        audio.volume = parseFloat(savedVolume);
        document.getElementById('volumeFill').style.width = (audio.volume * 100) + '%';
    } else {
        audio.volume = 1;
    }
}

// Save playback state to localStorage
function savePlaybackState() {
    if (!currentAlbumData || currentTracks.length === 0) return;

    const state = {
        albumData: currentAlbumData,
        tracks: currentTracks,
        trackIndex: currentTrackIndex,
        currentTime: audio.currentTime,
        volume: audio.volume,
        paused: audio.paused
    };
    localStorage.setItem('catalogPlaybackState', JSON.stringify(state));
    localStorage.setItem('catalogVolume', audio.volume);
}

// Restore playback state from localStorage
function restorePlaybackState() {
    const stateJson = localStorage.getItem('catalogPlaybackState');
    if (!stateJson) return;

    try {
        const state = JSON.parse(stateJson);
        if (!state.albumData || !state.tracks || state.tracks.length === 0) return;

        currentAlbumData = state.albumData;
        currentTracks = state.tracks;
        currentTrackIndex = state.trackIndex || 0;

        showPlayer();

        // Set the track
        const trackPath = currentTracks[currentTrackIndex];
        const trackName = trackPath.split(/[/\\]/).pop().replace(/\.[^.]+$/, '');
        document.getElementById('playerTitle').textContent = trackName;

        audio.src = 'file:///' + trackPath.replace(/\\/g, '/');

        // Restore position after metadata loads
        const savedTime = state.currentTime || 0;
        const wasPlaying = !state.paused;

        audio.addEventListener('loadedmetadata', function onRestore() {
            audio.removeEventListener('loadedmetadata', onRestore);
            document.getElementById('duration').textContent = formatTime(audio.duration);

            if (savedTime && savedTime < audio.duration) {
                audio.currentTime = savedTime;
            }

            // Show resume banner if was playing
            if (wasPlaying) {
                showResumeBanner(trackName, formatTime(savedTime));
            }
        });

    } catch (e) {
        console.log('Failed to restore playback state:', e);
    }
}

// Show a banner to resume playback
function showResumeBanner(trackName, position) {
    // Remove existing banner if any
    const existing = document.getElementById('resumeBanner');
    if (existing) existing.remove();

    const banner = document.createElement('div');
    banner.id = 'resumeBanner';
    banner.className = 'resume-banner';
    banner.innerHTML = '<span class="resume-icon">&#9654;</span>' +
        '<span>Resume: <strong>' + trackName + '</strong> at ' + position + '</span>' +
        '<button class="resume-close" onclick="event.stopPropagation(); closeResumeBanner();">&times;</button>';
    banner.onclick = resumePlayback;
    document.body.prepend(banner);
}

function resumePlayback() {
    closeResumeBanner();
    audio.play().catch(e => console.log('Playback error:', e));
}

function closeResumeBanner() {
    const banner = document.getElementById('resumeBanner');
    if (banner) banner.remove();
}

function playAlbum(cardElement) {
    const tracksStr = cardElement.dataset.tracks || '';
    currentTracks = tracksStr ? tracksStr.split('|') : [];
    currentTrackIndex = 0;
    currentAlbumData = {
        artist: cardElement.dataset.artist,
        album: cardElement.dataset.album,
        cover: cardElement.dataset.cover,
        path: cardElement.dataset.path
    };

    if (currentTracks.length > 0) {
        showPlayer();
        playTrack(0);
    }
}

function showPlayer() {
    document.getElementById('playerEmpty').classList.add('hidden');
    document.getElementById('playerContent').classList.remove('hidden');
    document.getElementById('playerControls').classList.remove('hidden');
    document.getElementById('playerRight').classList.remove('hidden');

    // Update album info
    document.getElementById('playerArtist').textContent = currentAlbumData.artist;

    // Update cover
    const coverEl = document.getElementById('playerCover');
    if (currentAlbumData.cover) {
        coverEl.innerHTML = '<img src="file:///' + currentAlbumData.cover.replace(/\\/g, '/') + '" onerror="this.parentElement.innerHTML=\'&#127925;\'">';
    } else {
        coverEl.innerHTML = '&#127925;';
    }

    // Update open folder link
    document.getElementById('openFolder').href = 'file:///' + currentAlbumData.path.replace(/\\/g, '/');
}

function playTrack(index) {
    if (index < 0 || index >= currentTracks.length) return;

    currentTrackIndex = index;
    const trackPath = currentTracks[index];
    const trackName = trackPath.split(/[/\\]/).pop().replace(/\.[^.]+$/, '');

    document.getElementById('playerTitle').textContent = trackName;

    // Set new source and ensure we start from beginning
    audio.src = 'file:///' + trackPath.replace(/\\/g, '/');
    audio.currentTime = 0;

    // Play when ready
    audio.addEventListener('canplay', function onCanPlay() {
        audio.removeEventListener('canplay', onCanPlay);
        audio.play().catch(e => console.log('Playback error:', e));
    });
}

function togglePlay() {
    if (audio.paused) {
        audio.play().catch(e => {});
    } else {
        audio.pause();
    }
}

function nextTrack() {
    if (currentTrackIndex < currentTracks.length - 1) {
        playTrack(currentTrackIndex + 1);
    }
}

function prevTrack() {
    if (audio.currentTime > 3) {
        audio.currentTime = 0;
    } else if (currentTrackIndex > 0) {
        playTrack(currentTrackIndex - 1);
    }
}

function seek(event) {
    const bar = document.getElementById('progressBar');
    const percent = event.offsetX / bar.offsetWidth;
    audio.currentTime = percent * audio.duration;
}

function setVolume(event) {
    const bar = event.currentTarget;
    const percent = event.offsetX / bar.offsetWidth;
    audio.volume = Math.max(0, Math.min(1, percent));
    document.getElementById('volumeFill').style.width = (percent * 100) + '%';
}

function formatTime(seconds) {
    if (isNaN(seconds)) return '0:00';
    const m = Math.floor(seconds / 60);
    const s = Math.floor(seconds % 60);
    return m + ':' + (s < 10 ? '0' : '') + s;
}

// Untagged section toggle
function toggleUntagged() {
    const header = document.querySelector('.untagged-header');
    const content = document.getElementById('untaggedContent');
    header.classList.toggle('collapsed');
    content.classList.toggle('collapsed');
}

// Current path being tagged
let currentTagPath = '';

// Open tagging modal
function openForTagging(path) {
    currentTagPath = path;
    document.getElementById('tagModalPath').textContent = path;
    document.getElementById('tagModal').classList.add('visible');
    document.getElementById('copyBtnText').textContent = 'Copy path to clipboard';
}

// Close tagging modal
function closeTagModal(event) {
    if (event && event.target !== document.getElementById('tagModal')) return;
    document.getElementById('tagModal').classList.remove('visible');
}

// Escape key to close modal
document.addEventListener('keydown', function(e) {
    if (e.key === 'Escape') {
        closeTagModal();
    }
});

// Copy path to clipboard
function copyPathToClipboard() {
    navigator.clipboard.writeText(currentTagPath).then(() => {
        const btn = document.getElementById('copyBtnText');
        btn.textContent = 'Copied!';
        btn.classList.add('copy-success');
        setTimeout(() => {
            btn.textContent = 'Copy path to clipboard';
            btn.classList.remove('copy-success');
        }, 2000);
    }).catch(err => {
        // Fallback for older browsers
        const textarea = document.createElement('textarea');
        textarea.value = currentTagPath;
        document.body.appendChild(textarea);
        textarea.select();
        document.execCommand('copy');
        document.body.removeChild(textarea);

        const btn = document.getElementById('copyBtnText');
        btn.textContent = 'Copied!';
        btn.classList.add('copy-success');
        setTimeout(() => {
            btn.textContent = 'Copy path to clipboard';
            btn.classList.remove('copy-success');
        }, 2000);
    });
}

// Open folder from modal
function openFolderFromModal() {
    window.open('file:///' + currentTagPath.replace(/\\/g, '/'), '_blank');
}
</script>

</body>
</html>
