package it.unibo.oop.lab.streams;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.stream.Stream;

/**
 *
 */
public final class MusicGroupImpl implements MusicGroup {

    private final Map<String, Integer> albums = new HashMap<>();
    private final Set<Song> songs = new HashSet<>();

    @Override
    public void addAlbum(final String albumName, final int year) {
        this.albums.put(albumName, year);
    }

    @Override
    public void addSong(final String songName, final Optional<String> albumName, final double duration) {
        if (albumName.isPresent() && !this.albums.containsKey(albumName.get())) {
            throw new IllegalArgumentException("invalid album name");
        }
        this.songs.add(new MusicGroupImpl.Song(songName, albumName, duration));
    }

    @Override
    public Stream<String> orderedSongNames() {
        return this.songs.stream()
                .map(s -> s.getSongName())
                .sorted();
    }

    @Override
    public Stream<String> albumNames() {
        return this.albums.keySet().stream();
    }

    @Override
    public Stream<String> albumInYear(final int year) {
        return this.albumNames()
                .filter(name -> albums.get(name) == year);
    }

    @Override
    public int countSongs(final String albumName) {
        return (int) this.songs.stream()
                .map(song -> song.getAlbumName())
                .filter(album -> album.isPresent())
                .map(album -> album.get())
                .filter(album -> album.equals(albumName))
                .count();
    }

    @Override
    public int countSongsInNoAlbum() {
        return (int) this.songs.stream()
                .filter(song -> !song.getAlbumName().isPresent())
                .count();
    }

    @Override
    public OptionalDouble averageDurationOfSongs(final String albumName) {
        return OptionalDouble.of(
                durationOfSongs(albumName).orElse(0.0) / countSongs(albumName));
    }

    @Override
    public Optional<String> longestSong() {
        return this.songs.stream()
                .max((s1, s2) -> {
                    final double d1 = s1.getDuration();
                    final double d2 = s2.getDuration();
                    if (d1 == d2) {
                        return 0;
                    }
                    return d1 > d2 ? 1 : -1;
                })
                .map(song -> song.getSongName());
    }

    @Override
    public Optional<String> longestAlbum() {
        return this.albumNames()
                .max((a1, a2) -> {
                    double d1 = durationOfSongs(a1).orElse(0.0);
                    double d2 = durationOfSongs(a2).orElse(0.0);
                    if (d1 == d2) {
                        return 0;
                    }
                    return d1 > d2 ? 1 : -1;
                });
    }

    private OptionalDouble durationOfSongs(final String albumName) {
        return OptionalDouble.of(
                this.songs.stream()
                        .filter(song -> song.getAlbumName().isPresent())
                        .filter(song -> song.getAlbumName().get().equals(albumName))
                        .map(song -> song.getDuration())
                        .reduce((d1, d2) -> d1 + d2).orElse(0.0));
    }

    private static final class Song {

        private final String songName;
        private final Optional<String> albumName;
        private final double duration;
        private int hash;

        Song(final String name, final Optional<String> album, final double len) {
            super();
            this.songName = name;
            this.albumName = album;
            this.duration = len;
        }

        public String getSongName() {
            return songName;
        }

        public Optional<String> getAlbumName() {
            return albumName;
        }

        public double getDuration() {
            return duration;
        }

        @Override
        public int hashCode() {
            if (hash == 0) {
                hash = songName.hashCode() ^ albumName.hashCode() ^ Double.hashCode(duration);
            }
            return hash;
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj instanceof Song) {
                final Song other = (Song) obj;
                return albumName.equals(other.albumName) && songName.equals(other.songName)
                        && duration == other.duration;
            }
            return false;
        }

        @Override
        public String toString() {
            return "Song [songName=" + songName + ", albumName=" + albumName + ", duration=" + duration + "]";
        }

    }

}
