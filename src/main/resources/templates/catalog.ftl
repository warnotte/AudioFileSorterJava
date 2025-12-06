<#-- AudioFilesSorter Music Catalog -->
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
        }

        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
            background-color: var(--color-bg);
            color: var(--color-text);
            line-height: 1.6;
        }

        .header {
            background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
            padding: 3rem 2rem;
            text-align: center;
            border-bottom: 1px solid var(--color-border);
        }

        .header h1 {
            font-size: 2.5rem;
            font-weight: 700;
            margin-bottom: 0.5rem;
            background: linear-gradient(90deg, #fff, #1db954);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            background-clip: text;
        }

        .header .subtitle {
            color: var(--color-text-secondary);
            font-size: 1rem;
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
        }

        .header .stat-label {
            font-size: 0.85rem;
            color: var(--color-text-secondary);
            text-transform: uppercase;
            letter-spacing: 0.1em;
        }

        .nav {
            background: var(--color-card);
            padding: 1rem 2rem;
            border-bottom: 1px solid var(--color-border);
            position: sticky;
            top: 0;
            z-index: 100;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }

        .nav a {
            color: var(--color-text-secondary);
            text-decoration: none;
            padding: 0.5rem 1rem;
            border-radius: 20px;
            transition: all 0.2s;
        }

        .nav a:hover {
            color: var(--color-text);
            background: var(--color-card-hover);
        }

        .search-box {
            display: flex;
            align-items: center;
            background: var(--color-bg);
            border-radius: 20px;
            padding: 0.5rem 1rem;
            width: 300px;
        }

        .search-box input {
            background: transparent;
            border: none;
            color: var(--color-text);
            font-size: 0.9rem;
            width: 100%;
            outline: none;
        }

        .search-box input::placeholder {
            color: var(--color-text-secondary);
        }

        .alphabet-nav {
            display: flex;
            gap: 0.25rem;
            flex-wrap: wrap;
        }

        .alphabet-nav a {
            padding: 0.25rem 0.5rem;
            font-size: 0.85rem;
            min-width: 28px;
            text-align: center;
        }

        .container {
            max-width: 1600px;
            margin: 0 auto;
            padding: 2rem;
        }

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
        }

        .artist-letter {
            font-size: 2.5rem;
            font-weight: 700;
            color: var(--color-accent);
            min-width: 50px;
        }

        .artist-name {
            font-size: 1.5rem;
            font-weight: 600;
            color: var(--color-text);
        }

        .album-count {
            color: var(--color-text-secondary);
            font-size: 0.9rem;
            margin-left: auto;
        }

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
            text-decoration: none;
            color: inherit;
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

        .album-info {
            padding: 1rem;
        }

        .album-title {
            font-weight: 600;
            font-size: 0.95rem;
            margin-bottom: 0.25rem;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
        }

        .album-year {
            font-size: 0.8rem;
            color: var(--color-text-secondary);
        }

        .album-meta {
            display: flex;
            gap: 0.5rem;
            margin-top: 0.5rem;
            flex-wrap: wrap;
        }

        .album-tag {
            font-size: 0.7rem;
            padding: 0.15rem 0.4rem;
            background: var(--color-bg);
            border-radius: 3px;
            color: var(--color-text-secondary);
        }

        .footer {
            text-align: center;
            padding: 2rem;
            color: var(--color-text-secondary);
            font-size: 0.85rem;
            border-top: 1px solid var(--color-border);
        }

        .footer a {
            color: var(--color-accent);
            text-decoration: none;
        }

        .hidden {
            display: none !important;
        }

        /* Letter divider for navigation */
        .letter-divider {
            font-size: 1.5rem;
            font-weight: 700;
            color: var(--color-accent);
            padding: 1rem 0;
            margin-top: 2rem;
            border-bottom: 2px solid var(--color-accent);
        }

        @media (max-width: 768px) {
            .header .stats {
                gap: 1.5rem;
            }
            .nav {
                flex-direction: column;
                gap: 1rem;
            }
            .search-box {
                width: 100%;
            }
            .albums-grid {
                grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
                gap: 1rem;
            }
        }
    </style>
</head>
<body>

<header class="header">
    <h1>Music Catalog</h1>
    <p class="subtitle">Generated by AudioFilesSorter on ${generatedAt}</p>
    <div class="stats">
        <div class="stat-item">
            <div class="stat-value">${catalogArtistCount?c}</div>
            <div class="stat-label">Artists</div>
        </div>
        <div class="stat-item">
            <div class="stat-value">${catalogAlbumCount?c}</div>
            <div class="stat-label">Albums</div>
        </div>
    </div>
</header>

<nav class="nav">
    <a href="report.html">&larr; Back to Report</a>
    <div class="search-box">
        <input type="text" id="searchInput" placeholder="Search artists or albums..." onkeyup="filterCatalog()">
    </div>
    <div class="alphabet-nav" id="alphabetNav">
        <!-- Will be populated by JavaScript -->
    </div>
</nav>

<div class="container">
    <#list catalogByArtist as artist, albums>
    <div class="artist-section" data-artist="${artist?lower_case}" data-first-letter="${artist?substring(0, 1)?upper_case}">
        <div class="artist-header" id="artist-${artist?replace('[^a-zA-Z0-9]', '', 'r')}">
            <span class="artist-letter">${artist?substring(0, 1)?upper_case}</span>
            <span class="artist-name">${artist}</span>
            <span class="album-count">${albums?size} album<#if albums?size != 1>s</#if></span>
        </div>
        <div class="albums-grid">
            <#list albums as album>
            <a class="album-card" href="file:///${album.path?replace('\\', '/')}" target="_blank" data-album="${(album.album!'')?lower_case}">
                <div class="album-cover">
                    <#if album.coverImagePath?? && album.coverImagePath != "">
                    <img src="file:///${album.coverImagePath?replace('\\', '/')}" alt="${album.album!''}" onerror="this.parentElement.innerHTML='<span class=\\'no-cover\\'>&#127925;</span>'">
                    <#else>
                    <span class="no-cover">&#127925;</span>
                    </#if>
                </div>
                <div class="album-info">
                    <div class="album-title" title="${album.album!''}">${album.album!''}</div>
                    <div class="album-year">${album.year!'Unknown'}</div>
                    <div class="album-meta">
                        <#if album.format??>
                        <span class="album-tag">${album.format}</span>
                        </#if>
                        <#if album.bitrate??>
                        <span class="album-tag">${album.bitrate?c} kbps</span>
                        </#if>
                        <span class="album-tag">${album.filesCount?c} files</span>
                    </div>
                </div>
            </a>
            </#list>
        </div>
    </div>
    </#list>
</div>

<footer class="footer">
    <p>Generated by AudioFilesSorter &bull; <a href="report.html">View Full Report</a></p>
</footer>

<script>
// Build alphabet navigation
(function() {
    const sections = document.querySelectorAll('.artist-section');
    const letters = new Set();

    sections.forEach(section => {
        const letter = section.dataset.firstLetter;
        if (letter) letters.add(letter);
    });

    const sortedLetters = Array.from(letters).sort();
    const nav = document.getElementById('alphabetNav');

    sortedLetters.forEach(letter => {
        const link = document.createElement('a');
        link.href = '#';
        link.textContent = letter;
        link.onclick = (e) => {
            e.preventDefault();
            scrollToLetter(letter);
        };
        nav.appendChild(link);
    });
})();

function scrollToLetter(letter) {
    const sections = document.querySelectorAll('.artist-section');
    for (const section of sections) {
        if (section.dataset.firstLetter === letter) {
            section.scrollIntoView({ behavior: 'smooth', block: 'start' });
            break;
        }
    }
}

function filterCatalog() {
    const query = document.getElementById('searchInput').value.toLowerCase().trim();
    const sections = document.querySelectorAll('.artist-section');

    sections.forEach(section => {
        const artistName = section.dataset.artist || '';
        const albums = section.querySelectorAll('.album-card');
        let hasVisibleAlbum = false;

        albums.forEach(album => {
            const albumName = album.dataset.album || '';
            const matches = artistName.includes(query) || albumName.includes(query);
            album.classList.toggle('hidden', !matches && query !== '');
            if (matches || query === '') hasVisibleAlbum = true;
        });

        // Show section if any album matches or artist matches
        const artistMatches = artistName.includes(query);
        section.classList.toggle('hidden', !hasVisibleAlbum && !artistMatches);

        // If artist matches, show all albums
        if (artistMatches) {
            albums.forEach(album => album.classList.remove('hidden'));
        }
    });
}
</script>

</body>
</html>
